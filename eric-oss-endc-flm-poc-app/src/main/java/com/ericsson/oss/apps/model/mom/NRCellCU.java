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
import com.ericsson.oss.apps.topology.model.PLMNId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.io.Serial;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class NRCellCU extends ManagedObject {

    @Serial
    private static final long serialVersionUID = 417727064423427105L;

    // The value component of the RDN
    @JsonProperty(value = "nRCellCUId", access = JsonProperty.Access.WRITE_ONLY)
    private String nRCellCUId;

    // NR Cell Identity.
    @JsonProperty(value = "nCI")
    private Long nCI;

    // 	PLMN ID for NR CGI. If empty, GNBCUCPFunction::pLMNId is used for PLMN ID in NR CGI.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private PLMNId primaryPLMNId;

    // Used together with gNodeB identifier to identify NR cell in PLMN. Used together with gNBId to form NCI.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer cellLocalId;

    // Reference to an instance of NRFrequency MO corresponding to cell SSB configuration.
    @OneToOne
    @JsonProperty(value = "nRFrequencyRef")
    private NRFrequency nRFrequencyRef;

    // Current service state.
    private NRCellServiceState serviceState;

    // Current cellState.
    private NRCellState cellState;

    // Whether cell is Primary Secondary Cell (PSCell) capable in EN-DC and NR-DC.
    // For EN-DC: If false, cell is not shared over X2AP.
    // For NR-DC: Regardless of value, all cells are shared over XnAP.
    // If empty, it is unknown whether cell is Primary Secondary Cell (PSCell) capable.
    @JsonProperty(value = "pSCellCapable")
    private Boolean pSCellCapable;

    // Whether traffic steering function PSCell change to higher priority is enabled for NRCellCU.
    @JsonProperty(value = "hiPrioDetEnabled")
    private Boolean hiPrioDetEnabled;

    // Reference to an instance of TrStPSCellProfile MO.
    // Automatically set to TrStPSCellProfile=Default instance, unless manually set to refer to another MO instance.
    @OneToOne
    @JsonProperty(value = "trStPSCellProfileRef")
    private TrStPSCellProfile trStPSCellProfileRef;

    public NRCellCU(String fdn) {
        super(fdn);
    }
}
