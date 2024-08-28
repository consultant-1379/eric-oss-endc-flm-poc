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
public class ExternalGNodeBFunction extends ManagedObject {
    @Serial
    private static final long serialVersionUID = 417727064423427105L;

    // The value component of the RDN
    @JsonProperty(value = "externalGNodeBFunctionId", access = JsonProperty.Access.WRITE_ONLY)
    private String externalGNodeBFunctionId;

    // The ID of an RBS within a Public Land Mobile Network (PLMN)
    @JsonProperty(value = "gNodeBId", access = JsonProperty.Access.WRITE_ONLY)
    private Long gNodeBId;

    // Length of the gNodeBId bit string representation
    @JsonProperty(value = "gNodeBIdLength", access = JsonProperty.Access.WRITE_ONLY)
    private Integer gNodeBIdLength;

    // The Public Land Mobile Network (PLMN) ID of the node.
    @JsonProperty(value = "gNodeBPlmnId", access = JsonProperty.Access.WRITE_ONLY)
    private PLMNId gNodeBPlmnId;

}