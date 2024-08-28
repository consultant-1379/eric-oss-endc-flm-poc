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
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ExternalNRCellCU extends ManagedObject {

    private static final long serialVersionUID = 6407255084855706649L;

    @JsonProperty(value = "externalNRCellCUId", access = JsonProperty.Access.WRITE_ONLY)
    private String externalNRCellCUId;

    // Used together with gNodeB identifier to identify NR cell in PLMN. Used together with gNBId to form NCI.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer cellLocalId;
    public ExternalNRCellCU(String fdn) {
        super(fdn);
    }
}
