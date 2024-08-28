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

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
abstract class ReportDataBase implements Serializable {
    @Serial
    private static final long serialVersionUID = 101L;

    @EmbeddedId
    @EqualsAndHashCode.Include
    private ReportDataId reportDataId;

    public ReportDataBase(long timeStamp, String fdn) {
        reportDataId = new ReportDataId(timeStamp, fdn);
    }
}
