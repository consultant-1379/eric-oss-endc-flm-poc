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

import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class GUtranFreqRelation extends ManagedObject {
    @Serial
    private static final long serialVersionUID = 417727064423427105L;

    // The value component of the RDN
    @JsonProperty(value = "gUtraFreqRelationId", access = JsonProperty.Access.WRITE_ONLY)
    private String gUtraFreqRelationId;

    // Frequency priority for EN-DC measurements.
    @JsonProperty(value = "endcB1MeasPriority")
    private Integer endcB1MeasPriority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonProperty(value = "gUtranSyncSignalFrequencyRef")
    private GUtranSyncSignalFrequency gUtranSyncSignalFrequencyRef;

    // Absolute priority of concerned carrier frequency used by cell reselection procedure.
    // Value 0 means lowest priority. Value -1 means frequency is excluded.
    @JsonProperty(value = "cellReselectionPriority")
    private Integer cellReselectionPriority;

    // Prioritizes NR frequencies among all frequencies related to cell for UEs in connected mode.
    // Value 0 means lowest priority and value -1 means frequency is excluded.
    @JsonProperty(value = "connectedModeMobilityPrio")
    private Integer connectedModeMobilityPrio;

}
