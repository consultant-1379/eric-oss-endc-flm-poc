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
import com.ericsson.oss.apps.model.mom.*;
import com.ericsson.oss.apps.model.pmrop.MoRopId;
import com.ericsson.oss.apps.model.pmrop.PmRopGUtranCellRelation;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.*;
import com.ericsson.oss.apps.topology.IdentityService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@DataJpaTest
@Import({CellHitRateService.class, CellFilterService.class, CellRelationService.class})
@Sql("classpath:db/cm_relation_test.sql")
public class CellHitRateServiceTest {
    @Autowired
    private CellHitRateService cellHitRateService;
    @Autowired
    private CmGUtranFreqRelationRepo cmGUtranFreqRelationRepo;
    @Autowired
    private CmGUtranSyncSignalFrequencyRepo cmGUtranSyncSignalFrequencyRepo;
    @Autowired
    private PmGUtranCellRelationRepo pmGUtranCellRelationRepo;
    @Autowired
    private CmEUtranCellRepo cmEUtranCellRepo;
    @Autowired
    private CmNRCellCURepo cmNRCellCURepo;
    @Autowired
    private CmNRCellDURepo cmNRCellDURepo;
    @MockBean
    private WebClient webClient;
    @MockBean
    private IdentityService identityService;
    @MockBean
    private CmDataLoader cmDataLoader;

    private static final long ROP_TIME = 100;
    private static final long NCI = 1343813;
    private static final long PM_ENDC_SETUP_SCG_ATT = 90;
    private static final List<PLMNId> plmnIds = List.of(new PLMNId(1, 2, 3));

    private static final ManagedObjectId GUTRAN_CELLRELATION_1021 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054815,GUtranFreqRelation=426970-15-20-0-2,GUtranCellRelation=5251-0000000001037902-1021");
    private static final ManagedObjectId GUTRAN_CELLRELATION_1022 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3,GUtranCellRelation=5251-0000000001037902-1022");
    private static final ManagedObjectId GUTRAN_CELLRELATION_1023 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3,GUtranCellRelation=5251-0000000001037902-1023");
    private static final ManagedObjectId GUTRAN_CELLRELATION_1024 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054816,GUtranFreqRelation=426970-15-20-0-3,GUtranCellRelation=5251-0000000001037902-1024");

    private static final ManagedObjectId NR_CELL_DU = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBDUFunction=1,NRCellDU=NR03gNodeBRadio00001-1");

    private static final ManagedObjectId NR_CELL_CU_ID_1021 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1");
    private static final ManagedObjectId NR_CELL_CU_ID_1022 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-2");
    private static final ManagedObjectId NR_CELL_CU_ID_1023 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-3");
    private static final ManagedObjectId NR_CELL_CU_ID_1024 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-4");


    @BeforeEach
    @Transactional
    public void setUp() {
        setUpPmEndcSetupScgUeAtt(List.of(GUTRAN_CELLRELATION_1021, GUTRAN_CELLRELATION_1022, GUTRAN_CELLRELATION_1023, GUTRAN_CELLRELATION_1024));

        List<GUtranSyncSignalFrequency> gUtranSyncSignalFrequencys = cmGUtranSyncSignalFrequencyRepo.findAll();
        List<EUtranCell> eUtranCells = cmEUtranCellRepo.findAll();
        List<GUtranFreqRelation> gUtranFreqRelations = cmGUtranFreqRelationRepo.findAll();

        eUtranCells.forEach(eUtranCell -> eUtranCell.setEndcAllowedPlmnList(plmnIds));
        AtomicInteger index = new AtomicInteger(0);
        gUtranFreqRelations.forEach(gUtranFreqRelation -> {
            gUtranFreqRelation.setEndcB1MeasPriority(2);
            gUtranFreqRelation.setGUtranSyncSignalFrequencyRef(gUtranSyncSignalFrequencys.get(index.getAndIncrement()));
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

    private void setUpPmEndcSetupScgUeAtt(List<ManagedObjectId> objectIds) {
        List<PmRopGUtranCellRelation> pmRopGUtranCellRelations = new ArrayList<>();

        objectIds.forEach(objectId ->
        {
            PmRopGUtranCellRelation pmRopGUtranCellRelation = new PmRopGUtranCellRelation();
            pmRopGUtranCellRelation.setMoRopId(new MoRopId(objectId, ROP_TIME));
            pmRopGUtranCellRelation.setPmEndcSetupScgUeAtt(PM_ENDC_SETUP_SCG_ATT);
            if (objectId == GUTRAN_CELLRELATION_1024) {
                pmRopGUtranCellRelation.setPmEndcSetupScgUeAtt(Double.NaN);
            }
            pmRopGUtranCellRelations.add(pmRopGUtranCellRelation);
        });
        pmGUtranCellRelationRepo.saveAll(pmRopGUtranCellRelations);
    }

    @Test
    public void processEutranCellHitRate() {
        List<EUtranCell> eUtranCells = cmEUtranCellRepo.findAll();
        Map<EUtranCell, Map<Integer, Map<ManagedObjectId, Double>>> result =
                cellHitRateService.processEutranCellHitRate(eUtranCells.stream().map(ManagedObject::getObjectId).toList(), ROP_TIME);

        // 1st eUtranCell has one gUtranFreqRelation(arfcn 33), one gUtranCellRelation(PmEndcSetupScgUeAtt 90)
        Assertions.assertEquals(1F, result.get(eUtranCells.get(0)).get(33).get(NR_CELL_CU_ID_1021));
        // 2nd eUtranCell has one gUtranFreqRelation(arfcn 44), three gUtranCellRelation(PmEndcSetupScgUeAtt 90, 90, NaN)
        Assertions.assertEquals(0.5F, result.get(eUtranCells.get(1)).get(44).get(NR_CELL_CU_ID_1022));
        Assertions.assertEquals(0.5F, result.get(eUtranCells.get(1)).get(44).get(NR_CELL_CU_ID_1023));
        // 3rd gUtranCellRelation has PmEndcSetupScgUeAtt NaN, it is invalid but store it in case for future debugging
        Assertions.assertEquals(Double.NaN, result.get(eUtranCells.get(1)).get(44).get(NR_CELL_CU_ID_1024));
    }
}
