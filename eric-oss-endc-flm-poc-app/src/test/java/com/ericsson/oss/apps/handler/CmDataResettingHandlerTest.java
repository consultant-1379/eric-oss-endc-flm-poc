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
import com.ericsson.oss.apps.service.CmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CmDataResettingHandlerTest {

    @InjectMocks
    private CmDataResettingHandler handler;
    @Mock
    private CmService cmService;

    @Test
    void cleanCmDateTest() {
        long ROP_TIMESTAMP = 1234L;
        ExecutionContext context = new ExecutionContext(ROP_TIMESTAMP);
        handler.handle(context);
        Mockito.verify(cmService, Mockito.times(1)).cleanCmData();
    }
}
