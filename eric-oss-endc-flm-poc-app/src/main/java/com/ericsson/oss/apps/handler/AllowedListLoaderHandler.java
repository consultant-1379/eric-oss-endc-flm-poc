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
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.service.CmService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AllowedListLoaderHandler implements ExecutionHandler<ExecutionContext> {
    private final CmService cmService;

    @Override
    public void handle(ExecutionContext context) {
        // Load all data from CmAllowListRepo as a snapshot for one execution loop.
        List<AllowedMo> allowList = cmService.loadCmAllowListRepo();
        if (allowList.isEmpty()) {
            log.warn("AllowListLoader: No data found in allowList repository.");
        }

        context.setAllowList(allowList);
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
