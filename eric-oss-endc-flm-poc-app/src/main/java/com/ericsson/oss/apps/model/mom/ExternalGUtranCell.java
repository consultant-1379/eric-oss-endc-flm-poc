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
public class ExternalGUtranCell extends ManagedObject {
    @Serial
    private static final long serialVersionUID = 417727064423427105L;

    // The value component of the RDN
    @JsonProperty(value = "externalGUtranCellId", access = JsonProperty.Access.WRITE_ONLY)
    private String externalGUtranCellId;

    // RBS internal ID for ExternalGUtranCell
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer localCellId;

    // Physical cell identity of external NR cell
    @JsonProperty(value = "nRPCI")
    private Integer nRPCI;

    // Absolute frequency reference point for downlink on external GUtranCell.
    // If pointAArfcnDl and pointAArfcnUl values are the same, cell is a TDD cell. Otherwise, cell is FDD.
    @JsonProperty(value = "pointAArfcnDl")
    private Integer pointAArfcnDl;

    // Absolute frequency reference point for uplink on external GUtranCell.
    // If pointAArfcnDl and pointAArfcnUl values are the same, cell is a TDD cell. Otherwise, cell is FDD.
    @JsonProperty(value = "pointAArfcnUl")
    private Integer pointAArfcnUl;

    // 5G-RAN Operating Bands.
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> bandList = new ArrayList<>(32);

    // NR cell cellStatus.
    @JsonProperty(value = "cellState")
    private NRCellState cellState;

    // Downlink transmission bandwidth configuration for the external GUtranCell.
    @JsonProperty(value = "transmissionBwDl")
    private NrTransmissionBandWidthRO transmissionBwDl;

    // Uplink transmission bandwidth configuration for the external GUtranCell.
    @JsonProperty(value = "transimissionBwUl")
    private NrTransmissionBandWidthRO transimissionBwUl;

    public ExternalGUtranCell(String fdn) {
        super(fdn);
    }
}
