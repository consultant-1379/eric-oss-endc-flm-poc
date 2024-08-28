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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;


@Slf4j
@SpringBootTest(properties = {"app.data.pm.rop.scheduler.cron=* * * * * *"})
class ExecutionSchedulerTest {

    @SpyBean
    private ExecutionScheduler executionScheduler;

    @MockBean
    private ExecutionChain executionChain;

    @Test
    void scheduleIsTriggered() {
        await().atMost(Duration.of(2000, ChronoUnit.MILLIS))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> verify(executionScheduler, atLeast(1)).triggerExecutionChain());
    }

}