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

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;


@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class EssCellScPairRO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8014789333222345686L;

    // eNodeB Local SectorCarrier ID.
    private Integer eNBessLocalScId;

    // ESS SectorCarrier Pair ID.
    private Long essScPairId;

    // gNodeB Local SectorCarrier ID.
    private Integer gNBessLocalScId;

}
