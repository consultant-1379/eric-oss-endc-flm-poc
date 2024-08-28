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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExecutionScheduler {

    @Value("${app.data.pm.rop.millis}")
    private int ropMillis = 900000;

    @Autowired
    ExecutionChain executionChain;

    @Scheduled(cron = "${app.data.pm.rop.scheduler.cron}")
    public void triggerExecutionChain() {
        long now = System.currentTimeMillis();
        long ropTimestamp = (now - now % ropMillis);
        executionChain.execute(ropTimestamp);
    }
}


