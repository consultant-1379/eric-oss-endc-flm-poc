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

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class EndcDistrProfile extends ManagedObject {
    @Serial
    private static final long serialVersionUID = 417727064423427105L;

    // The value component of the RDN
    @JsonProperty(value = "endcDistrProfileId", access = JsonProperty.Access.WRITE_ONLY)
    private String endcDistrProfileId;

    private Integer endcUserThreshold;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> gUtranFreqDistribution = new ArrayList<>();

    @JsonProperty(value = "gUtranFreqRef")
    @ManyToMany(fetch = FetchType.EAGER)
    private List<GUtranSyncSignalFrequency> gUtranFreqRef = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    private List<GUtranSyncSignalFrequency> mandatoryGUtranFreqRef = new ArrayList<>();

    public EndcDistrProfile(String fdn) {
        super(fdn);
    }
}
