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

import java.io.Serial;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class NRSectorCarrier extends ManagedObject {

    @Serial
    private static final long serialVersionUID = 4177270645822367105L;

    // The value component of the RDN
    @JsonProperty(value = "nRSectorCarrierId", access = JsonProperty.Access.WRITE_ONLY)
    private String nRSectorCarrierId;

    // NR Absolute Radio Frequency Channel Number (NR-ARFCN) for downlink.
    @JsonProperty("arfcnDL")
    private Integer arfcnDL;

    //NR Absolute Radio Frequency Channel Number (NR-ARFCN) for uplink.
    @JsonProperty("arfcnUL")
    private Integer arfcnUL;

    // RF Reference Frequency of downlink channel.
    @JsonProperty("frequencyDL")
    private Integer frequencyDL;

    // RF Reference Frequency of uplink channel.
    @JsonProperty("frequencyUL")
    private Integer frequencyUL;

    // Latitude of transmitter antenna position.
    // Positive value means north, negative value means south.
    @JsonProperty("latitude")
    private Integer latitude;

    // Longitude of transmitter antenna position.
    // Positive value means east, negative value means west.
    @JsonProperty("longitude")
    private Integer longitude;

    // BS Channel bandwidth in MHz for downlink.
    @JsonProperty("bSChannelBwDL")
    private Integer bSChannelBwDL;

    // BS Channel bandwidth in MHz for uplink.
    @JsonProperty("bSChannelBwUL")
    private Integer bSChannelBwUL;

    // The gNodeB internal ID attribute for SectorCarrier within Ericsson Shared Spectrum.
    @JsonProperty("essScLocalId")
    private Integer essScLocalId;

    // ID attribute that pairs SectorCarriers from gNodeB and eNodeB in Ericsson Spectrum Sharing (ESS).
    @JsonProperty("essScPairId")
    private Long essScPairId;

    public NRSectorCarrier(String fdn) {
        super(fdn);
    }
}