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

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.mom.EUtranCellFDD;
import com.ericsson.oss.apps.model.mom.EndcDistrProfile;
import com.ericsson.oss.apps.model.mom.GUtranSyncSignalFrequency;
import com.ericsson.oss.apps.model.report.CellDataReportTuple;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmEUtranCellRepo;
import com.ericsson.oss.apps.repository.CmEndcDistrProfileRepo;
import com.ericsson.oss.apps.repository.CmGUtranSyncSignalFrequencyRepo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
public class EndcDistrProfileServiceTest {
    @Autowired
    private CmEUtranCellRepo cmEUtranCellRepo;
    @Autowired
    private CmEndcDistrProfileRepo cmEndcDistrProfileRepo;
    @Autowired
    private CmGUtranSyncSignalFrequencyRepo cmGUtranSyncSignalFrequencyRepo;

    @Autowired
    private EndcDistrProfileService endcDistrProfileService;

    private static final long ROP_TIMESTAMP = 1234L;

    private static final String ME_FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building";
    private static final String ENB_FDN = ME_FDN + ",ENodeBFunction=1";

    private static final String CELL_FDN_1 = ENB_FDN + ",EUtranCellFDD=1";
    private static final String CELL_FDN_2 = ENB_FDN + ",EUtranCellFDD=2";
    private static final String CELL_FDN_3 = ENB_FDN + ",EUtranCellFDD=3";
    private static final String CELL_FDN_4 = ENB_FDN + ",EUtranCellFDD=4";

    private static final String PROFILE_FDN_1 = ENB_FDN + ",EndcDistrProfile=1";
    private static final String PROFILE_FDN_2 = ENB_FDN + ",EndcDistrProfile=2";

    private static final String GU_NETWORK_FDN = ENB_FDN + ",GUtraNetwork=1";
    private static final String FREQ_FDN_1 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=620000-20";
    private static final String FREQ_FDN_2 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=620100-20";
    private static final String FREQ_FDN_3 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=620200-20";
    private static final String FREQ_FDN_4 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=620300-20";
    private static final String FREQ_FDN_5 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=620400-20";
    private static final String FREQ_FDN_6 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=630500-20";
    private static final String FREQ_FDN_7 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=630600-20";
    private static final String FREQ_FDN_8 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=630700-20";
    private static final String FREQ_FDN_9 = GU_NETWORK_FDN + ",GUtranSyncSignalFrequency=630800-20";

    static EUtranCell cell1 = new EUtranCellFDD(CELL_FDN_1);
    static EUtranCell cell2 = new EUtranCellFDD(CELL_FDN_2);
    static EUtranCell cell3 = new EUtranCellFDD(CELL_FDN_3);
    static EUtranCell cell4 = new EUtranCellFDD(CELL_FDN_4);

    static EndcDistrProfile profile1 = new EndcDistrProfile(PROFILE_FDN_1);
    static EndcDistrProfile profile2 = new EndcDistrProfile(PROFILE_FDN_2);

    static GUtranSyncSignalFrequency freq1 = new GUtranSyncSignalFrequency(FREQ_FDN_1);
    static GUtranSyncSignalFrequency freq2 = new GUtranSyncSignalFrequency(FREQ_FDN_2);
    static GUtranSyncSignalFrequency freq3 = new GUtranSyncSignalFrequency(FREQ_FDN_3);
    static GUtranSyncSignalFrequency freq4 = new GUtranSyncSignalFrequency(FREQ_FDN_4);
    static GUtranSyncSignalFrequency freq5 = new GUtranSyncSignalFrequency(FREQ_FDN_5);
    static GUtranSyncSignalFrequency freq6 = new GUtranSyncSignalFrequency(FREQ_FDN_6);
    static GUtranSyncSignalFrequency freq7 = new GUtranSyncSignalFrequency(FREQ_FDN_7);
    static GUtranSyncSignalFrequency freq8 = new GUtranSyncSignalFrequency(FREQ_FDN_8);
    static GUtranSyncSignalFrequency freq9 = new GUtranSyncSignalFrequency(FREQ_FDN_9);

    @BeforeAll
    static void setUp() {
        cell1.setEndcDistrProfileRef(profile1);
        cell2.setEndcDistrProfileRef(profile2);
        cell3.setEndcDistrProfileRef(profile2);
        cell1.setCellId(1);
        cell2.setCellId(2);
        cell3.setCellId(3);
        cell4.setCellId(4);
    }

    @BeforeEach
    void resetRepos() {
        cmEUtranCellRepo.deleteAll();
        cmEndcDistrProfileRepo.deleteAll();
        cmGUtranSyncSignalFrequencyRepo.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("provideIsProfileUsedByCell")
    void isProfileUsedByCell(EUtranCell cell, EndcDistrProfile profile, boolean expectedResult) {
        Assertions.assertEquals(expectedResult, endcDistrProfileService.isProfileUsedByCell(cell, profile));
    }

    @ParameterizedTest
    @MethodSource("provideNeedNewProfile")
    void needNewProfile(EUtranCell cell, boolean expectedResult) {
        setupRepos();
        Assertions.assertEquals(expectedResult, endcDistrProfileService.needNewProfile(cell));
    }

    @Test
    void updateExistingProfileFromProfileData() {
        List<GUtranSyncSignalFrequency> gUtranFreqRef = List.of(freq1, freq2);
        List<GUtranSyncSignalFrequency> mandatoryGUtranFreqRef = List.of(freq3, freq4);
        List<Integer> distribution = List.of(30, 20);

        EndcDistrProfile newProfile = new EndcDistrProfile();
        newProfile.setGUtranFreqRef(gUtranFreqRef);
        newProfile.setMandatoryGUtranFreqRef(mandatoryGUtranFreqRef);
        newProfile.setGUtranFreqDistribution(distribution);

        CellDataReportTuple tuple = new CellDataReportTuple(ROP_TIMESTAMP, cell1.getFdn());
        tuple.setProfileToWrite(newProfile);

        setupRepos();
        EndcDistrProfile result = endcDistrProfileService.getUpdatedProfile(cell1, tuple);

        Assertions.assertEquals(PROFILE_FDN_1, result.getFdn());
        Assertions.assertEquals(gUtranFreqRef, result.getGUtranFreqRef());
        Assertions.assertEquals(mandatoryGUtranFreqRef, result.getMandatoryGUtranFreqRef());
        Assertions.assertEquals(distribution, result.getGUtranFreqDistribution());
    }

    @Test
    void constructNewProfileFromProfileData() {
        List<GUtranSyncSignalFrequency> gUtranFreqRef = List.of(freq1, freq2);
        List<GUtranSyncSignalFrequency> mandatoryGUtranFreqRef = List.of(freq3, freq4);
        List<Integer> distribution = List.of(30, 20);

        EndcDistrProfile newProfile = new EndcDistrProfile();
        newProfile.setGUtranFreqRef(gUtranFreqRef);
        newProfile.setMandatoryGUtranFreqRef(mandatoryGUtranFreqRef);
        newProfile.setGUtranFreqDistribution(distribution);

        CellDataReportTuple tuple = new CellDataReportTuple(ROP_TIMESTAMP, cell2.getFdn());
        tuple.setProfileToWrite(newProfile);

        String newProfileFdn = ENB_FDN + ",EndcDistrProfile=rApp_2";  // Auto generated profile for Cell2.

        setupRepos();
        EndcDistrProfile result = endcDistrProfileService.getUpdatedProfile(cell2, tuple);

        Assertions.assertEquals(newProfileFdn, result.getFdn());
        Assertions.assertEquals(gUtranFreqRef, result.getGUtranFreqRef());
        Assertions.assertEquals(mandatoryGUtranFreqRef, result.getMandatoryGUtranFreqRef());
        Assertions.assertEquals(distribution, result.getGUtranFreqDistribution());
    }

    @Test
    void constructNewProfileFromProfileDataWithNameCollision() {
        List<GUtranSyncSignalFrequency> gUtranFreqRef = List.of(freq1, freq2);
        List<GUtranSyncSignalFrequency> mandatoryGUtranFreqRef = List.of(freq3, freq4);
        List<Integer> distribution = List.of(30, 20);

        EndcDistrProfile newProfile = new EndcDistrProfile();
        newProfile.setGUtranFreqRef(gUtranFreqRef);
        newProfile.setMandatoryGUtranFreqRef(mandatoryGUtranFreqRef);
        newProfile.setGUtranFreqDistribution(distribution);

        CellDataReportTuple tuple = new CellDataReportTuple(ROP_TIMESTAMP, cell2.getFdn());
        tuple.setProfileToWrite(newProfile);

        String newProfileFdn = ENB_FDN + ",EndcDistrProfile=rApp_2_2";

        setupRepos();
        cmEndcDistrProfileRepo.save(new EndcDistrProfile(ENB_FDN + ",EndcDistrProfile=rApp_2"));
        cmEndcDistrProfileRepo.save(new EndcDistrProfile(ENB_FDN + ",EndcDistrProfile=rApp_2_1"));
        EndcDistrProfile result = endcDistrProfileService.getUpdatedProfile(cell2, tuple);

        Assertions.assertEquals(newProfileFdn, result.getFdn());
        Assertions.assertEquals(gUtranFreqRef, result.getGUtranFreqRef());
        Assertions.assertEquals(mandatoryGUtranFreqRef, result.getMandatoryGUtranFreqRef());
        Assertions.assertEquals(distribution, result.getGUtranFreqDistribution());
    }

    @Test
    void getUpdatedProfileFailedWithMismatchedMapping() {
        List<GUtranSyncSignalFrequency> gUtranFreqRef = List.of(freq1, freq2);
        List<GUtranSyncSignalFrequency> mandatoryGUtranFreqRef = List.of(freq3, freq4);
        List<Integer> distribution = List.of(30);  // only one item which is different from gUtranFreqRef;

        EndcDistrProfile newProfile = new EndcDistrProfile();
        newProfile.setGUtranFreqRef(gUtranFreqRef);
        newProfile.setMandatoryGUtranFreqRef(mandatoryGUtranFreqRef);
        newProfile.setGUtranFreqDistribution(distribution);

        CellDataReportTuple tuple = new CellDataReportTuple(ROP_TIMESTAMP, cell4.getFdn());
        tuple.setProfileToWrite(newProfile);

        EndcDistrProfile result = endcDistrProfileService.getUpdatedProfile(cell4, tuple);

        Assertions.assertNull(result);
    }

    @Test
    void getUpdatedProfileFailedWithTooManyMandatoryFreq() {
        List<GUtranSyncSignalFrequency> gUtranFreqRef = List.of(freq1, freq2);
        List<GUtranSyncSignalFrequency> mandatoryGUtranFreqRef = List.of(freq1, freq2, freq3, freq4, freq5, freq6, freq7, freq8, freq9);
        List<Integer> distribution = List.of(30, 20);

        EndcDistrProfile newProfile = new EndcDistrProfile();
        newProfile.setGUtranFreqRef(gUtranFreqRef);
        newProfile.setMandatoryGUtranFreqRef(mandatoryGUtranFreqRef);
        newProfile.setGUtranFreqDistribution(distribution);

        CellDataReportTuple tuple = new CellDataReportTuple(ROP_TIMESTAMP, cell4.getFdn());
        tuple.setProfileToWrite(newProfile);

        EndcDistrProfile result = endcDistrProfileService.getUpdatedProfile(cell3, tuple);

        Assertions.assertNull(result);
    }

    @Test
    void updateProfileData() {
        cmGUtranSyncSignalFrequencyRepo.saveAll(List.of(freq1, freq2, freq3, freq4, freq5, freq6, freq7, freq8, freq9));
        cmEndcDistrProfileRepo.save(profile1);

        List<GUtranSyncSignalFrequency> gUtranFreqRef = List.of(freq1, freq2, freq3, freq4, freq5);
        List<GUtranSyncSignalFrequency> mandatoryGUtranFreqRef = List.of(freq6, freq7, freq8, freq9);
        List<Integer> distributions = List.of(10, 10, 20, 20, 20);

        EndcDistrProfile updatedProfile = new EndcDistrProfile(PROFILE_FDN_1);
        updatedProfile.setGUtranFreqRef(gUtranFreqRef);
        updatedProfile.setMandatoryGUtranFreqRef(mandatoryGUtranFreqRef);
        updatedProfile.setGUtranFreqDistribution(distributions);
        updatedProfile.setEndcUserThreshold(1000);

        endcDistrProfileService.updateProfile(updatedProfile);

        Assertions.assertEquals(1, cmEndcDistrProfileRepo.count());

        EndcDistrProfile profileInDb = cmEndcDistrProfileRepo.findById(ManagedObjectId.of(PROFILE_FDN_1)).get();
        Assertions.assertEquals(gUtranFreqRef, profileInDb.getGUtranFreqRef());
        Assertions.assertEquals(mandatoryGUtranFreqRef, profileInDb.getMandatoryGUtranFreqRef());
        Assertions.assertEquals(distributions, profileInDb.getGUtranFreqDistribution());
        Assertions.assertEquals(1000, profileInDb.getEndcUserThreshold());
    }

    private static Stream<Arguments> provideIsProfileUsedByCell() {
        return Stream.of(
            Arguments.of(cell1, profile1, true),
            Arguments.of(cell2, profile1, false),
            Arguments.of(cell2, profile2, true),
            Arguments.of(cell3, profile2, true),
            Arguments.of(cell4, profile1, false)
        );
    }

    private static Stream<Arguments> provideNeedNewProfile() {
        return Stream.of(
            Arguments.of(cell1, false),
            Arguments.of(cell2, true),
            Arguments.of(cell3, true),
            Arguments.of(cell4, true)
        );
    }

    private void setupRepos() {
        cmGUtranSyncSignalFrequencyRepo.saveAll(List.of(freq1, freq2, freq3, freq4, freq5, freq6, freq7, freq8, freq9));
        cmEndcDistrProfileRepo.saveAll(List.of(profile1, profile2));
        cmEUtranCellRepo.saveAll(List.of(cell1, cell2, cell3, cell4));
    }
}
