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
import com.ericsson.oss.apps.execution.ExecutionHandler;
import com.ericsson.oss.apps.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PmDataAndReportResettingHandler implements ExecutionHandler<ExecutionContext> {

    private final ReportSCellRepo sCellRepo;
    private final ReportSecondaryCellGroupRepo scgDataRepo;
    private final ReportDataRepo reportDataRepo;
    private final PmEUtranCellRepo pmEUtranCellRepo;
    private final PmGNBDUFunctionRepo pmGNBDUFunctionRepo;
    private final PmGUtranCellRelationRepo pmGUtranCellRelationRepo;
    private final PmGUtranFreqRelationRepo pmGUtranFreqRelationRepo;
    private final PmNRCellCURepo pmNRCellCURepo;
    private final PmNRCellDURepo pmNRCellDURepo;

    // 14(days) * 24 * 60 * 60 * 1000 = 12096000000
    private static final long TWO_WEEKS_IN_MILLIS = 12096000000L;

    @Override
    public void handle(ExecutionContext executionContext) {
        log.debug("Removing pm data and report that are 2 weeks old");
        long expiredTimeStamp = executionContext.getRopTimeStamp() - TWO_WEEKS_IN_MILLIS;

        // deleteAllInBatch will not work with cascade relationship, so deleteAll() is used
        reportDataRepo.deleteAll(reportDataRepo.findByRopTimeLessThanEqual(expiredTimeStamp));
        sCellRepo.deleteAll(sCellRepo.findByRopTimeLessThanEqual(expiredTimeStamp));

        pmEUtranCellRepo.deleteAllInBatch(pmEUtranCellRepo.findByRopTimeLessThanEqual(expiredTimeStamp));
        pmGNBDUFunctionRepo.deleteAllInBatch(pmGNBDUFunctionRepo.findByRopTimeLessThanEqual(expiredTimeStamp));
        pmGUtranCellRelationRepo.deleteAllInBatch(pmGUtranCellRelationRepo.findByRopTimeLessThanEqual(expiredTimeStamp));
        pmGUtranFreqRelationRepo.deleteAllInBatch(pmGUtranFreqRelationRepo.findByRopTimeLessThanEqual(expiredTimeStamp));
        pmNRCellCURepo.deleteAllInBatch(pmNRCellCURepo.findByRopTimeLessThanEqual(expiredTimeStamp));
        pmNRCellDURepo.deleteAllInBatch(pmNRCellDURepo.findByRopTimeLessThanEqual(expiredTimeStamp));
    }


    @Override
    public int getPriority() {
        return 70;
    }
}
