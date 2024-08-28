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
package com.ericsson.oss.apps.model.mom;

public enum AvailStatus {
    IN_TEST,
    FAILED,
    POWER_OFF,
    OFF_LINE,
    OFF_DUTY,
    DEPENDENCY,
    DEGRADED,
    NOT_INSTALLED,
    LOG_FULL,
    DEPENDENCY_LOCKED,
    DEPENDENCY_FAILED,
    DEPENDENCY_SHUTTINGDOWN,
    DEPENDENCY_RECOVERY,
    DEPENDENCY_HOLDING;
}
