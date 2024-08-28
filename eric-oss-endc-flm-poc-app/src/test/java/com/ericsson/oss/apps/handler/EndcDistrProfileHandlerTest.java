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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.mom.EUtranCellFDD;
import com.ericsson.oss.apps.model.mom.EndcDistrProfile;
import com.ericsson.oss.apps.model.report.CellDataReportTuple;
import com.ericsson.oss.apps.model.report.EndcDistrProfileDataStatus;
import com.ericsson.oss.apps.model.report.Report;
import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.ncmp.NcmpClient;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmEUtranCellRepo;
import com.ericsson.oss.apps.service.EndcDistrProfileService;
import com.ericsson.oss.apps.service.ReportService;

@ExtendWith(MockitoExtension.class)
public class EndcDistrProfileHandlerTest {
    private ExecutionContext context;

    @Mock
    private EndcDistrProfileService endcDistrProfileService;
    @Mock
    private ReportService reportService;
    @Mock
    private NcmpClient ncmpClient;
    @Mock
    private CmEUtranCellRepo cmEUtranCellRepo;

    @InjectMocks
    private EndcDistrProfileHandler handler;

    private final long ROP_TIMESTAMP = 1234L;

    private final String ME_FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building";
    private final String ENB_FDN = ME_FDN + ",ENodeBFunction=1";

    private final String CELL_FDN = ENB_FDN + ",EUtranCellFDD=1";

    private final String PROFILE_FDN_1 = ENB_FDN + ",EndcDistrProfile=1";
    private final String PROFILE_FDN_2 = ENB_FDN + ",EndcDistrProfile=2";


    @BeforeEach
    public void setUp() {
        context = new ExecutionContext(ROP_TIMESTAMP);
    }

    @Test
    void updateExistingProfile() {
        EUtranCell cell1 = new EUtranCellFDD(CELL_FDN);
        EndcDistrProfile profile1 = new EndcDistrProfile(PROFILE_FDN_1);
        cell1.setEndcDistrProfileRef(profile1);

        CellDataReportTuple cellDataReportTuple = new CellDataReportTuple(ROP_TIMESTAMP, CELL_FDN);
        cellDataReportTuple.setProfileToWrite(profile1);
        Report report = new Report();
        report.setCellData(List.of(cellDataReportTuple));

        when(reportService.getReportByRop(ROP_TIMESTAMP)).thenReturn(report);
        when(cmEUtranCellRepo.getReferenceById(ManagedObjectId.of(CELL_FDN))).thenReturn(cell1);
        when(endcDistrProfileService.getUpdatedProfile(cell1, cellDataReportTuple)).thenReturn(profile1);
        when(endcDistrProfileService.isProfileUsedByCell(cell1, profile1)).thenReturn(true);
        when(ncmpClient.patchCmResource(profile1)).thenReturn(true);

        handler.handle(context);

        verify(endcDistrProfileService, times(1)).updateProfile(profile1);
        verify(reportService, times(1)).updateReportCellData(cellDataReportTuple);
        Assertions.assertEquals(EndcDistrProfileDataStatus.SUCCESS, cellDataReportTuple.getStatus());
        Assertions.assertEquals(false, cellDataReportTuple.getNewProfileCreated());
    }

    @Test
    void createNewProfile() {
        EUtranCell cell1 = new EUtranCellFDD(CELL_FDN);
        EndcDistrProfile profile1 = new EndcDistrProfile(PROFILE_FDN_1);
        cell1.setEndcDistrProfileRef(profile1);

        EndcDistrProfile profile2 = new EndcDistrProfile(PROFILE_FDN_2);

        CellDataReportTuple cellDataReportTuple = new CellDataReportTuple(ROP_TIMESTAMP, CELL_FDN);
        cellDataReportTuple.setProfileToWrite(profile1);
        Report report = new Report();
        report.setCellData(List.of(cellDataReportTuple));

        when(reportService.getReportByRop(ROP_TIMESTAMP)).thenReturn(report);
        when(cmEUtranCellRepo.getReferenceById(ManagedObjectId.of(CELL_FDN))).thenReturn(cell1);
        when(endcDistrProfileService.getUpdatedProfile(cell1, cellDataReportTuple)).thenReturn(profile2);
        when(endcDistrProfileService.isProfileUsedByCell(cell1, profile2)).thenReturn(false);
        when(ncmpClient.createCmResource(profile2)).thenReturn(true);
        when(ncmpClient.patchCmResource(cell1)).thenReturn(true);

        handler.handle(context);

        verify(endcDistrProfileService, times(1)).updateProfile(profile2);
        verify(cmEUtranCellRepo, times(1)).save(cell1);
        verify(reportService, times(1)).updateReportCellData(cellDataReportTuple);
        Assertions.assertEquals(EndcDistrProfileDataStatus.SUCCESS, cellDataReportTuple.getStatus());
        Assertions.assertEquals(true, cellDataReportTuple.getNewProfileCreated());
    }

    @Test
    void updateExistingProfileFailure() {
        EUtranCell cell1 = new EUtranCellFDD(CELL_FDN);
        EndcDistrProfile profile1 = new EndcDistrProfile(PROFILE_FDN_1);
        cell1.setEndcDistrProfileRef(profile1);

        CellDataReportTuple cellDataReportTuple = new CellDataReportTuple(ROP_TIMESTAMP, CELL_FDN);
        cellDataReportTuple.setProfileToWrite(profile1);
        Report report = new Report();
        report.setCellData(List.of(cellDataReportTuple));

        when(reportService.getReportByRop(ROP_TIMESTAMP)).thenReturn(report);
        when(cmEUtranCellRepo.getReferenceById(ManagedObjectId.of(CELL_FDN))).thenReturn(cell1);
        when(endcDistrProfileService.getUpdatedProfile(cell1, cellDataReportTuple)).thenReturn(profile1);
        when(endcDistrProfileService.isProfileUsedByCell(cell1, profile1)).thenReturn(true);
        when(ncmpClient.patchCmResource(profile1)).thenReturn(false);

        handler.handle(context);

        verify(endcDistrProfileService, times(0)).updateProfile(profile1);
        verify(reportService, times(1)).updateReportCellData(cellDataReportTuple);
        Assertions.assertEquals(EndcDistrProfileDataStatus.FAILED_AT_MODIFY_PROFILE, cellDataReportTuple.getStatus());
        Assertions.assertEquals(false, cellDataReportTuple.getNewProfileCreated());
    }

    @Test
    void createNewProfileFailedAtCreation() {
        EUtranCell cell1 = new EUtranCellFDD(CELL_FDN);
        EndcDistrProfile profile1 = new EndcDistrProfile(PROFILE_FDN_1);
        cell1.setEndcDistrProfileRef(profile1);

        EndcDistrProfile profile2 = new EndcDistrProfile(PROFILE_FDN_2);

        CellDataReportTuple cellDataReportTuple = new CellDataReportTuple(ROP_TIMESTAMP, CELL_FDN);
        cellDataReportTuple.setProfileToWrite(profile1);
        Report report = new Report();
        report.setCellData(List.of(cellDataReportTuple));

        when(reportService.getReportByRop(ROP_TIMESTAMP)).thenReturn(report);
        when(cmEUtranCellRepo.getReferenceById(ManagedObjectId.of(CELL_FDN))).thenReturn(cell1);
        when(endcDistrProfileService.getUpdatedProfile(cell1, cellDataReportTuple)).thenReturn(profile2);
        when(endcDistrProfileService.isProfileUsedByCell(cell1, profile2)).thenReturn(false);
        when(ncmpClient.createCmResource(profile2)).thenReturn(false);

        handler.handle(context);

        verify(endcDistrProfileService, times(0)).updateProfile(profile2);
        verify(cmEUtranCellRepo, times(0)).save(cell1);
        verify(reportService, times(1)).updateReportCellData(cellDataReportTuple);
        Assertions.assertEquals(EndcDistrProfileDataStatus.FAILED_AT_CREATING_PROFILE, cellDataReportTuple.getStatus());
        Assertions.assertEquals(false, cellDataReportTuple.getNewProfileCreated());
    }

    @Test
    void createNewProfileFailedAfterCreation() {
        EUtranCell cell1 = new EUtranCellFDD(CELL_FDN);
        EndcDistrProfile profile1 = new EndcDistrProfile(PROFILE_FDN_1);
        cell1.setEndcDistrProfileRef(profile1);

        EndcDistrProfile profile2 = new EndcDistrProfile(PROFILE_FDN_2);

        CellDataReportTuple cellDataReportTuple = new CellDataReportTuple(ROP_TIMESTAMP, CELL_FDN);
        cellDataReportTuple.setProfileToWrite(profile1);
        Report report = new Report();
        report.setCellData(List.of(cellDataReportTuple));

        when(reportService.getReportByRop(ROP_TIMESTAMP)).thenReturn(report);
        when(cmEUtranCellRepo.getReferenceById(ManagedObjectId.of(CELL_FDN))).thenReturn(cell1);
        when(endcDistrProfileService.getUpdatedProfile(cell1, cellDataReportTuple)).thenReturn(profile2);
        when(endcDistrProfileService.isProfileUsedByCell(cell1, profile2)).thenReturn(false);
        when(ncmpClient.createCmResource(profile2)).thenReturn(true);
        when(ncmpClient.patchCmResource(cell1)).thenReturn(false);

        handler.handle(context);

        verify(endcDistrProfileService, times(1)).updateProfile(profile2);
        verify(cmEUtranCellRepo, times(0)).save(cell1);
        verify(reportService, times(1)).updateReportCellData(cellDataReportTuple);
        Assertions.assertEquals(EndcDistrProfileDataStatus.FAILED_AT_MODIFY_CELL, cellDataReportTuple.getStatus());
        Assertions.assertEquals(true, cellDataReportTuple.getNewProfileCreated());
    }

    @Test
    void unableToGetNewProfile() {
        EUtranCell cell1 = new EUtranCellFDD(CELL_FDN);
        EndcDistrProfile profile1 = new EndcDistrProfile(PROFILE_FDN_1);
        cell1.setEndcDistrProfileRef(profile1);

        CellDataReportTuple cellDataReportTuple = new CellDataReportTuple(ROP_TIMESTAMP, CELL_FDN);
        cellDataReportTuple.setProfileToWrite(profile1);
        Report report = new Report();
        report.setCellData(List.of(cellDataReportTuple));

        when(reportService.getReportByRop(ROP_TIMESTAMP)).thenReturn(report);
        when(cmEUtranCellRepo.getReferenceById(ManagedObjectId.of(CELL_FDN))).thenReturn(cell1);
        when(endcDistrProfileService.getUpdatedProfile(cell1, cellDataReportTuple)).thenReturn(null);

        handler.handle(context);

        verify(reportService, times(1)).updateReportCellData(cellDataReportTuple);
        Assertions.assertEquals(EndcDistrProfileDataStatus.ABORTED_DATA_ERROR, cellDataReportTuple.getStatus());
        Assertions.assertEquals(false, cellDataReportTuple.getNewProfileCreated());
    }
}
