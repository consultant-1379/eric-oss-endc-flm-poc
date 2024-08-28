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
package com.ericsson.oss.apps.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.apps.model.EndcFreqProfileData;
import com.ericsson.oss.apps.model.entities.AllowedMo;

import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.ericsson.oss.apps.model.SecondaryCellGroup;

@Setter
@Getter
@RequiredArgsConstructor
public class ExecutionContext {
    private final long ropTimeStamp;

    private List<AllowedMo> allowList = new ArrayList<>();
    private List<ManagedObjectId> allowEutranCells = new ArrayList<>();
    private Map<String, List<SecondaryCellGroup>> eUtranCellToSCGsMap = new HashMap<>();
    private Map<String, EndcFreqProfileData> eUtranCellToProfileData = new HashMap<>();
}
