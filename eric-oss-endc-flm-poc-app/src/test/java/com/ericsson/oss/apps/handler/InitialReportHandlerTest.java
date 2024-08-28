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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.service.ReportService;

@ExtendWith(MockitoExtension.class)
public class InitialReportHandlerTest {
    private ExecutionContext context;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private InitialReportHandler handler;

    private final long ROP_TIMESTAMP = 1234L;

    @Test
    void setupInitialReport() {
        context = new ExecutionContext(ROP_TIMESTAMP);
        handler.handle(context);
        verify(reportService, times(1)).createNewReport(ROP_TIMESTAMP, List.of(), Map.of(), Map.of());
    }
}
