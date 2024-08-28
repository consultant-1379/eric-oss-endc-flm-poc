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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class SubscriptionServiceTest {

    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private CmSubscriptionRepo cmSubscriptionRepo;
    @MockBean
    private DccClient subscriptionClient;

    public static final String SUB_NAME = "name";

    ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @CsvSource(value = {
            "true", "false"
    })
    void createIds(boolean dccResult) throws IOException {
        //TODO: Check these tests are correct
        IDS ids = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("ids/ids.json"), IDS.class);
        when(subscriptionClient.createIds(ids.getVersion(), ids.getSubscriptions())).thenReturn(dccResult);
        subscriptionService.createIds();
        verify(subscriptionClient, times(1)).createIds(ids.getVersion(), ids.getSubscriptions());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true", "false"
    })
    void deleteIds(boolean dccResult) {
        when(subscriptionClient.deleteIds()).thenReturn(dccResult);
        subscriptionService.deleteIds();
        verify(subscriptionClient, times(1)).deleteIds();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true", "false"
    })
    void patchSubscription(boolean dccResult) {
        when(subscriptionClient.patchIdsSubscription(SUB_NAME, Map.of("key", List.of("value")))).thenReturn(dccResult);
        assertEquals(dccResult, subscriptionService.patchDccSubscription(SUB_NAME, Map.of("key", List.of("value"))));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true", "false"
    })
    void blankSubscription(boolean dccResult) {
        when(subscriptionClient.blankIdsSubscription(SUB_NAME)).thenReturn(dccResult);
        assertEquals(dccResult, subscriptionService.blankDccSubscription(SUB_NAME));
    }

    @Test
    void saveSubscription() {
        List<String> nrNodeList = List.of("nrNode1", "nrNode2");
        List<String> eNodebList = List.of(
                "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1");
        subscriptionService.saveSubscription(123L, nrNodeList, eNodebList);
        List<Subscription> subscription = cmSubscriptionRepo.findAll();
        assertEquals(1, subscription.size());
        assertEquals(1, subscription.get(0).getENodebList().size());
        assertEquals(2, subscription.get(0).getNrNodeList().size());
        assertEquals(123L, subscription.get(0).getRopTime());
    }
}