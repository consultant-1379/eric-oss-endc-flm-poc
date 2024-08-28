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
package com.ericsson.oss.apps.execution;

import com.ericsson.oss.apps.handler.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ExecutionChainItNormalExecTest extends ExecutionChainItTest {

    @Override
    List<Class<? extends ExecutionHandler<ExecutionContext>>> getExpectedHandlerList() {
        return List.of(
                AllowedListLoaderHandler.class,
                CmDataResettingHandler.class,
                CmDataLoaderHandler.class,
                CellFilterHandler.class,
                CellSuitabilityCheckHandler.class,
                SubscriptionHandler.class,
                CalcEndcDistrProfileHandler.class,
                InitialReportHandler.class,
                EndcDistrProfileHandler.class,
                PmDataAndReportResettingHandler.class
        );
    }

    @Test
    void testHandlersNormalExec() {
        testHandlers();
    }
}
