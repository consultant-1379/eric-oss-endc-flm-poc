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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ExternalGNBCUCPFunction extends ManagedObject {

    private static final long serialVersionUID = 6455912077859249587L;

    @JsonProperty(value = "gNBId", access = JsonProperty.Access.WRITE_ONLY)
    private Long gNBId;
    @JsonProperty(value = "gNBIdLength", access = JsonProperty.Access.WRITE_ONLY)
    private Integer gNBIdLength;
    @JsonProperty(value = "pLMNId", access = JsonProperty.Access.WRITE_ONLY)
    private PLMNId pLMNId;
    public ExternalGNBCUCPFunction(String fdn) {
        super(fdn);
    }
}
