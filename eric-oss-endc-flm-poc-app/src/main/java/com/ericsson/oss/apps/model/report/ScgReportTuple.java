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
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ScgReportTuple implements Serializable {
    // Note this class is not derived from ReportDataBase
    @Serial
    private static final long serialVersionUID = 101L;

    @EmbeddedId
    @EqualsAndHashCode.Include
    private ScgDataId scgDataId;

    private Float totalCapacity = 0F;
    private Float totalLoad = 0F;

    @ManyToMany(targetEntity = SCellReportTuple.class, fetch = FetchType.EAGER)
    private List<SCellReportTuple> sCells = new ArrayList<>();

    public ScgReportTuple(long ropTimeStamp, String pCellFdn, Integer arfcn) {
        scgDataId = new ScgDataId(ropTimeStamp, pCellFdn, arfcn);
    }
}
