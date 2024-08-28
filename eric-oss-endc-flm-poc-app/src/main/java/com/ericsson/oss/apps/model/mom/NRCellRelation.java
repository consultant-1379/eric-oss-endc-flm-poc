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
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class NRCellRelation extends ManagedObject {

    private static final long serialVersionUID = 4116157051366354856L;

    public enum SupportedCoverage {
        NONE, COVERS, OVERLAP, CONTAINED_IN
    }

    public enum SCellCandidate {
        NOT_ALLOWED, ALLOWED, ONLY_ALLOWED_FOR_DL
    }

    private SupportedCoverage coverageIndicator;
    private SCellCandidate sCellCandidate;
    private Integer cellIndividualOffsetNR;

    // this avoids confusion between lombok
    // and jackson serialization
    @JsonProperty("isHoAllowed")
    private Boolean hoAllowed;

    public Boolean isHoAllowed() {
        return hoAllowed;
    }

    @Embedded
    @JsonProperty(value = "nRCellRef", access = JsonProperty.Access.WRITE_ONLY)
    @AttributeOverrides(value = {@AttributeOverride(name = "meFdn", column = @Column(name = "nrcell_me_fdn")), @AttributeOverride(name = "resRef", column = @Column(name = "nrcell_res_ref"))})
    private ManagedObjectId nRCellRef;
}
