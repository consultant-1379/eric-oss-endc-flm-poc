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
package com.ericsson.oss.apps.service;

import com.ericsson.oss.apps.api.model.AllowList;
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class CmServiceTest {
    private static final String NODE_FDN = "SubNetwork=Ireland,MeContext=NR03gNodeBRadio00002,ManagedElement=NR03gNodeBRadio00001";
    private static final String CELL_TDD_FDN = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00002,ManagedElement=NR01gNodeBRadio00002,ENodeBFunction=1,EUtranCellFDD=eUtranCell00002-1";
    @Autowired
    public CmService cmService;
    @Autowired
    CmAllowListRepo cmAllowListRepo;

    @MockBean
    CmEndcDistrProfileRepo cmEndcDistrProfileRepo;
    @MockBean
    CmEUtranCellRepo cmEUtranCellRepo;
    @MockBean
    CmExternalGNBCUCPFunctionRepo cmExternalGNBCUCPFunctionRepo;
    @MockBean
    CmExternalGNodeBFunctionRepo cmExternalGNodeBFunctionRepo;
    @MockBean
    CmExternalGUtranCellRepo cmExternalGUtranCellRepo;
    @MockBean
    CmExternalNRCellCURepo cmExternalNRCellCURepo;
    @MockBean
    CmGNBCUCPFunctionRepo cmGNBCUCPFunctionRepo;
    @MockBean
    CmGUtranCellRelationRepo cmGUtranCellRelationRepo;
    @MockBean
    CmGUtranFreqRelationRepo cmGUtranFreqRelationRepo;
    @MockBean
    CmGUtranSyncSignalFrequencyRepo cmGUtranSyncSignalFrequencyRepo;
    @MockBean
    CmNRCellCURepo cmNRCellCURepo;
    @MockBean
    CmNRCellDURepo cmNRCellDURepo;
    @MockBean
    CmNRCellRelationRepo cmNRCellRelationRepo;
    @MockBean
    CmNRSectorCarrierRepo cmNRSectorCarrierRepo;

    @BeforeEach
    void setup() {
        cmAllowListRepo.deleteAll();
        List<AllowedMo> allowedMos = new ArrayList<>();

        allowedMos.add(new AllowedMo(new ManagedObjectId(NODE_FDN), true, false, false));
        allowedMos.add(new AllowedMo(new ManagedObjectId(CELL_TDD_FDN), true, true, true));
        cmAllowListRepo.saveAll(allowedMos);
    }

    @Test
    void getAllowList() {
        AllowList allowList = cmService.getAllowList();
        assertEquals(1, allowList.getEnodebs().size());
        assertEquals(1, allowList.getEutranCells().size());
    }

    @Test
    void loadCmAllowListRepo() {
        List<AllowedMo> allRepoData = cmService.loadCmAllowListRepo();
        assertEquals(2, allRepoData.size());
    }
    @Test
    void getAllowedEutranCells() {
        List<AllowedMo> allRepoData = cmService.loadCmAllowListRepo();
        List<ManagedObjectId> allowEutranCells = cmService.getAllowedEutranCells(allRepoData);
        assertEquals(1, allowEutranCells.size());
    }

    @Test
    void cleanCmData() {
        cmService.cleanCmData();
        verify(cmEndcDistrProfileRepo, times(1)).deleteAll();
        verify(cmEUtranCellRepo, times(1)).deleteAll();
        verify(cmExternalGNBCUCPFunctionRepo, times(1)).deleteAll();
        verify(cmExternalGNodeBFunctionRepo, times(1)).deleteAll();
        verify(cmExternalGUtranCellRepo, times(1)).deleteAll();
        verify(cmExternalNRCellCURepo, times(1)).deleteAll();
        verify(cmGNBCUCPFunctionRepo, times(1)).deleteAll();
        verify(cmGUtranCellRelationRepo, times(1)).deleteAll();
        verify(cmGUtranFreqRelationRepo, times(1)).deleteAll();
        verify(cmGUtranSyncSignalFrequencyRepo, times(1)).deleteAll();
        verify(cmNRCellCURepo, times(1)).deleteAll();
        verify(cmNRCellDURepo, times(1)).deleteAll();
        verify(cmNRCellRelationRepo, times(1)).deleteAll();
        verify(cmNRSectorCarrierRepo, times(1)).deleteAll();
    }
}
