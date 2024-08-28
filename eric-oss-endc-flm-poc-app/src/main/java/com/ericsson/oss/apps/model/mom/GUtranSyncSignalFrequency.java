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
public class GUtranSyncSignalFrequency extends ManagedObject {
    @Serial
    private static final long serialVersionUID = 417727064423427105L;

    // The value component of the RDN
    @JsonProperty(value = "gUtranSyncSignalFrequencyId", access = JsonProperty.Access.WRITE_ONLY)
    private String gUtranSyncSignalFrequencyId;

    // NR ARFCN associated with the synchronization signal frequency.
    @JsonProperty(value = "arfcn")
    private Integer arfcn;

    // 5G-RAN Operating Bands.
    @JsonProperty(value = "band")
    private Integer band;

    // 5G-RAN Operating Bands.
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> bandList = new ArrayList<>(32);

    public GUtranSyncSignalFrequency(String fdn) {
        super(fdn);
    }

}
