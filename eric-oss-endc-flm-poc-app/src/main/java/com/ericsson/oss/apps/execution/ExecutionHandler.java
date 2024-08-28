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

public interface ExecutionHandler<T> {

    /**
     * Execute a step in the processing chain
     *
     * @param context A class used to pass results between handlers.
     */
    default void handle(T context) {}

    /**
     * The priority dictates the order of execution of the handlers.
     *
     * @return the priority, as an int
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Returning true will end the processing chain.
     *
     * @param context A class used to pass results between handlers.
     * @return true if is the last task, false if it's not.
     */
    default boolean isLast(T context) {
        return false;
    }
}
