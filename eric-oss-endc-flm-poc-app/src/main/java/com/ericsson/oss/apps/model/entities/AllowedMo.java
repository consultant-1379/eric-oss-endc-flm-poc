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
package com.ericsson.oss.apps.model.entities;

import com.ericsson.oss.apps.api.model.EUtranCell;
import com.ericsson.oss.apps.api.model.Enodeb;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Entity(name = "allowed_mo")
@AllArgsConstructor
@NoArgsConstructor
public class AllowedMo {

    @EmbeddedId
    private ManagedObjectId objectId;
    @Column(name = "READ_ONLY")
    private Boolean readOnly;
    @Column(name = "IS_CELL", nullable = false)
    private Boolean isCell;
    @Column(name = "IS_TDD", nullable = false)
    private Boolean isTdd;
    @Column(name = "IS_BLOCKED", nullable = false)
    private Boolean isBlocked;

    public Optional<EUtranCell> asEutranCell() {

        if (isCell) {
            EUtranCell eUtranCell = new EUtranCell(this.getObjectId().toString());
            eUtranCell.setCellName(objectId.fetchDNValue());
            eUtranCell.setReadOnly(readOnly);
            return Optional.of(eUtranCell);
        }

        return Optional.empty();
    }

    public Optional<Enodeb> asEnodeb() {

        if (!isCell) {
            Enodeb enodeb = new Enodeb(this.getObjectId().toString());
            enodeb.setNodeName(objectId.fetchDNValue());
            enodeb.setReadOnly(readOnly);
            return Optional.of(enodeb);
        }

        return Optional.empty();
    }

    public AllowedMo(ManagedObjectId objectId, Boolean readOnly, Boolean isCell, Boolean isTdd) {
        this(objectId, readOnly, isCell, isTdd, false);
    }
}
