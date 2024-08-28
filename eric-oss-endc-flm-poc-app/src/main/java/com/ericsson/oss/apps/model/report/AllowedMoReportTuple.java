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

import java.io.Serial;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class AllowedMoReportTuple extends ReportDataBase {
    @Serial
    private static final long serialVersionUID = 101L;

    // Are below information needed in report?
    private Boolean readOnly;
    private Boolean isCell;
    private Boolean isTdd;
    private Boolean isBlocked;

    public AllowedMoReportTuple(long timeStamp, String fdn) {
        super(timeStamp, fdn);
    }
}
