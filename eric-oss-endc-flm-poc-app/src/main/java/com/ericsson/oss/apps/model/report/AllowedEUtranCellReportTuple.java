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
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class AllowedEUtranCellReportTuple extends ReportDataBase {
    @Serial
    private static final long serialVersionUID = 101L;

    // Store SCG per frequency (ARFCN)
    @OneToMany(targetEntity = ScgReportTuple.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<ScgReportTuple> scgData = new ArrayList<>();

    public AllowedEUtranCellReportTuple(long timeStamp, String fdn) {
        super(timeStamp, fdn);
    }
}
