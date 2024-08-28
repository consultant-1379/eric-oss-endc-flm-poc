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
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
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
public class SectorCarrier extends ManagedObject {

    @Serial
    private static final long serialVersionUID = 4177270645822367105L;

    // The value component of the RDN.
    @JsonProperty(value = "sectorCarrierId", access = JsonProperty.Access.WRITE_ONLY)
    private String sectorCarrierId;

    // Contains a list of MO instances that reserve this MO instance.
    @ElementCollection(fetch = FetchType.EAGER)
    private List<ManagedObjectId> reservedBy = new ArrayList<>();

    // eNodeB-internal ID for SectorCarrier within Ericsson Shared Spectrum.
    // Must be unique in eNodeB and non-zero. Zero means undefined.
    @JsonProperty("essScLocalId")
    private Integer essScLocalId;

    // ID attribute that pairs SectorCarriers from gNodeB and eNode in Ericsson Shared Spectrum.
    // Must be unique within gNodeB and eNodeB. Zero means undefined.
    @JsonProperty("essScPairId")
    private Integer essScPairId;

    public SectorCarrier(String fdn) {
        super(fdn);
    }

}