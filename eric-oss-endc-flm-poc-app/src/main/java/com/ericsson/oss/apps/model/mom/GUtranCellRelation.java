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
import lombok.*;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class GUtranCellRelation extends ManagedObject {
    @Serial
    private static final long serialVersionUID = 417727064423427105L;

    // The value component of the RDN
    @JsonProperty(value = "gUtraCellRelationId", access = JsonProperty.Access.WRITE_ONLY)
    private String gUtraCellRelationId;

    // Whether handover between cells in this relation is allowed.
    @JsonProperty(value = "isHoAllowed")
    private Boolean isHoAllowed;

    // Whether ANR function is allowed to remove this object. Does not restrict operator removal of the object.
    @JsonProperty(value = "isRemovedAllowed")
    private Boolean isRemovedAllowed;

    // Reference to an instance of an ExternalGUtranCell MO.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToOne(targetEntity = ExternalGUtranCell.class, fetch = FetchType.EAGER)
    private ExternalGUtranCell neighborCellRef;

    // ESS service enabled between cells in this relation.
    @JsonProperty(value = "essEnabled")
    private Boolean essEnabled;

    // Whether target en-gNB cell referenced by this relation is allowed to be used for EN-DC.
    @JsonProperty(value = "isEndcAllowed")
    private Boolean isEndcAllowed;

    // eNodeB-gNodeB Sector Carrier pairs used in cell for ESS operation.
    @ElementCollection(fetch = FetchType.EAGER)
    @JsonProperty(value = "essCellScPairs")
    private List<EssCellScPairRO> essCellScPairs = new ArrayList<>();
    public GUtranCellRelation(String fdn) {
        super(fdn);
    }
}
