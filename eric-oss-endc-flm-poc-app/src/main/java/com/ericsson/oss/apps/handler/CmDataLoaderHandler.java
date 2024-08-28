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
import com.ericsson.oss.apps.loader.CmDataLoader;
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmDataLoaderHandler implements ExecutionHandler<ExecutionContext> {
    private final CmDataLoader cmDataLoader;

    @Override
    public void handle(ExecutionContext executionContext) {

        List<ManagedObjectId> allowList = executionContext.getAllowList().stream()
                .filter(AllowedMo::getIsCell)
                .map(AllowedMo::getObjectId)
                .collect(Collectors.groupingBy(ManagedObjectId::fetchMEId))
                .values().stream()
                .flatMap(list -> list.stream().findFirst().stream())
                .toList();

        if (allowList.isEmpty()) {
            log.warn("CmLoader: allowedNodes list is empty");
            return;
        }
        cmDataLoader.extractCmInfo(allowList);
    }

    @Override
    public int getPriority() {
        return 10;
    }

}
