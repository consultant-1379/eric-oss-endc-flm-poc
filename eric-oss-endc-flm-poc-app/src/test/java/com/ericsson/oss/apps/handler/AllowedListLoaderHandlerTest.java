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

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.service.CmService;

@ExtendWith(MockitoExtension.class)
public class AllowedListLoaderHandlerTest {
    ExecutionContext context;

    @Mock
    CmService mockedCmService;

    @InjectMocks
    AllowedListLoaderHandler handler;

    private static final ManagedObjectId CELL_RESOURCE = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,ENodeBFunction=1,EUtranCellFDD=LTE31dg2ERBS00035-1");
    private static final ManagedObjectId ENB_RESOURCE = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,ENodeBFunction=2");
    private static final ManagedObjectId ANOTHER_ME = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=AnotherME,ENodeBFunction=3");

    private static final AllowedMo CELL_MO = new AllowedMo(CELL_RESOURCE, true, true, false);
    private static final AllowedMo ENB_MO = new AllowedMo(ENB_RESOURCE, true, false, false);
    private static final AllowedMo ANOTHER_ME_MO = new AllowedMo(ANOTHER_ME, true, false, false);

    private static final List<AllowedMo> REPO_DATA = List.of(CELL_MO, ENB_MO, ANOTHER_ME_MO);

    @ParameterizedTest
    @MethodSource("listProvider")
    void loadAllowedList(List<AllowedMo> list) {
        context = new ExecutionContext(1234L);
        Mockito.when(mockedCmService.loadCmAllowListRepo()).thenReturn(list);

        Assertions.assertTrue(context.getAllowList().isEmpty());
        handler.handle(context);
        Assertions.assertEquals(list, context.getAllowList());
    }

    static Stream<List<AllowedMo>> listProvider() {
        return Stream.of(REPO_DATA, List.of());
    }
}
