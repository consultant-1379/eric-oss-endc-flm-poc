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

import org.springframework.stereotype.Component;

import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.execution.ExecutionHandler;
import com.ericsson.oss.apps.service.ReportService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InitialReportHandler implements ExecutionHandler<ExecutionContext> {
    private final ReportService reportService;

    @Override
    public void handle(ExecutionContext executionContext) {
        reportService.createNewReport(executionContext.getRopTimeStamp(), executionContext.getAllowList(),
                executionContext.getEUtranCellToSCGsMap(), executionContext.getEUtranCellToProfileData());
    }

    @Override
    public int getPriority() {
        return 48;
    }
}
