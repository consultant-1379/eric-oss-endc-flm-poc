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

import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.mom.EUtranCellFDD;
import com.ericsson.oss.apps.model.pmrop.MoRopId;
import com.ericsson.oss.apps.model.pmrop.PmRopNRCellCU;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.PmNRCellCURepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest
public class WeightedAverageRRCServiceTest {
    @Autowired
    private WeightedAverageRRCService weightedAverageRRCService;
    @Autowired
    private PmNRCellCURepo pmNRCellCURepo;
    @MockBean
    private CellHitRateService cellHitRateService;
    private static final long ROP_TIME = 100;
    private static final Integer ARFCN_30 = 30;
    private static final Integer ARFCN_56 = 56;

    private static final ManagedObjectId NRCELLCU_ID_1 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1");
    private static final ManagedObjectId NRCELLCU_ID_2 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-2");
    private static final ManagedObjectId NRCELLCU_ID_3 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-3");

    private static final ManagedObjectId EUTRAN_CELL_FDD_ID_1 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054815");
    private static final ManagedObjectId EUTRAN_CELL_FDD_ID_2 = new ManagedObjectId("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054816");

    private static final EUtranCellFDD EUTRAN_CELL_FDD_1 = new EUtranCellFDD(EUTRAN_CELL_FDD_ID_1.toString());
    private static final EUtranCellFDD EUTRAN_CELL_FDD_2 = new EUtranCellFDD(EUTRAN_CELL_FDD_ID_2.toString());

    @BeforeEach
    public void setup() {
        setUpPmNRCellCURepo(Map.ofEntries(
                Map.entry(NRCELLCU_ID_1, List.of(3600D, 180D)),
                Map.entry(NRCELLCU_ID_2, List.of(13140D, 180D)),
                Map.entry(NRCELLCU_ID_3, List.of(5940D, 180D))));
    }

    private void setUpPmNRCellCURepo(Map<ManagedObjectId, List<Double>> objectIdToPmCounterMap) {

        List<PmRopNRCellCU> pmRopNRCellCUs = objectIdToPmCounterMap.entrySet().stream()
                .map(entry -> {
                    PmRopNRCellCU pmRopNRCellCU = new PmRopNRCellCU();
                    pmRopNRCellCU.setMoRopId(new MoRopId(entry.getKey(), ROP_TIME));
                    pmRopNRCellCU.setPmRrcConnLevelSumEnDc(entry.getValue().get(0));
                    pmRopNRCellCU.setPmRrcConnLevelSamp(entry.getValue().get(1));
                    return pmRopNRCellCU;
                })
                .toList();

        pmNRCellCURepo.saveAll(pmRopNRCellCUs);
    }

    @Test
    void processWeightedAverageRRCConnUsers() {
        Map<EUtranCell, Map<Integer, Map<ManagedObjectId, Double>>> HIT_RATE = Map.ofEntries(
                Map.entry(EUTRAN_CELL_FDD_1, Map.ofEntries(
                        Map.entry(ARFCN_30,
                                Map.ofEntries(Map.entry(NRCELLCU_ID_1, 0.33333333D), Map.entry(NRCELLCU_ID_2, 0.33333333D), Map.entry(NRCELLCU_ID_3, 0.33333333D))),
                        Map.entry(ARFCN_56,
                                Map.ofEntries(Map.entry(NRCELLCU_ID_1, Double.NaN), Map.entry(NRCELLCU_ID_2, 0.5D)))
                )),
                Map.entry(EUTRAN_CELL_FDD_2, Map.ofEntries(
                        Map.entry(ARFCN_30,
                                Map.ofEntries(Map.entry(NRCELLCU_ID_1, 0.4D), Map.entry(NRCELLCU_ID_2, 0.3D), Map.entry(NRCELLCU_ID_3, 0.3D))),
                        Map.entry(ARFCN_56,
                                Map.ofEntries(Map.entry(NRCELLCU_ID_1, 0.4D), Map.entry(NRCELLCU_ID_2, 0.4D), Map.entry(NRCELLCU_ID_3, 0.2D)))
                ))
        );

        when(cellHitRateService.processEutranCellHitRate(List.of(EUTRAN_CELL_FDD_ID_1, EUTRAN_CELL_FDD_ID_2), ROP_TIME)).thenReturn(HIT_RATE);

        Map<EUtranCell, Map<Integer, Float>> weightedAverageRRC = weightedAverageRRCService.processWeightedAverageRRCConnUsers(List.of(EUTRAN_CELL_FDD_ID_1, EUTRAN_CELL_FDD_ID_2), ROP_TIME);
        // (0.33333333 * (3600 / 180)) + (0.33333333 * (13140 / 180)) + (0.33333333 * (5940 / 180)) = 42
        Assertions.assertEquals(42F, weightedAverageRRC.get(EUTRAN_CELL_FDD_1).get(ARFCN_30), 0.0001);
        // since one Cell's hitRate has NaN, the final result as NaN
        Assertions.assertEquals(Float.NaN, weightedAverageRRC.get(EUTRAN_CELL_FDD_1).get(ARFCN_56));
        // (0.4 * (3600 / 180)) + (0.3 * (13140 / 180)) + (0.3 * (5940 / 180)) = 39.8
        Assertions.assertEquals(39.8, weightedAverageRRC.get(EUTRAN_CELL_FDD_2).get(ARFCN_30), 0.0001);
        // (0.4 * (3600 / 180)) + (0.4 * (13140 / 180)) + (0.2 * (5940 / 180)) = 43.8
        Assertions.assertEquals(43.8F, weightedAverageRRC.get(EUTRAN_CELL_FDD_2).get(ARFCN_56), 0.0001);

        Assertions.assertEquals(2, weightedAverageRRCService.getFreqLoadMap().size());
    }
}
