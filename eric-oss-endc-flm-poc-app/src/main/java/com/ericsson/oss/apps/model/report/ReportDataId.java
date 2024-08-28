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
import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class ReportDataId implements Serializable {
    @Serial
    private static final long serialVersionUID = 101L;

    private long ropTimeStamp;
    private String objectFdn;

    public ReportDataId(long timeStamp, String fdn) {
        ropTimeStamp = timeStamp;
        objectFdn = fdn;
    }

    @Override
    public String toString() {
        return ropTimeStamp + "_" + objectFdn;
    }
}
