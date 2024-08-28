/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.apps.service;

import com.ericsson.oss.apps.loader.CmDataLoader;
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.mom.*;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.*;
import com.ericsson.oss.apps.topology.IdentityService;
import com.ericsson.oss.apps.topology.model.NRCellId;
import com.ericsson.oss.apps.topology.model.PLMNId;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;

@DataJpaTest
@Import({CellFilterService.class, CellRelationService.class})
@Sql("classpath:db/cm_relation_test.sql")
public class CellFilterServiceTest {

    @Autowired
    private CellFilterService cellFilterService;
    @Autowired
    private CmGUtranFreqRelationRepo cmGUtranFreqRelationRepo;
    @Autowired
    private CmNRCellCURepo cmNRCellCURepo;
    @Autowired
    private CmGUtranSyncSignalFrequencyRepo cmGUtranSyncSignalFrequencyRepo;
    @Autowired
    private CmEUtranCellRepo cmEUtranCellRepo;
    @Autowired
    private CmNRCellDURepo cmNRCellDURepo;
    @MockBean
    private WebClient webClient;
    @MockBean
    private IdentityService identityService;
    @MockBean
    private CmDataLoader cmDataLoader;
    private static final long NCI = 1343813;
    private static final List<PLMNId> plmnIds = List.of(new PLMNId(1, 2, 3));
    private static final NRCellId NR_CELL_ID = new NRCellId(new PLMNId(525, 1, 2), 1037902, 23, 1021);
    private static final ManagedObjectId NR_CELL_CU_ID = ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=10379021021");
    private static final ManagedObjectId EUTRAN_CELL_ID_1 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054815");
    private static final ManagedObjectId EUTRAN_CELL_ID_2 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054816");
    private static final ManagedObjectId PRIMARY_NR_CELL = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1");

    private static final ManagedObjectId NR_CELL_DU = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBDUFunction=1,NRCellDU=NR03gNodeBRadio00001-1");

    @BeforeEach
    @Transactional
    public void setUp() {

        GUtranSyncSignalFrequency gUtranSyncSignalFrequency = cmGUtranSyncSignalFrequencyRepo.findAll().get(0);

        List<EUtranCell> eUtranCells = cmEUtranCellRepo.findAll();
        eUtranCells.forEach(eUtranCell -> eUtranCell.setEndcAllowedPlmnList(plmnIds));

        List<GUtranFreqRelation> gUtranFreqRelations = cmGUtranFreqRelationRepo.findAll();
        gUtranFreqRelations.forEach(gUtranFreqRelation -> {
            gUtranFreqRelation.setEndcB1MeasPriority(2);
            gUtranFreqRelation.setGUtranSyncSignalFrequencyRef(gUtranSyncSignalFrequency);
        });

        List<NRCellCU> nrCellCUs = cmNRCellCURepo.findAll();
        assert !nrCellCUs.isEmpty();
        nrCellCUs.forEach(nrCellCU -> nrCellCU.setNCI(NCI));

        // For testing purpose, setting this NRCellDU relates to all NRCellCUs and a valid sub carrier Spacing
        NRCellDU nrCellDU = new NRCellDU();
        nrCellDU.setObjectId(NR_CELL_DU);
        nrCellDU.setNCI(NCI);
        nrCellDU.setSubCarrierSpacing(30);
        cmNRCellDURepo.save(nrCellDU);
    }

    @Test
    void fetchSecondaryCellGroups() {
        List<EUtranCell> eUtranCells = cmEUtranCellRepo.findAll();
        when(identityService.getObjectIdFromCellId(NR_CELL_ID)).thenReturn(Optional.of(NR_CELL_CU_ID));
        Map<String, List<SecondaryCellGroup>> eUtranCellToSCGsMap =
                cellFilterService.fetchSecondaryCellGroups(eUtranCells.stream().map(ManagedObject::getObjectId).toList());
        Optional<EUtranCell> eUtranCell_1 = cmEUtranCellRepo.findById(EUTRAN_CELL_ID_1);
        Optional<EUtranCell> eUtranCell_2 = cmEUtranCellRepo.findById(EUTRAN_CELL_ID_2);
        Optional<NRCellCU> primaryNRCell = cmNRCellCURepo.findById(PRIMARY_NR_CELL);

        if (eUtranCell_1.isPresent() && eUtranCell_2.isPresent() && primaryNRCell.isPresent()) {
            Integer arfcn = eUtranCellToSCGsMap.get(eUtranCell_1.get().getFdn()).get(0).arfcn();
            // Current eUtranCells size is 2, if size increased, below check may have to change
            Assertions.assertEquals(2, eUtranCells.size());
            // This eutran cell has not SCGs
            Assertions.assertEquals(0, eUtranCellToSCGsMap.get(eUtranCell_2.get().getFdn()).size());

            // this eutran cell only have one SCG
            Assertions.assertEquals(1, eUtranCellToSCGsMap.get(eUtranCell_1.get().getFdn()).size());
            Assertions.assertEquals(primaryNRCell.get().getObjectId(), eUtranCellToSCGsMap.get(eUtranCell_1.get().getFdn()).get(0).primaryNRCell().getObjectId());
            // All sCells are from NRCellCU, ExternalNRCellCU only have dummy date which will return empty
            Assertions.assertEquals(2, eUtranCellToSCGsMap.get(eUtranCell_1.get().getFdn()).get(0).secondaryCells().size());
            Assertions.assertEquals(33, arfcn);
        } else {
            Assertions.fail("Above checks are not triggered");
        }
    }

    @Test
    void generateSCGsUsage() {
        NRCellCU pCell1 = new NRCellCU();
        pCell1.setObjectId(ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=1037902101"));
        NRCellCU pCell2 = new NRCellCU();
        pCell2.setObjectId(ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=1037902102"));
        NRCellCU pCell3 = new NRCellCU();
        pCell3.setObjectId(ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=1037902103"));

        NRCellCU sCell4 = new NRCellCU();
        sCell4.setObjectId(ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=1037902104"));
        NRCellCU sCell5 = new NRCellCU();
        sCell5.setObjectId(ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=1037902105"));

        List<SecondaryCellGroup> SECONDARY_CELL_GROUPS = List.of(
                new SecondaryCellGroup(pCell1, List.of(sCell4, sCell5, pCell2), 345),
                new SecondaryCellGroup(pCell2, List.of(sCell4, sCell5, pCell1), 377),
                new SecondaryCellGroup(pCell3, List.of(sCell4, sCell5), 377));

        cellFilterService.generateSCGsUsageData(SECONDARY_CELL_GROUPS);

        Assertions.assertEquals(1, cellFilterService.getFrequencyUsage().get(345));
        Assertions.assertEquals(2, cellFilterService.getFrequencyUsage().get(377));

        Assertions.assertEquals(2, cellFilterService.getCellUsage().get(pCell1));
        Assertions.assertEquals(2, cellFilterService.getCellUsage().get(pCell2));
        Assertions.assertEquals(1, cellFilterService.getCellUsage().get(pCell3));

        Assertions.assertEquals(3, cellFilterService.getCellUsage().get(sCell4));
        Assertions.assertEquals(3, cellFilterService.getCellUsage().get(sCell5));
    }
}
