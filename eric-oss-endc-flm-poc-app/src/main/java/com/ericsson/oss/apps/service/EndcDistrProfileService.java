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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.mom.EndcDistrProfile;
import com.ericsson.oss.apps.model.report.CellDataReportTuple;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmEUtranCellRepo;
import com.ericsson.oss.apps.repository.CmEndcDistrProfileRepo;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndcDistrProfileService {
    private final CmEUtranCellRepo cmEUtranCellRepo;
    private final CmEndcDistrProfileRepo cmEndcDistrProfileRepo;

    public boolean isProfileUsedByCell(EUtranCell cell, EndcDistrProfile profile) {
        final EndcDistrProfile cellProfile = cell.getEndcDistrProfileRef();
        if (cellProfile != null) {
            return cellProfile.getFdn().equals(profile.getFdn());
        }
        return false;
    }

    public boolean needNewProfile(EUtranCell cell) {
        /**
         * Check if new EndcDistrProfile needed if cell's configuration is updated.
         * - the new profile is NOT needed only if the Cell has a dedicated non-shared profile;
         * - otherwise a new profile shall be created, it could be following conditions:
         *   - there is no EndcDistrProfile associates with the cell; or
         *   - the EndcDistrProfile associates with the cell is also referred by other cells.
         **/
        final EndcDistrProfile profile = cell.getEndcDistrProfileRef();

        if (profile != null) {
            List<EUtranCell> cellsWithProfile = cmEUtranCellRepo.findAll().stream()
                    .filter(theCell -> isProfileUsedByCell(theCell, profile))
                    .toList();

            return cellsWithProfile.size() > 1;
        }

        return true;
    }

    public EndcDistrProfile getUpdatedProfile(EUtranCell cell, CellDataReportTuple tuple) {
        // Sanity checks for new profile.
        EndcDistrProfile profileToWrite = tuple.getProfileToWrite();
        if (profileToWrite.getGUtranFreqRef().size() != profileToWrite.getGUtranFreqDistribution().size()) {
            log.error("ERROR: Unable to update EndcDistrProfile, " +
                            "size of gUtranFreqDistribution ({}) and gUtranFreqRef ({}) mismatch.",
                      profileToWrite.getGUtranFreqDistribution().size(),
                      profileToWrite.getGUtranFreqRef().size());
            return null;
        }

        if (profileToWrite.getMandatoryGUtranFreqRef().size() > 8) {
            log.error("ERROR: Unable to update EndcDistrProfile, " +
                            "size of incoming mandatoryGUtranFreqRef is too large: {}",
                       profileToWrite.getMandatoryGUtranFreqRef().size());
            return null;
        }

        // Update an existing EndcDistrProfile with input data from initial report, or create a new
        // EndcDistrProfile if needed.
        EndcDistrProfile profile = cell.getEndcDistrProfileRef();
        if (needNewProfile(cell)) {
            // Try to pick an unused profile before creating one - leave for future enhancement.
            profile = constructDefaultProfile(cell);
        }

        profile.setGUtranFreqRef(profileToWrite.getGUtranFreqRef());
        profile.setMandatoryGUtranFreqRef(profileToWrite.getMandatoryGUtranFreqRef());
        profile.setGUtranFreqDistribution(profileToWrite.getGUtranFreqDistribution());

        return profile;
    }

    public void updateProfile(EndcDistrProfile profile) {
        cmEndcDistrProfileRepo.save(profile);
    }

    private EndcDistrProfile constructDefaultProfile(EUtranCell cell) {
        ManagedObjectId cellObjectId = cell.getObjectId();
        String ldnPrefix = cellObjectId.fetchParentId().toString() + ",EndcDistrProfile=";
        String profileId = "rApp_" + cell.getCellId();

        // Check if profileId is already being used.
        if (!isProfileIdGood(ldnPrefix + profileId)) {
            // DB access per LDN candidate v.s. Read all objects from DB and search in memory?
            // Assuming the "rApp_" prefix is not used by customer in manual operation, hence
            // the DB read would be less than thrice.
            int i = 1;
            while (!isProfileIdGood(ldnPrefix + profileId + "_" + i)) {
                i++;
            }
            profileId = profileId + "_" + i;
        }

        log.debug("New profile ID found: {}", profileId);
        final String ldn = ldnPrefix + profileId;

        // Create default endcDistrProfile
        EndcDistrProfile profile = new EndcDistrProfile(ldn);
        profile.setEndcDistrProfileId(profileId);
        profile.setEndcUserThreshold(0);

        return profile;
    }

    private Boolean isProfileIdGood(String fdn) {
        // Check if given fdn can be used for creating a new EndcDistrProfile.
        return cmEndcDistrProfileRepo.findById(ManagedObjectId.of(fdn)).isEmpty();
    }
}
