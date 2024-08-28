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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExecutionChainTest {

    public static final long ROP_TIME_STAMP = 0L;

    @Spy
    ExecutionHandler<ExecutionContext> handler1 = new ExecutionHandler<>() {
        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public boolean isLast(ExecutionContext ignored) {
            return true;
        }
    };

    @Spy
    ExecutionHandler<ExecutionContext> handler2 = new ExecutionHandler<>() {
    };

    @Spy
    ExecutionHandler<ExecutionContext> handler3 = new ExecutionHandler<>() {
        @Override
        public int getPriority() {
            return 2;
        }
    };

    @Test
    void executeChain() {
        List<ExecutionHandler<ExecutionContext>> handlerList = new ArrayList<>(List.of(handler2, handler1));
        ExecutionChain executionChain = new ExecutionChain(handlerList);
        executionChain.setup();
        executionChain.execute(ROP_TIME_STAMP);
        handlerList.forEach(handler -> verify(handler, times(1)).handle(any(ExecutionContext.class)));
    }

    @Test
    void executeChainBreak() {
        List<ExecutionHandler<ExecutionContext>> handlerList = new ArrayList<>(List.of(handler3, handler1));
        ExecutionChain executionChain = new ExecutionChain(handlerList);
        executionChain.setup();
        executionChain.execute(ROP_TIME_STAMP);

        // The handlerList is sorted as [handler1, handler3], and the executionChain breaks at handler1.
        verify(handler1, times(1)).handle(any(ExecutionContext.class));
        verify(handler3, times(0)).handle(any(ExecutionContext.class));
    }
}
