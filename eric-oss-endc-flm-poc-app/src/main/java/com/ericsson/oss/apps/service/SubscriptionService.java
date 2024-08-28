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

package com.ericsson.oss.apps.service;

import com.ericsson.oss.apps.dcc.DccClient;
import com.ericsson.oss.apps.dcc.model.IDS;
import com.ericsson.oss.apps.model.entities.Subscription;
import com.ericsson.oss.apps.repository.CmSubscriptionRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class SubscriptionService {

    private final DccClient subscriptionClient;
    private final CmSubscriptionRepo cmSubscriptionRepo;
    private final ObjectMapper mapper;

    @EventListener(value = ApplicationReadyEvent.class,
            condition = "@environment.getProperty('rapp-sdk.dcc.test-enabled') == 'false'")
    @Retryable(backoff = @Backoff(delay = 1000))
    public boolean createIds() {
        boolean result = false;
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("ids/ids.json")) {
            IDS ids = mapper.readValue(inputStream, IDS.class);
            result = subscriptionClient.createIds(ids.getVersion(), ids.getSubscriptions());
            if (result) {
                log.info("IDS creation successful");
            }
            else {
                log.warn("IDS creation failed");
            }

        } catch (IOException e) {
            log.error("Cannot create IDS, cannot read IDS specs", e);
        }
        return result;
    }

    @PreDestroy
    @Retryable(backoff = @Backoff(delay = 1000))
    public boolean deleteIds() {
        boolean result = subscriptionClient.deleteIds();
        if (result) {
            log.info("IDS deletion successful");
        }
        else {
            log.warn("IDS deletion failed");
        }
        return result;
    }

    @Retryable(backoff = @Backoff(delay = 1000))
    public boolean patchDccSubscription(String name, Map<String, List<String>> predicates) {
        log.info("Subscribing to {} with {}", name, predicates);
        boolean result = subscriptionClient.patchIdsSubscription(name, predicates);
        if (result) {
            log.info("Subscription successful");
        }
        else {
            log.warn("Subscription failed");
        }
        return result;
    }

    @Retryable(backoff = @Backoff(delay = 1000))
    public boolean blankDccSubscription(String name) {
        log.info("Removing Subscription to {}", name);
        boolean result = subscriptionClient.blankIdsSubscription(name);
        if (result) {
            log.info("Unsubscription successful");
        }
        else {
            log.warn("Unsubscription failed");
        }
        cmSubscriptionRepo.deleteAll();
        return result;
    }

    @Transactional
    public void saveSubscription(long ropTime, List<String> nrNodeList, List<String> eNodebList) {
        log.info("Creating Subscribing into CM, eNodebList size {}, nrNodeList size {}", eNodebList.size(), nrNodeList.size());
        cmSubscriptionRepo.deleteAll();
        Subscription subscription = new Subscription(ropTime);
        subscription.setENodebList(eNodebList);
        subscription.setNrNodeList(nrNodeList);
        cmSubscriptionRepo.save(subscription);
    }
}