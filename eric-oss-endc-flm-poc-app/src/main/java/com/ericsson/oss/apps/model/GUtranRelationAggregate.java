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
package com.ericsson.oss.apps.model;

import com.ericsson.oss.apps.model.mom.*;
import com.ericsson.oss.apps.topology.model.NRCellId;

public record GUtranRelationAggregate(
        GUtranFreqRelation gUtranFreqRelation,
        GUtranCellRelation gUtranCellRelation,
        ExternalGUtranCell externalGUtranCell,
        ExternalGNodeBFunction externalGNodeBFunction
) {
    public NRCellId getTargetGlobalCellId() {
        return new NRCellId(
                externalGNodeBFunction.getGNodeBPlmnId(),
                externalGNodeBFunction.getGNodeBId(), externalGNodeBFunction.getGNodeBIdLength(),
                externalGUtranCell.getLocalCellId());
    }
}
