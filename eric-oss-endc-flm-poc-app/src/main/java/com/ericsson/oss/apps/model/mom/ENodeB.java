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
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public abstract class ENodeB extends ManagedObject {

    @Serial
    private static final long serialVersionUID = 5362568752262201741L;

    // The ENodeB ID that forms part of the Cell Global Identity,
    // and is also used to identify the node over the S1 Interface
    @JsonProperty(value = "eNBId")
    private Long eNBId;

    // Allows or restricts use of EN-DC for UEs within node.
    @JsonProperty(value = "endcAllowed")
    private Boolean endcAllowed;

    // PLMN identifier used as part of PM Events data
    @JsonProperty(value = "eNodeBPlmnId")
    private PLMNId eNodeBPlmnId;

    ENodeB(String fdn) {
        super(fdn);
    }

}
