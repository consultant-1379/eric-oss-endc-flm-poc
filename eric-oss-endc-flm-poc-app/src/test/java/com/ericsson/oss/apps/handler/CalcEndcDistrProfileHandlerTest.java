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
import com.ericsson.oss.apps.model.EndcFreqProfileData;
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;

import com.ericsson.oss.apps.service.CalcEndcDistrProfileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CalcEndcDistrProfileHandlerTest {

    @InjectMocks
    private CalcEndcDistrProfileHandler handler;
    @Mock
    private CalcEndcDistrProfileService calcEndcDistrProfileService;
    private ExecutionContext context;
    private static final long ROP_TIME = 1234L;

    private static final String EUTRAN_CELL_FDD_1 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054815";
    private static final String EUTRAN_CELL_FDD_2 = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054816";

    private static final Map<String, List<SecondaryCellGroup>> EUTRAN_CELL_TO_SCG_MAP = Map.of(EUTRAN_CELL_FDD_1, List.of());
    private static final List<ManagedObjectId> ALLOW_EUTRAN_CELLS = List.of(new ManagedObjectId(EUTRAN_CELL_FDD_1), new ManagedObjectId(EUTRAN_CELL_FDD_2));

    @BeforeEach
    public void setUp() {

        context = new ExecutionContext(ROP_TIME);
        context.setEUtranCellToSCGsMap(EUTRAN_CELL_TO_SCG_MAP);
        context.setAllowEutranCells(ALLOW_EUTRAN_CELLS);
    }

    @Test
    void endcProfileDataByCapacity() {

        Map<String, EndcFreqProfileData> eUtranCellToProfileData = new HashMap<>();
        EndcFreqProfileData profileData = new EndcFreqProfileData(null, null, null);
        eUtranCellToProfileData.put(EUTRAN_CELL_FDD_1, profileData);

        when(calcEndcDistrProfileService.processEndcDistrProfile(ALLOW_EUTRAN_CELLS, EUTRAN_CELL_TO_SCG_MAP, false, ROP_TIME)).thenReturn(eUtranCellToProfileData);

        Assertions.assertTrue(context.getEUtranCellToProfileData().isEmpty());

        handler.handle(context);
        Assertions.assertEquals(eUtranCellToProfileData, context.getEUtranCellToProfileData());
    }
}
