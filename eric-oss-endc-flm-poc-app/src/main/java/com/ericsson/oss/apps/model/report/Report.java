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

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Report implements Serializable {
    @Serial
    private static final long serialVersionUID = 101L;

    @Id
    private long ropTimeStamp;

    @OneToMany(targetEntity = AllowedMoReportTuple.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<AllowedMoReportTuple> allowList = new ArrayList<>();

    // allowEutranCells and cellData share a same keyset - all EUtranCells.
    // Data not frequently changed are put in allowEutranCells.
    @OneToMany(targetEntity = AllowedEUtranCellReportTuple.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<AllowedEUtranCellReportTuple> allowEutranCells = new ArrayList<>();

    @OneToMany(targetEntity = CellDataReportTuple.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<CellDataReportTuple> cellData = new ArrayList<>();

    public Report(long timeStamp) {
        ropTimeStamp = timeStamp;
    }
}
