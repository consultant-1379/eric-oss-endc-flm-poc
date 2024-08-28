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

import lombok.*;
import org.springframework.data.annotation.Id;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class NrTransmissionBandWidthRO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8014789333222345686L;

    @Id
    // Number of Resource Blocks (NRB).
    private Integer noOfResourceBlocks;

    // Subcarrier Spacing (SCS).
    private Integer subCarrierSpacing;
}
