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

import com.ericsson.oss.apps.ncmp.model.AdministrativeState;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.model.Toggle;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
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
public class NRCellDU extends ManagedObject {
    @Serial
    private static final long serialVersionUID = 4177270645822367105L;

    // The value component of the RDN.
    @JsonProperty(value = "nRCellDUId", access = JsonProperty.Access.WRITE_ONLY)
    private String nRCellDUId;

    // Used together with gNodeB identifier to identify NR cell in PLMN.
    @JsonProperty(value = "cellLocalId")
    private Integer cellLocalId;

    // NR Cell Identity.
    @JsonProperty(value = "nCI")
    private Long nCI;

    // Frequency (NR-ARFCN) of the SSB transmission.
    @JsonProperty(value = "ssbFrequency")
    private Integer ssbFrequency;

    // Sub-carrier spacing of the SSB.
    @JsonProperty(value = "ssbSubCarrierSpacing")
    private Integer ssbSubCarrierSpacing;

    // The administrative state. While set to SHUTTINGDOWN the cell is automatically barred.
    // Set to LOCKED when all UEs have left the cell or timer maxOffloadDurationShutDown expires.
    @JsonProperty(value = "administrativeState")
    private AdministrativeState administrativeState;

    @JsonProperty(value = "operationalState")
    private Toggle operationalState;

    // Current service state.
    @JsonProperty(value = "serviceState")
    private NRCellServiceState serviceState;

    // Current cellState.
    @JsonProperty(value = "cellState")
    private NRCellState cellState;

    // Whether the NR cell is barred or not.
    @JsonProperty(value = "cellBarred")
    private CellBarred cellBarred;

    @JsonProperty(value = "dlAvailableCrbs")
    private Integer dlAvailableCrbs;

    // List of all NR bands the cells belongs to.
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> bandList = new ArrayList<>(32);

    @JsonProperty(value = "dl256QamEnabled")
    private Boolean dl256QamEnabled;

    public enum ModulationOrder {QAM_64, QAM_256}

    @JsonProperty(value = "dlMaxSupportedModOrder")
    private ModulationOrder dlMaxSupportedModOrder;

    @JsonProperty(value = "subCarrierSpacing")
    private Integer subCarrierSpacing;

    public enum TddUlDlPatternType {
        TDD_ULDL_PATTERN_00, TDD_ULDL_PATTERN_01, TDD_ULDL_PATTERN_02, TDD_ULDL_PATTERN_03, TDD_ULDL_PATTERN_04,
        TDD_ULDL_PATTERN_05, TDD_ULDL_PATTERN_06, TDD_ULDL_PATTERN_07, TDD_ULDL_PATTERN_08
    }

    @JsonProperty(value = "tddUlDlPattern")
    private TddUlDlPatternType tddUlDlPattern;

    // Whether the cell is reserved for operator use or not.
    @JsonProperty(value = "cellReservedForOperator")
    private CellReservedForOperator cellReservedForOperator;

    // Whether EN-DC downlink leg switching is enabled.
    @JsonProperty(value = "endcDlLegSwitchEnabled")
    private Boolean endcDlLegSwitchEnabled;

    // Whether EN-DC or NR-DC uplink leg switching is enabled.
    @JsonProperty(value = "endcUlLegSwitchEnabled")
    private Boolean endcUlLegSwitchEnabled;

    // References to instances of NRSectorCarrier MO.
    @JsonProperty(value = "nRSectorCarrierRef", access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(targetEntity = NRSectorCarrier.class, fetch = FetchType.EAGER)
    private List<NRSectorCarrier> nRSectorCarrierRef = new ArrayList<>();

}
