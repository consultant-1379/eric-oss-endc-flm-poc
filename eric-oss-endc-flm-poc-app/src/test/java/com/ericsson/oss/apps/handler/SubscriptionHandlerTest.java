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

import com.ericsson.oss.apps.dcc.DccClient;
import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.model.mom.NRCellCU;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SubscriptionHandlerTest {

    ExecutionContext context;

    @Mock
    SubscriptionService mockedSubscriptionService;

    @Mock
    DccClient subscriptionClient;
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    SubscriptionHandler handler;

    public static final String NR_SUBSCRIPTION_NAME = "5g-pm-events";
    public static final String LTE_SUBSCRIPTION_NAME = "lte-pm-events";
    public static final String NODE_PREDICATE_NAME = "nodeName";

    private static final String FDD_FDN = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00045,ENodeBFunction=1,EUtranCellFDD=LTE31dg2ERBS00035-1";
    private static final String TDD_FDN = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00045,ENodeBFunction=1,EUtranCellTDD=LTE31dg2ERBS00045-2";
    private static final String ENODEB = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00045,ENodeBFunction=2";
    private static final ManagedObjectId FDD_CELL_RESOURCE = ManagedObjectId.of(FDD_FDN);
    private static final ManagedObjectId TDD_CELL_RESOURCE = ManagedObjectId.of(TDD_FDN);
    private static final ManagedObjectId ENODEB_RESOURCE = ManagedObjectId.of(ENODEB);
    private static final List<AllowedMo> allowedList = List.of(
            new AllowedMo(FDD_CELL_RESOURCE, true, true, false, false),
            new AllowedMo(TDD_CELL_RESOURCE, true, true, true, false),
            new AllowedMo(ENODEB_RESOURCE, true, false, false, false));
    private static final List<AllowedMo> allowedListBlocked = List.of(
            new AllowedMo(FDD_CELL_RESOURCE, true, true, false, true),
            new AllowedMo(TDD_CELL_RESOURCE, true, true, true, false));

    // Since FDD_FDN and TDD_FDN are belong to the same LTE Node, they are considered as the same
    private static final Map<String, List<String>> predicatesLTE = Map.of(NODE_PREDICATE_NAME, List.of(ENODEB, FDD_CELL_RESOURCE.fetchParentFdn()));
    private static final Map<String, List<String>> predicatesLTEBlocked = Map.of(NODE_PREDICATE_NAME, List.of(TDD_CELL_RESOURCE.fetchParentFdn()));


    private static final String NR_PRIMARY_CELL = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,ENodeBFunction=1,NRCELLCU=NR03gNodeBRadio00002-1";
    private static final String NR_SECONDARY_CELL = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,ENodeBFunction=1,NRCELLCU=NR03gNodeBRadio00002-2";

    private static final NRCellCU PRIMARY_NR_CELL = new NRCellCU(NR_PRIMARY_CELL);
    private static final List<NRCellCU> SECONDARY_NR_CELLS = List.of(new NRCellCU(NR_SECONDARY_CELL));
    private static final Map<String, List<SecondaryCellGroup>> EUTRAN_CELL_TO_SCG_MAP =
            Map.of(FDD_FDN, List.of(new SecondaryCellGroup(PRIMARY_NR_CELL, SECONDARY_NR_CELLS, 2)));
    // Since both nrCellCu are belong to the same NR Node, they are considered as the same
    private static final Map<String, List<String>> predicatesNR = Map.of(NODE_PREDICATE_NAME, List.of(PRIMARY_NR_CELL.getObjectId().fetchParentFdn()));

    @Test
    void noAllowList() {

        ExecutionContext context = new ExecutionContext(1234L);

        // Clear Allowed List
        List<AllowedMo> allowedList = List.of();
        context.setAllowList(allowedList);
        handler.handle(context);

        Mockito.verify(mockedSubscriptionService, Mockito.times(1)).blankDccSubscription(NR_SUBSCRIPTION_NAME);
        Mockito.verify(mockedSubscriptionService, Mockito.times(1)).blankDccSubscription(LTE_SUBSCRIPTION_NAME);
    }

    @Test
    void verifyAllowList() {

        ExecutionContext context = new ExecutionContext(1234L);

        context.setAllowList(allowedList);
        handler.handle(context);
        //verify LTE subscription with no NR Relations
        ArgumentCaptor<String> subscriptionNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, List<String>>> predicateCaptor = ArgumentCaptor.forClass(Map.class);

        Mockito.verify(mockedSubscriptionService, Mockito.times(2)).patchDccSubscription(subscriptionNameCaptor.capture(), predicateCaptor.capture());
        Mockito.verify(mockedSubscriptionService, Mockito.times(1)).saveSubscription(1234L, List.of(), predicatesLTE.get(NODE_PREDICATE_NAME));

        List<String> capturedSubscriptionNames = subscriptionNameCaptor.getAllValues();
        List<Map<String, List<String>>> capturedPredicates = predicateCaptor.getAllValues();

        assertEquals(List.of(NR_SUBSCRIPTION_NAME, LTE_SUBSCRIPTION_NAME), capturedSubscriptionNames);
        assertEquals(List.of(Map.of(NODE_PREDICATE_NAME, List.of()), predicatesLTE), capturedPredicates);
    }

    @Test
    void verifySecondaryList() {

        ExecutionContext context = new ExecutionContext(1234L);

        context.setAllowList(allowedList);
        context.setEUtranCellToSCGsMap(EUTRAN_CELL_TO_SCG_MAP);
        handler.handle(context);
        ArgumentCaptor<String> subscriptionNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, List<String>>> predicateCaptor = ArgumentCaptor.forClass(Map.class);

        Mockito.verify(mockedSubscriptionService, Mockito.times(2)).patchDccSubscription(subscriptionNameCaptor.capture(), predicateCaptor.capture());
        Mockito.verify(mockedSubscriptionService, Mockito.times(1)).saveSubscription(1234L, predicatesNR.get(NODE_PREDICATE_NAME), predicatesLTE.get(NODE_PREDICATE_NAME));

        List<String> capturedSubscriptionNames = subscriptionNameCaptor.getAllValues();
        List<Map<String, List<String>>> capturedPredicates = predicateCaptor.getAllValues();

        assertEquals(List.of(NR_SUBSCRIPTION_NAME, LTE_SUBSCRIPTION_NAME), capturedSubscriptionNames);
        assertEquals(List.of(predicatesNR, predicatesLTE), capturedPredicates);
    }

    @Test
    void verifyBlockedCell() {

        ExecutionContext context = new ExecutionContext(1234L);
        context.setAllowList(allowedListBlocked);

        handler.handle(context);
        ArgumentCaptor<String> subscriptionNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, List<String>>> predicateCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(mockedSubscriptionService, Mockito.times(2)).patchDccSubscription(subscriptionNameCaptor.capture(), predicateCaptor.capture());

        List<String> capturedSubscriptionNames = subscriptionNameCaptor.getAllValues();
        List<Map<String, List<String>>> capturedPredicates = predicateCaptor.getAllValues();

        assertEquals(List.of(NR_SUBSCRIPTION_NAME, LTE_SUBSCRIPTION_NAME), capturedSubscriptionNames);
        assertEquals(List.of(Map.of(NODE_PREDICATE_NAME, List.of()), predicatesLTEBlocked), capturedPredicates);
    }
}
