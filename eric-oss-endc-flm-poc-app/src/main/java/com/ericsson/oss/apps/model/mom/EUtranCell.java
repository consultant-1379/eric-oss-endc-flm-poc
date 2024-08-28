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
import com.ericsson.oss.apps.ncmp.model.Toggle;
import com.ericsson.oss.apps.topology.model.PLMNId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
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
public abstract class EUtranCell extends ManagedObject {

    @Serial
    private static final long serialVersionUID = 417727064423427105L;

    // The value component of the RDN
    private String eUtranCellId;

    // RBS internal ID attribute for EUtranCell
    @JsonProperty(value = "cellId", access = JsonProperty.Access.WRITE_ONLY)
    private Integer cellId;

    @JsonProperty(value = "availabilityStatus")
    private AvailStatus availabilityStatus;

    @JsonProperty(value = "operationalState")
    private Toggle operationalState;

    // The channel number for the central downlink frequency.
    @JsonProperty(value = "earfcndl")
    private Integer earfcndl;

    // Channel number for the central UL frequency.
    @JsonProperty(value = "earfcnul")
    private Integer earfcnul;

    // The primary frequency band the cell belongs to according to its defined EARFCN.
    @JsonProperty(value = "freqBand")
    private Integer freqBand;

    // PLMN ID allowed to use EN-DC. If empty, EN-DC not allowed in cell.
    @ElementCollection(fetch = FetchType.EAGER)
    private List<PLMNId> endcAllowedPlmnList = new ArrayList<>();

    // Reference to an instance of ExternalGUtranCell. If set, indicated GUtranCell must have coverage over this cell.
    @ElementCollection(fetch = FetchType.EAGER)
    private List<ExternalGUtranCell> extGUtranCellRef = new ArrayList<>();

    // Threshold for age of earliest RLC SDU for DRB in DL, monitored by buffer-based EN-DC setup.
    // If value is 0, age of RLC SDUs in DL is not considered during buffer-based EN-DC setup.
    @JsonProperty(value = "endcSetupDlPktAgeThr")
    private Integer endcSetupDlPktAgeThr;

    // Threshold for total volume of RLC SDUs of DRB in DL, monitored by buffer-based EN-DC setup.
    // If value is 0, volume of RLC SDUs in DL is not considered during buffer-based EN-DC setup.
    @JsonProperty(value = "endcSetupDLPktVolThr")
    private Integer endcSetupDLPktVolThr;

    // Reference to a list of instances of SectorCarrier MO
    @ElementCollection(fetch = FetchType.EAGER)
    private List<SectorCarrier> sectorCarrierRef = new ArrayList<>();

    // The downlink channel bandwidth in the cell.
    @JsonProperty(value = "dlChannelBandwidth")
    private Integer dlChannelBandwidth;

    // The uplink channel bandwidth in the cell.
    @JsonProperty(value = "ulChannelBandwidth")
    private Integer ulChannelBandwidth;

    @ManyToOne(fetch = FetchType.EAGER)
    private EndcDistrProfile endcDistrProfileRef;

    public EUtranCell(String fdn) {
        super(fdn);
    }
}
