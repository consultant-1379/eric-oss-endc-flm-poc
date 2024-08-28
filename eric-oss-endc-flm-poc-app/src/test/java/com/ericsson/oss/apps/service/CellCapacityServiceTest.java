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


import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.mom.NRCellCU;
import com.ericsson.oss.apps.model.mom.NRCellDU;
import com.ericsson.oss.apps.model.mom.NRSectorCarrier;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmNRCellCURepo;
import com.ericsson.oss.apps.repository.CmNRCellDURepo;
import com.ericsson.oss.apps.repository.CmNRSectorCarrierRepo;
import com.ericsson.oss.apps.topology.model.PLMNId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.ericsson.oss.apps.model.mom.NRCellDU.ModulationOrder.QAM_256;

@SpringBootTest
public class CellCapacityServiceTest {
    @Autowired
    private CellCapacityService cellCapacityService;
    @Autowired
    private CmNRCellCURepo cmNRCellCURepo;
    @Autowired
    private CmNRCellDURepo cmNRCellDURepo;
    @Autowired
    private CmNRSectorCarrierRepo cmNRSectorCarrierRepo;

    private static final Long NCI = 123L;
    private static final String EUTRA_CELL_FDN = "EUTRA_CELL_FDN";
    private static final String PCELL1_FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=1037902101";
    private static final String PCELL2_FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=1037902102";
    private static final String PCELL3_FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=1037902103";
    private static final String SCELL4_FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=1037902104";

private List<SecondaryCellGroup> SECONDARY_CELL_GROUPS;

    @BeforeEach
    @Transactional
    public void setUp() {
        // One NRCellCU will reference to one NRCellDU, but for testing purpose, one NRCellDU and NRSectorCarrier is used
        // Below setting will result each cell with cell weight of 55642860 and bandwidth of 10
        NRSectorCarrier nrSectorCarrier = new NRSectorCarrier("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBDUFunction=1,NRSectorCarrier=DX-21380_2S001");
        nrSectorCarrier.setBSChannelBwDL(10);
        nrSectorCarrier.setArfcnDL(159600);
        nrSectorCarrier.setArfcnUL(148600);

        NRCellDU nrCellDU = new NRCellDU();
        nrCellDU.setObjectId(ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBDUFunction=1,NRCellDU=2034902075"));
        nrCellDU.setNRSectorCarrierRef(List.of(nrSectorCarrier));
        nrCellDU.setNCI(NCI);
        nrCellDU.setBandList(List.of(32));
        nrCellDU.setSubCarrierSpacing(15);
        nrCellDU.setDl256QamEnabled(true);

        NRCellCU pCell1 = createNRCellCU(ManagedObjectId.of(PCELL1_FDN));
        NRCellCU pCell2 = createNRCellCU(ManagedObjectId.of(PCELL2_FDN));
        NRCellCU pCell3 = createNRCellCU(ManagedObjectId.of(PCELL3_FDN));
        NRCellCU sCell4 = createNRCellCU(ManagedObjectId.of(SCELL4_FDN));

        cmNRSectorCarrierRepo.save(nrSectorCarrier);
        cmNRCellDURepo.save(nrCellDU);
        cmNRCellCURepo.saveAll(List.of(pCell1, pCell2, pCell3, sCell4));

        SECONDARY_CELL_GROUPS = List.of(
                // 1st SCG total capacity, cell weight -> 55642860/2 + 55642860/3 + 55642860/2 = 74190480, bandwidth -> 10/2 + 10/3 + 10/2 = 13.33
                new SecondaryCellGroup(pCell1, List.of(sCell4, pCell2), 345),
                // 2nd SCG total capacity, cell weight -> 55642860/2 + 55642860/3 + 55642860/2 = 74190480, bandwidth -> 10/2 + 10/3 + 10/2 = 13.33
                new SecondaryCellGroup(pCell2, List.of(pCell3, sCell4, pCell1), 377),
                // 3rd SCG total capacity, cell weight -> 55642860/1 + 55642860/3 = 74190480, bandwidth -> 10/1 + 10/3 = 13.33
                new SecondaryCellGroup(pCell3, List.of(sCell4), 377));
    }

    @Test
    void processCapacityPerFrequency() {
        Map<Integer, Float> capacityPerFrequency = cellCapacityService.processCapacityPerFrequency(EUTRA_CELL_FDN, new ArrayList<>(), true);
        Assertions.assertEquals(0, capacityPerFrequency.size());
        Assertions.assertEquals(0, cellCapacityService.getFreqCapacityMap().size());
        Assertions.assertEquals(0, cellCapacityService.getNrCellCapacityMap().size());

        capacityPerFrequency = cellCapacityService.processCapacityPerFrequency(EUTRA_CELL_FDN, SECONDARY_CELL_GROUPS, true);
        Assertions.assertEquals(2,capacityPerFrequency.size());
        // For arfcn:345, capacityPerFrequency  74190480/1 = 74190480
        Assertions.assertEquals(74190480, capacityPerFrequency.get(345));
        // For arfcn:377, capacityPerFrequency  (74190480 + 74190480) / 2 = 74190480
        Assertions.assertEquals(74190480, capacityPerFrequency.get(377));
        Assertions.assertEquals(1, cellCapacityService.getFreqCapacityMap().size());
        Assertions.assertEquals(2, cellCapacityService.getFreqCapacityMap().get(EUTRA_CELL_FDN).size());
        // Per Frequency capacity, use same calculation for verifying capacityPerFrequency.
        Assertions.assertEquals(74190480, cellCapacityService.getFreqCapacityMap().get(EUTRA_CELL_FDN).get(377));
        Assertions.assertEquals(74190480, cellCapacityService.getFreqCapacityMap().get(EUTRA_CELL_FDN).get(345));
        Assertions.assertEquals(1, cellCapacityService.getNrCellCapacityMap().size());
        Assertions.assertEquals(4, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).size());
        // pCell1: cell weight -> 55642860/2 + 55642860/2/2 = 41732145
        Assertions.assertEquals(41732145, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).get(PCELL1_FDN));
        // pCell2: cell weight -> 55642860/2 + 55642860/2/2 = 41732145
        Assertions.assertEquals(41732145, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).get(PCELL2_FDN));
        // pCell3: cell weight -> 55642860/2/2 + 55642860/2/2 = 27821430
        Assertions.assertEquals(27821430, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).get(PCELL3_FDN));
        // sCell4: cell weight -> 55642860/3 + 55642860/3/2 + 55642860/3/2 = 37095240
        Assertions.assertEquals(37095240, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).get(SCELL4_FDN));

        cellCapacityService.setNrCellCapacityMap(new HashMap<>());  // Need reset the per-cell data for a new round test.

        capacityPerFrequency = cellCapacityService.processCapacityPerFrequency(EUTRA_CELL_FDN, SECONDARY_CELL_GROUPS, false);
        Assertions.assertEquals(2,capacityPerFrequency.size());
        // For arfcn:345, capacityPerFrequency  13.33/1 = 13.33
        Assertions.assertEquals(13.333333F, capacityPerFrequency.get(345), 0.000001F);
        // For arfcn:377, capacityPerFrequency  (13.33 + 13.33) / 2 = 13.33
        Assertions.assertEquals(13.333333F, capacityPerFrequency.get(377), 0.000001F);

        Assertions.assertEquals(1, cellCapacityService.getFreqCapacityMap().size());
        Assertions.assertEquals(2, cellCapacityService.getFreqCapacityMap().get(EUTRA_CELL_FDN).size());
        // Per Frequency capacity, use same calculation for verifying capacityPerFrequency.
        Assertions.assertEquals(13.333333F, cellCapacityService.getFreqCapacityMap().get(EUTRA_CELL_FDN).get(377), 0.000001F);
        Assertions.assertEquals(13.333333F, cellCapacityService.getFreqCapacityMap().get(EUTRA_CELL_FDN).get(345), 0.000001F);
        Assertions.assertEquals(1, cellCapacityService.getNrCellCapacityMap().size());
        Assertions.assertEquals(4, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).size());
        // pCell1: cell weight -> 10/2 + 10/2/2 = 7.5
        Assertions.assertEquals(7.5F, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).get(PCELL1_FDN));
        // pCell2: cell weight -> 10/2 + 10/2/2 = 7.5
        Assertions.assertEquals(7.5F, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).get(PCELL2_FDN));
        // pCell3: cell weight -> 10/2/2 + 10/2/2 = 5
        Assertions.assertEquals(5F, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).get(PCELL3_FDN));
        // sCell4: cell weight -> 10/3 + 10/3/2 + 10/3/2 = 6.6666667
        Assertions.assertEquals(6.6666666F, cellCapacityService.getNrCellCapacityMap().get(EUTRA_CELL_FDN).get(SCELL4_FDN), 0.0000001F);
    }

    @Test
    void cellWeightCalc() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("__files/nrCellWeightCalc.json").getInputStream();
        JsonNode mapper = objectMapper.readTree(inputStream);
        mapper.forEach(this::validateCellWeight);
    }

    private void validateCellWeight(JsonNode jsonNode) {
        NRCellDU nrCellDU = new NRCellDU();
        String dlAvailableCrbs = jsonNode.get("dlAvailableCrbs").toString();
        String bandList = jsonNode.get("bandList").toString();
        int tddUlDlPattern = Integer.parseInt(jsonNode.get("tddUlDlPattern").toString());
        nrCellDU.setObjectId(ManagedObjectId.of(jsonNode.get("fdn").toString()));
        nrCellDU.setDlAvailableCrbs(Objects.equals(dlAvailableCrbs, "null") ? null : Integer.parseInt(dlAvailableCrbs));
        nrCellDU.setBandList(Objects.equals(bandList, "null") ? List.of(32, 0) : List.of(Integer.parseInt(bandList)));
        nrCellDU.setSubCarrierSpacing(Integer.parseInt(jsonNode.get("ssbSubCarrierSpacing").toString()));
        nrCellDU.setDlMaxSupportedModOrder(Objects.equals(jsonNode.get("dlMaxSupportedModOrder").toString(), "null") ? null : QAM_256);
        nrCellDU.setDl256QamEnabled(Objects.equals(jsonNode.get("dl256QamEnabled").toString(), "true"));
        nrCellDU.setTddUlDlPattern(NRCellDU.TddUlDlPatternType.values()[tddUlDlPattern]);

        NRSectorCarrier nrSectorCarrier = new NRSectorCarrier();
        String essScLocalId = jsonNode.get("essScLocalId").toString();
        nrSectorCarrier.setObjectId(ManagedObjectId.of(jsonNode.get("nRSectorCarrierRef(without vsData)").toString()));
        nrSectorCarrier.setBSChannelBwDL(Integer.parseInt(jsonNode.get("bSChannelBwDL").toString()));
        nrSectorCarrier.setEssScLocalId(Objects.equals(essScLocalId, "null") ? null : Integer.parseInt(jsonNode.get("essScLocalId").toString()));
        nrSectorCarrier.setArfcnDL(Integer.parseInt(jsonNode.get("arfcnDL").toString()));
        nrSectorCarrier.setArfcnUL(Integer.parseInt(jsonNode.get("arfcnUL").toString()));

        float expectedCellWeight = Float.parseFloat(jsonNode.get("nDL CellWeight").toString());
        float cellWeight = cellCapacityService.calculateCellWeight(nrCellDU, nrSectorCarrier);
        float diffByPercent = (Math.abs(expectedCellWeight - cellWeight) / expectedCellWeight * 100);
        // Since cellWeight is around 9 digits number, the difference in percentage that is less than 0.03% is allowed
        Assertions.assertTrue(0.03F > diffByPercent);
    }

    private NRCellCU createNRCellCU (ManagedObjectId objectId)
    {
        NRCellCU nrCellCU = new NRCellCU();
        nrCellCU.setObjectId(objectId);
        nrCellCU.setNCI(NCI);
        nrCellCU.setPrimaryPLMNId(new PLMNId(525, 1, 2));
        return nrCellCU;
    }
}
