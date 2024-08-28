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

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * This class is used to run all the steps (handlers) of the algorithm execution.
 * The handlers are discovered by spring and run in order of priority
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionChain {

    private final List<ExecutionHandler<ExecutionContext>> executionHandlers;

    @PostConstruct
    void setup() {
        executionHandlers.sort(Comparator.comparingInt(ExecutionHandler::getPriority));
    }

    public void execute(long ropTimestamp) {
        log.info("Started algorithm execution for ROP {}", ropTimestamp);

        ExecutionContext executionContext = new ExecutionContext(ropTimestamp);
        for (ExecutionHandler<ExecutionContext> handler : executionHandlers) {
            handler.handle(executionContext);
            if (handler.isLast(executionContext)) {
                log.warn("Execution chain terminates at handler {} in ROP {}", handler.getClass().getSimpleName(), ropTimestamp);
                break;
            }
        }
        log.info("Finished algorithm execution for ROP {}", ropTimestamp);
    }
}
