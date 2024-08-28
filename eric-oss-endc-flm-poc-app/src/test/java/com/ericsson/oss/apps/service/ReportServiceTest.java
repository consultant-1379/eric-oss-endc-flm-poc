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

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.ericsson.oss.apps.model.EndcFreqProfileData;
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.mom.EUtranCellFDD;
import com.ericsson.oss.apps.model.mom.EndcDistrProfile;
import com.ericsson.oss.apps.model.mom.GUtranSyncSignalFrequency;
import com.ericsson.oss.apps.model.mom.NRCellCU;
import com.ericsson.oss.apps.model.report.AllowedEUtranCellReportTuple;
import com.ericsson.oss.apps.model.report.AllowedMoReportTuple;
import com.ericsson.oss.apps.model.report.CellDataReportTuple;
import com.ericsson.oss.apps.model.report.EndcDistrProfileDataStatus;
import com.ericsson.oss.apps.model.report.Report;
import com.ericsson.oss.apps.model.report.SCellReportTuple;
import com.ericsson.oss.apps.model.report.ScgReportTuple;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmEUtranCellRepo;
import com.ericsson.oss.apps.repository.CmEndcDistrProfileRepo;
import com.ericsson.oss.apps.repository.ReportAllowEUtranCellsRepo;
import com.ericsson.oss.apps.repository.ReportAllowListRepo;
import com.ericsson.oss.apps.repository.ReportCellDataRepo;
import com.ericsson.oss.apps.repository.ReportDataRepo;
import com.ericsson.oss.apps.repository.ReportSCellRepo;
import com.ericsson.oss.apps.repository.ReportSecondaryCellGroupRepo;

@SpringBootTest
public class ReportServiceTest {
    @Autowired
    ReportService reportService;

    @Autowired
    ReportDataRepo reportDataRepo;
    @Autowired
    ReportAllowListRepo allowListRepo;
    @Autowired
    ReportAllowEUtranCellsRepo allowEutranCellsRepo;
    @Autowired
    ReportCellDataRepo cellDataRepo;
    @Autowired
    ReportSecondaryCellGroupRepo scgDataRepo;
    @Autowired
    ReportSCellRepo sCellRepo;
    @Autowired
    CmEUtranCellRepo eUtranCellRepo;
    @Autowired
    CmEndcDistrProfileRepo endcDistrProfileRepo;

    @MockBean
    CellCapacityService cellCapacityService;
    @MockBean
    WeightedAverageRRCService weightedAverageRRCService;

    private final long ROP_TIMESTAMP = 1234L;

    private final String ME_FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building";
    private final String ENB1_FDN = ME_FDN + ",ENodeBFunction=1";
    private final String CELL_FDN1 = ENB1_FDN + ",EUtranCellFDD=1";
    private final String GNB1_FDN = ME_FDN + ",GNBCUCPFunction=1";
    private final String NR_CELL_FDN1 = GNB1_FDN + ",NRCellCU=1";
    private final String NR_CELL_FDN2 = GNB1_FDN + ",NRCellCU=2";
    private final String PROFILE_FDN_1 = ENB1_FDN + ",EndcDistrProfile=1";


    private final ManagedObjectId CELL1 = ManagedObjectId.of(CELL_FDN1);
    private final AllowedMo CELL_MO = new AllowedMo(CELL1, true, true, false);

    private final String GU_NETWORK_FDN = ENB1_FDN + ",GUtraNetwork=1";
    private final String FREQ_FDN_1 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=620000-20";
    private final String FREQ_FDN_2 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=620100-20";
    private final String FREQ_FDN_3 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=620200-20";
    private final String FREQ_FDN_4 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=620300-20";

    private EUtranCell cell1 = new EUtranCellFDD(CELL_FDN1);

    private GUtranSyncSignalFrequency freq1 = new GUtranSyncSignalFrequency(FREQ_FDN_1);
    private GUtranSyncSignalFrequency freq2 = new GUtranSyncSignalFrequency(FREQ_FDN_2);
    private GUtranSyncSignalFrequency freq3 = new GUtranSyncSignalFrequency(FREQ_FDN_3);
    private GUtranSyncSignalFrequency freq4 = new GUtranSyncSignalFrequency(FREQ_FDN_4);

    private List<GUtranSyncSignalFrequency> gUtranFreqRef1 = List.of(freq1, freq2);
    private List<GUtranSyncSignalFrequency> mandatoryGUtranFreqRef1 = List.of(freq3, freq4);
    private Map<String, Integer> distributionMap1 = Map.of(FREQ_FDN_1, 30, FREQ_FDN_2, 20);
    private EndcFreqProfileData profileData1 = new EndcFreqProfileData(distributionMap1, gUtranFreqRef1, mandatoryGUtranFreqRef1);

    @BeforeEach
    void setup() {
        reportDataRepo.deleteAll();
        allowListRepo.deleteAll();
        allowEutranCellsRepo.deleteAll();
        cellDataRepo.deleteAll();
        scgDataRepo.deleteAll();
        sCellRepo.deleteAll();
        eUtranCellRepo.deleteAll();
        endcDistrProfileRepo.deleteAll();
    }

    @Test
    void createNewReport() {
        List<AllowedMo> allowedMos = List.of(CELL_MO);
        NRCellCU primaryCellCu1 = new NRCellCU(NR_CELL_FDN1);
        List<NRCellCU> secondaryCellCus1 = List.of(new NRCellCU(NR_CELL_FDN2));
        NRCellCU primaryCellCu2 = new NRCellCU(NR_CELL_FDN2);
        List<NRCellCU> secondaryCellCus2 = List.of(new NRCellCU(NR_CELL_FDN1));
        Integer arfcn = 100;
        Float totalCapaity = 100F;
        Float totalLoad = 235F;
        Float nrCellCu1Capacity = 40F;
        Float nrCellCu2Capacity = 60F;
        Float nrCellCu1Load = 110F;
        Float nrCellCu2Load = 125F;
        Map<String, List<SecondaryCellGroup>> cellToScgMap =
                Map.of(CELL_FDN1, List.of(new SecondaryCellGroup(primaryCellCu1, secondaryCellCus1, arfcn),
                                          new SecondaryCellGroup(primaryCellCu2, secondaryCellCus2, arfcn)));
        Map<String, EndcFreqProfileData> cellToProfileMap = Map.of(CELL_FDN1, profileData1);
        Map<String, Map<Integer, Float>> freqCapacityMap = Map.of(CELL_FDN1, Map.of(arfcn, totalCapaity));
        Map<String, Map<Integer, Float>> freqLoadMap = Map.of(CELL_FDN1, Map.of(arfcn, totalLoad));
        Map<String, Map<String, Float>> nrCellCapacityMap = Map.of(CELL_FDN1, Map.of(NR_CELL_FDN1, nrCellCu1Capacity, NR_CELL_FDN2, nrCellCu2Capacity));
        Map<String, Map<String, Float>> nrCellLoadMap = Map.of(CELL_FDN1, Map.of(NR_CELL_FDN1, nrCellCu1Load, NR_CELL_FDN2, nrCellCu2Load));

        eUtranCellRepo.save(cell1);

        when(cellCapacityService.getFreqCapacityMap()).thenReturn(freqCapacityMap);
        when(cellCapacityService.getNrCellCapacityMap()).thenReturn(nrCellCapacityMap);
        when(weightedAverageRRCService.getFreqLoadMap()).thenReturn(freqLoadMap);
        when(weightedAverageRRCService.getNrCellLoadMap()).thenReturn(nrCellLoadMap);

        reportService.createNewReport(ROP_TIMESTAMP, allowedMos, cellToScgMap, cellToProfileMap);

        Assertions.assertEquals(1, allowListRepo.count());
        Assertions.assertEquals(1, allowEutranCellsRepo.count());
        Assertions.assertEquals(1, cellDataRepo.count());
        Assertions.assertEquals(1, reportDataRepo.count());
        Assertions.assertEquals(1, scgDataRepo.count());
        Assertions.assertEquals(2, sCellRepo.count());

        Report reportInDb = reportDataRepo.findById(ROP_TIMESTAMP).get();
        Assertions.assertNotNull(reportInDb);

        List<AllowedMoReportTuple> allowedMoInDb = reportInDb.getAllowList();
        Assertions.assertEquals(1, allowedMoInDb.size());
        validateAllowedMoData(ROP_TIMESTAMP, CELL_MO, allowedMoInDb.get(0));

        List<AllowedEUtranCellReportTuple> allowedCellsInDb = reportInDb.getAllowEutranCells();
        Assertions.assertEquals(1, allowedCellsInDb.size());

        AllowedEUtranCellReportTuple allowedCellInDb = allowedCellsInDb.get(0);
        Assertions.assertEquals(ROP_TIMESTAMP, allowedCellInDb.getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(CELL_FDN1, allowedCellInDb.getReportDataId().getObjectFdn());
        Assertions.assertEquals(1, allowedCellInDb.getScgData().size());

        ScgReportTuple scgInDb = allowedCellInDb.getScgData().get(0);
        Assertions.assertEquals(ROP_TIMESTAMP, scgInDb.getScgDataId().getRopTimeStamp());
        Assertions.assertEquals(CELL_FDN1, scgInDb.getScgDataId().getObjectFdn());
        Assertions.assertEquals(arfcn, scgInDb.getScgDataId().getArfcn());
        Assertions.assertEquals(totalCapaity, scgInDb.getTotalCapacity());
        Assertions.assertEquals(totalLoad, scgInDb.getTotalLoad());
        Assertions.assertEquals(2, scgInDb.getSCells().size());

        List<SCellReportTuple> sCellsInDb = scgInDb.getSCells();
        Assertions.assertEquals(ROP_TIMESTAMP, sCellsInDb.get(0).getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(NR_CELL_FDN1, sCellsInDb.get(0).getReportDataId().getObjectFdn());
        Assertions.assertEquals(nrCellCu1Capacity, sCellsInDb.get(0).getCellCapacity());
        Assertions.assertEquals(nrCellCu1Load, sCellsInDb.get(0).getCellLoad());
        Assertions.assertEquals(ROP_TIMESTAMP, sCellsInDb.get(1).getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(NR_CELL_FDN2, sCellsInDb.get(1).getReportDataId().getObjectFdn());
        Assertions.assertEquals(nrCellCu2Capacity, sCellsInDb.get(1).getCellCapacity());
        Assertions.assertEquals(nrCellCu2Load, sCellsInDb.get(1).getCellLoad());

        List<CellDataReportTuple> cellDataInDb = reportInDb.getCellData();
        Assertions.assertEquals(1, cellDataInDb.size());

        CellDataReportTuple cellDbTuple = cellDataInDb.get(0);
        Assertions.assertEquals(ROP_TIMESTAMP, cellDbTuple.getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(CELL_FDN1, cellDbTuple.getReportDataId().getObjectFdn());
        Assertions.assertEquals("", cellDbTuple.getOldProfileRef());
        Assertions.assertEquals(gUtranFreqRef1, cellDbTuple.getProfileToWrite().getGUtranFreqRef());
        Assertions.assertEquals(mandatoryGUtranFreqRef1, cellDbTuple.getProfileToWrite().getMandatoryGUtranFreqRef());
        Assertions.assertEquals(List.of(30, 20), cellDbTuple.getProfileToWrite().getGUtranFreqDistribution());
        Assertions.assertEquals(false, cellDbTuple.getNewProfileCreated());
        Assertions.assertEquals(EndcDistrProfileDataStatus.PENDING, cellDbTuple.getStatus());
    }

    @Test
    void createNewReportWithoutProfileChange() {
        List<AllowedMo> allowedMos = List.of(CELL_MO);

        EndcDistrProfile oldProfile = new EndcDistrProfile(PROFILE_FDN_1);
        cell1.setEndcDistrProfileRef(oldProfile);

        NRCellCU primaryCellCu = new NRCellCU(NR_CELL_FDN1);
        List<NRCellCU> secondaryCellCus = List.of(new NRCellCU(NR_CELL_FDN2));
        Integer arfcn = 100;
        Map<String, List<SecondaryCellGroup>> cellToScgMap = Map.of(CELL_FDN1, List.of(new SecondaryCellGroup(primaryCellCu, secondaryCellCus, arfcn)));

        endcDistrProfileRepo.save(oldProfile);
        eUtranCellRepo.save(cell1);

        reportService.createNewReport(ROP_TIMESTAMP, allowedMos, cellToScgMap, Map.of());

        Assertions.assertEquals(1, allowListRepo.count());
        Assertions.assertEquals(1, allowEutranCellsRepo.count());
        Assertions.assertEquals(1, cellDataRepo.count());
        Assertions.assertEquals(1, reportDataRepo.count());
        Assertions.assertEquals(1, scgDataRepo.count());
        Assertions.assertEquals(2, sCellRepo.count());

        Report reportInDb = reportDataRepo.findById(ROP_TIMESTAMP).get();
        Assertions.assertNotNull(reportInDb);

        List<AllowedMoReportTuple> allowedMoInDb = reportInDb.getAllowList();
        Assertions.assertEquals(1, allowedMoInDb.size());
        validateAllowedMoData(ROP_TIMESTAMP, CELL_MO, allowedMoInDb.get(0));

        List<AllowedEUtranCellReportTuple> allowedCellsInDb = reportInDb.getAllowEutranCells();
        Assertions.assertEquals(1, allowedCellsInDb.size());

        AllowedEUtranCellReportTuple allowedCellInDb = allowedCellsInDb.get(0);
        Assertions.assertEquals(ROP_TIMESTAMP, allowedCellInDb.getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(CELL_FDN1, allowedCellInDb.getReportDataId().getObjectFdn());
        Assertions.assertEquals(1, allowedCellInDb.getScgData().size());

        ScgReportTuple scgInDb = allowedCellInDb.getScgData().get(0);
        Assertions.assertEquals(ROP_TIMESTAMP, scgInDb.getScgDataId().getRopTimeStamp());
        Assertions.assertEquals(CELL_FDN1, scgInDb.getScgDataId().getObjectFdn());
        Assertions.assertEquals(arfcn, scgInDb.getScgDataId().getArfcn());
        Assertions.assertEquals(0, scgInDb.getTotalCapacity());
        Assertions.assertEquals(0, scgInDb.getTotalLoad());
        Assertions.assertEquals(2, scgInDb.getSCells().size());

        List<SCellReportTuple> sCellsInDb = scgInDb.getSCells();
        Assertions.assertEquals(ROP_TIMESTAMP, sCellsInDb.get(0).getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(NR_CELL_FDN1, sCellsInDb.get(0).getReportDataId().getObjectFdn());
        Assertions.assertEquals(0, sCellsInDb.get(0).getCellCapacity());
        Assertions.assertEquals(0, sCellsInDb.get(0).getCellLoad());
        Assertions.assertEquals(ROP_TIMESTAMP, sCellsInDb.get(1).getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(NR_CELL_FDN2, sCellsInDb.get(1).getReportDataId().getObjectFdn());
        Assertions.assertEquals(0, sCellsInDb.get(1).getCellCapacity());
        Assertions.assertEquals(0, sCellsInDb.get(1).getCellLoad());

        List<CellDataReportTuple> cellDataInDb = reportInDb.getCellData();
        Assertions.assertEquals(1, cellDataInDb.size());

        CellDataReportTuple cellDbTuple = cellDataInDb.get(0);
        Assertions.assertEquals(ROP_TIMESTAMP, cellDbTuple.getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(CELL_FDN1, cellDbTuple.getReportDataId().getObjectFdn());
        Assertions.assertEquals(PROFILE_FDN_1, cellDbTuple.getOldProfileRef());
        Assertions.assertEquals(List.of(), cellDbTuple.getProfileToWrite().getGUtranFreqRef());
        Assertions.assertEquals(List.of(), cellDbTuple.getProfileToWrite().getMandatoryGUtranFreqRef());
        Assertions.assertEquals(List.of(), cellDbTuple.getProfileToWrite().getGUtranFreqDistribution());
        Assertions.assertEquals(false, cellDbTuple.getNewProfileCreated());
        Assertions.assertEquals(EndcDistrProfileDataStatus.UNCHANGED, cellDbTuple.getStatus());

        // Remove profile from cell1 to keep cell's initial data are same for all tests.
        cell1.setEndcDistrProfileRef(null);
    }

    @Test
    void createNewReportWithEmptyList() {
        reportService.createNewReport(ROP_TIMESTAMP, List.of(), Map.of(), Map.of());

        Assertions.assertEquals(0, allowListRepo.count());
        Assertions.assertEquals(0, allowEutranCellsRepo.count());
        Assertions.assertEquals(0, cellDataRepo.count());
        Assertions.assertEquals(1, reportDataRepo.count());
    }

    @Test
    void updateReportCellData() {
        List<AllowedMo> allowedMos = List.of(CELL_MO);
        NRCellCU primaryCellCu = new NRCellCU(NR_CELL_FDN1);
        List<NRCellCU> secondaryCellCus = List.of(new NRCellCU(NR_CELL_FDN2));
        Integer arfcn = 100;
        Map<String, List<SecondaryCellGroup>> cellToScgMap = Map.of(CELL_FDN1, List.of(new SecondaryCellGroup(primaryCellCu, secondaryCellCus, arfcn)));
        Map<String, EndcFreqProfileData> cellToProfileMap = Map.of(CELL_FDN1, profileData1);

        eUtranCellRepo.save(cell1);

        reportService.createNewReport(ROP_TIMESTAMP, allowedMos, cellToScgMap, cellToProfileMap);

        Report oldReportInDb = reportService.getReportByRop(ROP_TIMESTAMP);
        Assertions.assertNotNull(oldReportInDb);

        CellDataReportTuple cellDbTuple = oldReportInDb.getCellData().get(0);
        Assertions.assertEquals(ROP_TIMESTAMP, cellDbTuple.getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(CELL_FDN1, cellDbTuple.getReportDataId().getObjectFdn());
        Assertions.assertEquals("", cellDbTuple.getOldProfileRef());
        Assertions.assertEquals(gUtranFreqRef1, cellDbTuple.getProfileToWrite().getGUtranFreqRef());
        Assertions.assertEquals(mandatoryGUtranFreqRef1, cellDbTuple.getProfileToWrite().getMandatoryGUtranFreqRef());
        Assertions.assertEquals(List.of(30, 20), cellDbTuple.getProfileToWrite().getGUtranFreqDistribution());
        Assertions.assertEquals(false, cellDbTuple.getNewProfileCreated());
        Assertions.assertEquals(EndcDistrProfileDataStatus.PENDING, cellDbTuple.getStatus());

        CellDataReportTuple newCellTuple = new CellDataReportTuple(ROP_TIMESTAMP, CELL_FDN1);
        newCellTuple.setOldProfileRef(PROFILE_FDN_1);
        newCellTuple.setStatus(EndcDistrProfileDataStatus.SUCCESS);

        reportService.updateReportCellData(newCellTuple);

        Report newReportInDb = reportService.getReportByRop(ROP_TIMESTAMP);
        Assertions.assertNotNull(oldReportInDb);

        CellDataReportTuple newCellDbTuple = newReportInDb.getCellData().get(0);
        Assertions.assertEquals(ROP_TIMESTAMP, newCellDbTuple.getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(CELL_FDN1, newCellDbTuple.getReportDataId().getObjectFdn());
        Assertions.assertEquals(PROFILE_FDN_1, newCellDbTuple.getOldProfileRef());
        Assertions.assertEquals(null, newCellDbTuple.getProfileToWrite());
        Assertions.assertEquals(false, newCellDbTuple.getNewProfileCreated());
        Assertions.assertEquals(EndcDistrProfileDataStatus.SUCCESS, newCellDbTuple.getStatus());
    }

    @Test
    void getReportByRop() {
        reportService.createNewReport(ROP_TIMESTAMP, List.of(), Map.of(), Map.of());

        Report reportInDb = reportService.getReportByRop(ROP_TIMESTAMP);
        Report reportNotInDb = reportService.getReportByRop(ROP_TIMESTAMP + 1000);

        Assertions.assertNotNull(reportInDb);
        Assertions.assertNull(reportNotInDb);
    }

    private void validateAllowedMoData(long ropTimeStamp, AllowedMo origin, AllowedMoReportTuple dbTuple) {
        Assertions.assertEquals(ROP_TIMESTAMP, dbTuple.getReportDataId().getRopTimeStamp());
        Assertions.assertEquals(origin.getObjectId().toString(), dbTuple.getReportDataId().getObjectFdn());
        Assertions.assertEquals(origin.getReadOnly(), dbTuple.getReadOnly());
        Assertions.assertEquals(origin.getIsCell(), dbTuple.getIsCell());
        Assertions.assertEquals(origin.getIsTdd(), dbTuple.getIsTdd());
        Assertions.assertEquals(origin.getIsBlocked(), dbTuple.getIsBlocked());
    }
}
