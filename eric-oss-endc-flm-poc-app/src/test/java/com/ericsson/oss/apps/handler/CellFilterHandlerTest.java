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
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.mom.NRCellCU;
import com.ericsson.oss.apps.service.CellFilterService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CellFilterHandlerTest {

    @Mock
    private CellFilterService cellFilterService;
    @InjectMocks
    private CellFilterHandler handler;
    private static final NRCellCU PRIMARY_NR_CELL = new NRCellCU();
    private static final List<NRCellCU> SECONDARY_NR_CELLS = List.of(new NRCellCU());
    private static final String EUTRAN_CELL_FDD_FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054815";
    private static final Map<String, List<SecondaryCellGroup>> EUTRAN_CELL_TO_SCG_MAP =
            Map.of(EUTRAN_CELL_FDD_FDN, List.of(new SecondaryCellGroup(PRIMARY_NR_CELL, SECONDARY_NR_CELLS, 2)));
    ExecutionContext context = new ExecutionContext(1234L);;

    @Test
    void loadFilteredCells() {
        handler.handle(context);
        Assertions.assertTrue(context.getEUtranCellToSCGsMap().isEmpty());
        when(cellFilterService.fetchSecondaryCellGroups(context.getAllowEutranCells())).thenReturn(EUTRAN_CELL_TO_SCG_MAP);
        handler.handle(context);
        Assertions.assertEquals(EUTRAN_CELL_TO_SCG_MAP, context.getEUtranCellToSCGsMap());
    }
}
