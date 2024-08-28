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
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SubscriptionHandler implements ExecutionHandler<ExecutionContext> {

    private final SubscriptionService subscriptionService;

    public static final String NR_SUBSCRIPTION_NAME = "5g-pm-events";
    public static final String LTE_SUBSCRIPTION_NAME = "lte-pm-events";
    public static final String NODE_PREDICATE_NAME = "nodeName";

    @Override
    public void handle(ExecutionContext context) {

        if (context.getAllowList().isEmpty()) {
            subscriptionService.blankDccSubscription(NR_SUBSCRIPTION_NAME);
            subscriptionService.blankDccSubscription(LTE_SUBSCRIPTION_NAME);
        } else {
            Map<String, List<String>> nrPredicates = getNRPredicates(context.getEUtranCellToSCGsMap().values().stream().flatMap(Collection::stream).toList());
            Map<String, List<String>> ltePredicates = getLTEPredicates(context.getAllowList());

            subscriptionService.patchDccSubscription(
                    NR_SUBSCRIPTION_NAME,
                    nrPredicates);
            subscriptionService.patchDccSubscription(
                    LTE_SUBSCRIPTION_NAME,
                    ltePredicates);
            subscriptionService.saveSubscription(context.getRopTimeStamp(), nrPredicates.get(NODE_PREDICATE_NAME), ltePredicates.get(NODE_PREDICATE_NAME));
        }
    }

    private Map<String, List<String>> getLTEPredicates(List<AllowedMo> allowedMos) {

        if (allowedMos.isEmpty()) {
            return Map.of(NODE_PREDICATE_NAME, List.of());
        }

        return Map.of(NODE_PREDICATE_NAME, Stream.concat(
                        allowedMos.stream()
                                .filter(allowedMo -> !allowedMo.getIsBlocked())
                                .filter(allowedMo -> !allowedMo.getIsCell())
                                .map(allowedMo -> allowedMo.getObjectId().toString()),
                        allowedMos.stream()
                                .filter(allowedMo -> !allowedMo.getIsBlocked())
                                .filter(AllowedMo::getIsCell)
                                .map(allowedMo -> allowedMo.getObjectId().fetchParentFdn()))
                .distinct().toList());
    }

    private Map<String, List<String>> getNRPredicates(List<SecondaryCellGroup> secondaryCellGroup) {

        if (secondaryCellGroup.isEmpty()) {
            return Map.of(NODE_PREDICATE_NAME, List.of());
        }

        return Map.of(NODE_PREDICATE_NAME, secondaryCellGroup.stream()
                .flatMap(scg -> Stream.concat(Stream.of(scg.primaryNRCell()), scg.secondaryCells().stream()))
                .map(nrCell -> nrCell.getObjectId().fetchParentId())
                .map(ManagedObjectId::toString)
                .distinct()
                .toList());
    }

    @Override
    public int getPriority() {
        return 22;
    }
}
