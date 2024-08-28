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

import java.util.List;

import org.springframework.stereotype.Component;

import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.execution.ExecutionHandler;
import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.mom.EndcDistrProfile;
import com.ericsson.oss.apps.model.report.CellDataReportTuple;
import com.ericsson.oss.apps.model.report.EndcDistrProfileDataStatus;
import com.ericsson.oss.apps.ncmp.NcmpClient;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmEUtranCellRepo;
import com.ericsson.oss.apps.service.EndcDistrProfileService;
import com.ericsson.oss.apps.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndcDistrProfileHandler implements ExecutionHandler<ExecutionContext> {
    private final EndcDistrProfileService endcDistrProfileService;
    private final ReportService reportService;

    private final NcmpClient ncmpClient;

    private final CmEUtranCellRepo cmEUtranCellRepo;

    @Override
    public void handle(ExecutionContext executionContext) {
        List<CellDataReportTuple> cellDataTuples = reportService.getReportByRop(executionContext.getRopTimeStamp()).getCellData();
        log.debug("number of EUTran Cells to be maintained: " + cellDataTuples.size());

        for (CellDataReportTuple tuple: cellDataTuples) {
            ManagedObjectId cellObjectId = ManagedObjectId.of(tuple.getReportDataId().getObjectFdn());
            EUtranCell cell = cmEUtranCellRepo.getReferenceById(cellObjectId);

            EndcDistrProfile profile = endcDistrProfileService.getUpdatedProfile(cell, tuple);
            if (profile != null) {
                log.info("Before: Cell {} references to profile {}.", cell.getObjectId(), cell.getEndcDistrProfileRef());
                if (!endcDistrProfileService.isProfileUsedByCell(cell, profile)) {
                    // New profile - Create EndcDistrProfile MO and update Cell's endcDistrProfileRef.
                    setupNewProfileForCell(cell, profile, tuple);
                }
                else {
                    // Existing profile - Only update EndcDistrProfile MO.
                    updateProfileForCell(profile, tuple);
                }
            }
            else {
                tuple.setStatus(EndcDistrProfileDataStatus.ABORTED_DATA_ERROR);
            }

            reportService.updateReportCellData(tuple);
        }
    }

    @Override
    public int getPriority() {
        return 50;
    }

    private void setupNewProfileForCell(EUtranCell cell, EndcDistrProfile profile, CellDataReportTuple tuple) {
        if (ncmpClient.createCmResource(profile)) {
            endcDistrProfileService.updateProfile(profile);
            tuple.setNewProfileCreated(true);

            // Update CM data before storing it in local database.
            cell.setEndcDistrProfileRef(profile);
            if (ncmpClient.patchCmResource(cell)) {
                // Need re-visit for efficient way to update object in JPA.
                cmEUtranCellRepo.save(cell);
                log.info("After: Cell {} references to profile {}.", cell.getObjectId(), cell.getEndcDistrProfileRef());

                // Update report DB tuple with status SUCCESS
                tuple.setStatus(EndcDistrProfileDataStatus.SUCCESS);
            }
            else {
                // Leave created profile there.
                log.error("Failed to update Cell MO.");

                // Update report DB tuple with status FAILED with profile created.
                tuple.setStatus(EndcDistrProfileDataStatus.FAILED_AT_MODIFY_CELL);
            }
        }
        else {
            log.error("Failed to create new EndcDistrProfile MO.");
            // Update the report DB tuple with status FAILED
            tuple.setStatus(EndcDistrProfileDataStatus.FAILED_AT_CREATING_PROFILE);
        }
    }

    private void updateProfileForCell(EndcDistrProfile profile, CellDataReportTuple tuple) {
        if (ncmpClient.patchCmResource(profile)) {
            // Need re-visit for efficient way to update object in JPA
            endcDistrProfileService.updateProfile(profile);
            // Update the report DB tuple with status SUCCESS
            tuple.setStatus(EndcDistrProfileDataStatus.SUCCESS);
        }
        else {
            log.error("Failed to update EndcDistrProfile MO.");
            // Update the report DB tuple with status FAILED
            tuple.setStatus(EndcDistrProfileDataStatus.FAILED_AT_MODIFY_PROFILE);
        }
    }
}
