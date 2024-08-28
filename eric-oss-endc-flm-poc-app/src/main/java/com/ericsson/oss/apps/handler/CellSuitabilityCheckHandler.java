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
import com.ericsson.oss.apps.model.mom.FeatureState;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmFeatureStateRepo;
import com.ericsson.oss.apps.service.CmService;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CellSuitabilityCheckHandler implements ExecutionHandler<ExecutionContext> {

    private final CmFeatureStateRepo cmFeatureStateRepo;
    private final Counter cellBlocked;
    private final Counter cellSuitabilityChecked;
    private final CmService cmService;

    @Override
    public void handle(ExecutionContext context) {

        Map<ManagedObjectId, List<FeatureState>> featureStates = cmFeatureStateRepo.findAll().stream()
                .collect(Collectors.groupingBy(featureState -> featureState.getObjectId().fetchMEId()));

        List<AllowedMo> allowList = context.getAllowList().parallelStream()
                .peek(allowedMo -> allowedMo.setIsBlocked(isFeatureBlocked(FeatureState.BIC_FEATURE, featureStates.getOrDefault(allowedMo.getObjectId().fetchMEId(), List.of()))
                        || isFeatureBlocked(FeatureState.UE_ENDC_DISTRIBUTION_FEATURE, featureStates.getOrDefault(allowedMo.getObjectId().fetchMEId(), List.of())))).toList();

        List<ManagedObjectId> allowEutranCells = cmService.getAllowedEutranCells(allowList);

        // Replace the whole allowList in context as the initial proposal.
        updateMetrics(allowList);
        context.setAllowList(allowList);
        context.setAllowEutranCells(allowEutranCells);

        log.warn("allowList size {} allowEutranCells size {}", allowList.size(), allowEutranCells.size());
    }

    private boolean isFeatureBlocked(String keyId, List<FeatureState> featureStates) {
        return featureStates.parallelStream()
                .noneMatch(featureState -> Objects.equals(featureState.getKeyId(), keyId) && featureState.isFeatureAllowed());
    }

    private void updateMetrics(List<AllowedMo> allowList) {
        // Monitor number of cells checked and blocked
        cellSuitabilityChecked.increment(allowList.size());
        cellBlocked.increment(allowList.stream().filter(AllowedMo::getIsBlocked).count());
    }

    @Override
    public int getPriority() {
        return 20;
    }
}

