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
package com.ericsson.oss.apps.handler;

import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.model.pmrop.*;
import com.ericsson.oss.apps.model.report.*;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PmDataAndReportResettingHandlerTest {

    @Autowired
    private ReportDataRepo reportDataRepo;
    @Autowired
    private PmEUtranCellRepo pmEUtranCellRepo;
    @Autowired
    private PmGNBDUFunctionRepo pmGNBDUFunctionRepo;
    @Autowired
    private PmGUtranCellRelationRepo pmGUtranCellRelationRepo;
    @Autowired
    private PmGUtranFreqRelationRepo pmGUtranFreqRelationRepo;
    @Autowired
    private PmNRCellCURepo pmNRCellCURepo;
    @Autowired
    private PmNRCellDURepo pmNRCellDURepo;
    @Autowired
    private PmDataAndReportResettingHandler handler;

    @Autowired
    private ReportAllowListRepo allowListRepo;
    @Autowired
    private ReportAllowEUtranCellsRepo allowEutranCellsRepo;
    @Autowired
    private ReportCellDataRepo cellDataRepo;
    @Autowired
    private ReportSecondaryCellGroupRepo scgDataRepo;
    @Autowired
    private ReportSCellRepo sCellRepo;

    private static final long TWO_WEEKS_IN_MILLIS = 12096000000L;
    private static final long CURRENT_TIME = System.currentTimeMillis();
    private static final ManagedObjectId OBJECT_ID = ManagedObjectId.of("test");

    @Test
    void runHandler() {
        addDataToRepoBaseTimeStamp(CURRENT_TIME);
        ExecutionContext context = new ExecutionContext(CURRENT_TIME);

        handler.handle(context);
        checkReposSize(1);

        addDataToRepoBaseTimeStamp(CURRENT_TIME - TWO_WEEKS_IN_MILLIS);
        checkReposSize(2);

        handler.handle(context);
        checkReposSize(1);
    }

    private void checkReposSize(int size) {

        Assertions.assertEquals(size * 4L, sCellRepo.count());
        Assertions.assertEquals(size * 3L, scgDataRepo.count());
        Assertions.assertEquals(size, allowListRepo.count());
        Assertions.assertEquals(size, allowEutranCellsRepo.count());
        Assertions.assertEquals(size, cellDataRepo.count());

        Assertions.assertEquals(size, reportDataRepo.count());

        Assertions.assertEquals(size, pmEUtranCellRepo.count());
        Assertions.assertEquals(size, pmGNBDUFunctionRepo.count());
        Assertions.assertEquals(size, pmGUtranCellRelationRepo.count());
        Assertions.assertEquals(size, pmGUtranFreqRelationRepo.count());
        Assertions.assertEquals(size, pmNRCellCURepo.count());
        Assertions.assertEquals(size, pmNRCellDURepo.count());
    }

    @Transactional
    private void addDataToRepoBaseTimeStamp(long timeStamp) {
        SCellReportTuple sCellReportTuple_1 = new SCellReportTuple(timeStamp, "sCell_1", 3000F, 0.5F);
        SCellReportTuple sCellReportTuple_2 = new SCellReportTuple(timeStamp, "sCell_2", 4000F, 0.6F);
        SCellReportTuple sCellReportTuple_3 = new SCellReportTuple(timeStamp, "sCell_3", 5000F, 0.7F);
        SCellReportTuple sCellReportTuple_4 = new SCellReportTuple(timeStamp, "sCell_4", 5000F, 0.7F);
        sCellRepo.saveAll(List.of(sCellReportTuple_1, sCellReportTuple_2, sCellReportTuple_3, sCellReportTuple_4));

        ScgReportTuple scgReportTuple_1 = new ScgReportTuple(timeStamp, "psCell_1", 3000);
        scgReportTuple_1.setSCells(List.of(sCellReportTuple_1, sCellReportTuple_2, sCellReportTuple_3));
        ScgReportTuple scgReportTuple_2 = new ScgReportTuple(timeStamp, "psCell_2", 4000);
        scgReportTuple_2.setSCells(List.of(sCellReportTuple_2, sCellReportTuple_1, sCellReportTuple_3));
        ScgReportTuple scgReportTuple_3 = new ScgReportTuple(timeStamp, "psCell_3", 4000);
        scgReportTuple_3.setSCells(List.of(sCellReportTuple_4));
        scgDataRepo.saveAll(List.of(scgReportTuple_1, scgReportTuple_2, scgReportTuple_3));

        AllowedMoReportTuple allowedMoReportTuple = new AllowedMoReportTuple(timeStamp, "test");
        AllowedEUtranCellReportTuple allowedEUtranCellReportTuple = new AllowedEUtranCellReportTuple(timeStamp, "test");
        allowedEUtranCellReportTuple.setScgData(List.of(scgReportTuple_1, scgReportTuple_2, scgReportTuple_3));
        CellDataReportTuple cellDataReportTuple = new CellDataReportTuple(timeStamp, "test");
        allowListRepo.save(allowedMoReportTuple);
        allowEutranCellsRepo.save(allowedEUtranCellReportTuple);
        cellDataRepo.save(cellDataReportTuple);

        Report report = new Report(timeStamp);
        report.setAllowList(List.of(allowedMoReportTuple));
        report.setAllowEutranCells(List.of(allowedEUtranCellReportTuple));
        report.setCellData(List.of(cellDataReportTuple));
        reportDataRepo.save(report);

        MoRopId moRopId = new MoRopId(OBJECT_ID, timeStamp);

        PmRopEUtranCell pmRopEUtranCell = new PmRopEUtranCell();
        pmRopEUtranCell.setMoRopId(moRopId);
        pmEUtranCellRepo.save(pmRopEUtranCell);

        PmRopGNBDUFunction pmRopGNBDUFunction = new PmRopGNBDUFunction();
        pmRopGNBDUFunction.setMoRopId(moRopId);
        pmGNBDUFunctionRepo.save(pmRopGNBDUFunction);

        PmRopGUtranCellRelation pmRopGUtranCellRelation = new PmRopGUtranCellRelation();
        pmRopGUtranCellRelation.setMoRopId(moRopId);
        pmGUtranCellRelationRepo.save(pmRopGUtranCellRelation);

        PmRopGUtranFreqRelation pmRopGUtranFreqRelation = new PmRopGUtranFreqRelation();
        pmRopGUtranFreqRelation.setMoRopId(moRopId);
        pmGUtranFreqRelationRepo.save(pmRopGUtranFreqRelation);

        PmRopNRCellCU pmRopNRCellCU = new PmRopNRCellCU();
        pmRopNRCellCU.setMoRopId(moRopId);
        pmNRCellCURepo.save(pmRopNRCellCU);

        PmRopNRCellDU pmRopNRCellDU = new PmRopNRCellDU();
        pmRopNRCellDU.setMoRopId(moRopId);
        pmNRCellDURepo.save(pmRopNRCellDU);
    }
}
