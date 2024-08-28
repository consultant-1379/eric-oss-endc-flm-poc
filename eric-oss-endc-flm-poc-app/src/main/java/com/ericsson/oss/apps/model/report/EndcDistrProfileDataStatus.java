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
package com.ericsson.oss.apps.model.report;

public enum EndcDistrProfileDataStatus {
    UNCHANGED,
    PENDING,
    SUCCESS,
    FAILED_AT_CREATING_PROFILE,
    FAILED_AT_MODIFY_PROFILE,
    FAILED_AT_MODIFY_CELL,
    ABORTED_DATA_ERROR
}
