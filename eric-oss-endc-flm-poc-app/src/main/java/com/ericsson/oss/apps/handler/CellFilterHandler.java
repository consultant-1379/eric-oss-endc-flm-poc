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
package com.ericsson.oss.apps.handler;

import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.execution.ExecutionHandler;
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.service.CellFilterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CellFilterHandler implements ExecutionHandler<ExecutionContext> {

    private final CellFilterService cellFilterService;

    @Override
    public void handle(ExecutionContext context) {

        Map<String, List<SecondaryCellGroup>> eUtranCellToSCGsMap = cellFilterService.fetchSecondaryCellGroups(context.getAllowEutranCells());
        if (eUtranCellToSCGsMap.isEmpty()) {
            log.warn("CmLoader: primarySecondaryCell list is empty");
            return;
        }
        context.setEUtranCellToSCGsMap(eUtranCellToSCGsMap);
    }

    @Override
    public int getPriority() {
        return 11;
    }
}
