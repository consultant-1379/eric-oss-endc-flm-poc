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

import com.ericsson.oss.apps.model.EndcFreqProfileData;
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.entities.EutranCellLoadChange;
import com.ericsson.oss.apps.model.mom.*;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmEutranCellLoadChangeRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@Nested
@SpringBootTest
public class CalcEndcDistrProfileServiceTest {
    @Autowired
    private CalcEndcDistrProfileService calcEndcDistrProfileService;
    @Autowired
    private CmEutranCellLoadChangeRepo cmEutranCellLoadChangeRepo;
    @MockBean
    private WeightedAverageRRCService weightedAverageRRCService;
    @MockBean
    private CellCapacityService cellCapacityService;
    @MockBean
    private CellRelationService cellRelationService;
    private static final long ROP_TIME = 1234L;

    private static final String EUTRAN_CELL_FDN_1 = "eutran_cell_1";
    private static final String SYNC_SIGNAL_FREQ_FDN = "SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,GUtraNetwork=1,GUtranSyncSignalFrequency=";
    private static final String ENDC_DISTR_PROFILE_FDN = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,ENodeBFunction=1,EndcDistrProfile=";
    private static final EUtranCellFDD EUTRAN_CELL_FDD_1 = new EUtranCellFDD(EUTRAN_CELL_FDN_1);
    private static final List<SecondaryCellGroup> SECONDARY_CELL_GROUPS = List.of(new SecondaryCellGroup(new NRCellCU(), List.of(new NRCellCU()), 374567));
    private static final Map<String, List<SecondaryCellGroup>> EUTRAN_CELL_TO_SCG_MAP = Map.of(EUTRAN_CELL_FDN_1, SECONDARY_CELL_GROUPS);
    private static final List<ManagedObjectId> ALLOW_EUTRAN_CELLS = List.of(EUTRAN_CELL_FDD_1.getObjectId());
    private static Map<Integer, Float> freqToCapacityMap = new HashMap<>();
    private static Map<Integer, Float> weightedAverageRRC = new HashMap<>();
    private static Map<Integer, Integer> finalOutputMap = new HashMap<>();
    private static List<GUtranSyncSignalFrequency> gUtranSyncSignalFrequencies = new ArrayList<>();

    private static List<GUtranSyncSignalFrequency> createSyncSignalFreq(List<Integer> listOfFrequency) {

        List<GUtranSyncSignalFrequency> gUtranSyncSignalFrequencies = new ArrayList<>();

        listOfFrequency.forEach(arfcn ->
        {
            String syncSignalFdn = SYNC_SIGNAL_FREQ_FDN + arfcn.toString();
            GUtranSyncSignalFrequency syncSignalFreq = new GUtranSyncSignalFrequency(syncSignalFdn);
            syncSignalFreq.setArfcn(arfcn);
            gUtranSyncSignalFrequencies.add(syncSignalFreq);
        });
        return gUtranSyncSignalFrequencies;
    }

    private static EndcDistrProfile createEndcDistrProfile(String name, List<Integer> frequencyFR1, List<Integer> frequencyFR2, List<Integer> distribution) {

        EndcDistrProfile endcDistrProfile = new EndcDistrProfile(ENDC_DISTR_PROFILE_FDN + name);
        endcDistrProfile.setGUtranFreqRef(createSyncSignalFreq(frequencyFR1));
        endcDistrProfile.setMandatoryGUtranFreqRef(createSyncSignalFreq(frequencyFR2));
        endcDistrProfile.setGUtranFreqDistribution(distribution);
        return endcDistrProfile;
    }

    private JsonNode setUpTestFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("__files/endcDistrProfilCalc.json").getInputStream();
            return objectMapper.readTree(inputStream);
        } catch (IOException ignored) {
            return null;
        }
    }

    @BeforeEach
    public void setUp() {
        cmEutranCellLoadChangeRepo.deleteAll();
        freqToCapacityMap = new HashMap<>();
        weightedAverageRRC = new HashMap<>();
    }

    @Test
    void verifyEndcDistrProfileByCapacity() {

        Map<EUtranCell, Map<Integer, Float>> invalidWeightedRRC = Map.ofEntries(
                Map.entry(EUTRAN_CELL_FDD_1, Map.ofEntries(
                        Map.entry(2239999, Float.NaN),
                        Map.entry(2251661, Float.NaN))));
        freqToCapacityMap = Map.ofEntries(
                Map.entry(374567, 100F),
                Map.entry(483741, 200F),
                Map.entry(385475, 300F),
                Map.entry(3016669, 400F),
                Map.entry(385222, 5F)
        );
        gUtranSyncSignalFrequencies = createSyncSignalFreq(freqToCapacityMap.keySet().stream().toList());

        // Set weighted average RRC as NaN to simulate as PM data are not available
        when(weightedAverageRRCService.processWeightedAverageRRCConnUsers(ALLOW_EUTRAN_CELLS, ROP_TIME)).thenReturn(invalidWeightedRRC);
        when(cellCapacityService.processCapacityPerFrequency(EUTRAN_CELL_FDN_1, SECONDARY_CELL_GROUPS, true)).thenReturn(freqToCapacityMap);
        when(cellRelationService.listGUtranSyncSignalFreqByEUtranCell(EUTRAN_CELL_FDN_1)).thenReturn(gUtranSyncSignalFrequencies);

        Map<String, EndcFreqProfileData> eUtranCellToProfileData = calcEndcDistrProfileService.processEndcDistrProfile(ALLOW_EUTRAN_CELLS, EUTRAN_CELL_TO_SCG_MAP, true, ROP_TIME);

        final EndcFreqProfileData profileData = eUtranCellToProfileData.get(EUTRAN_CELL_FDN_1);
        //ARFCN 374567 havs capacity in percentage of 100 / (100+200+300+5) * 100 = 16.528
        Assertions.assertEquals(16, profileData.freqDistributionMapping().get(SYNC_SIGNAL_FREQ_FDN + "374567"));
        //ARFCN 483741 havs capacity in percentage of 200 / (100+200+300+5) * 100 = 33.057
        Assertions.assertEquals(33, profileData.freqDistributionMapping().get(SYNC_SIGNAL_FREQ_FDN + "483741"));
        //ARFCN 385475 havs capacity in percentage of 300 / (100+200+300+5) * 100 = 49.586
        Assertions.assertEquals(49, profileData.freqDistributionMapping().get(SYNC_SIGNAL_FREQ_FDN + "385475"));
        //ARFCN 385222 havs capacity in percentage of 5 / (100+200+300+5) * 100 = 0.826
        Assertions.assertEquals(2, profileData.freqDistributionMapping().get(SYNC_SIGNAL_FREQ_FDN + "385222"));
        Assertions.assertEquals(4, profileData.gUtranFreqRef().size());
        Assertions.assertEquals(1, profileData.mandatoryGUtranFreqRef().size());

    }

    @Test
    void processEndcDistrProfileTest() {
        // Now, load in real PM data
        Objects.requireNonNull(setUpTestFile()).get("eutran_cell_first").forEach(object -> {
            Integer arfcn = Integer.parseInt(object.get("ssbFrequency").toString());
            Float cellCapacity = Float.parseFloat(object.get("Cell Capacity or BW").toString());
            Integer finalOutput = Integer.parseInt(object.get("Final Output").toString());
            Float weightedRRC = Float.parseFloat(object.get("Weighted Average RRC Connected Users").toString());

            freqToCapacityMap.put(arfcn, cellCapacity);
            weightedAverageRRC.put(arfcn, weightedRRC);
            finalOutputMap.put(arfcn, finalOutput);
        });

        Map<EUtranCell, Map<Integer, Float>> invalidWeightedRRC = Map.ofEntries(
                Map.entry(EUTRAN_CELL_FDD_1, Map.ofEntries(
                        Map.entry(2239999, Float.NaN))));

        gUtranSyncSignalFrequencies = createSyncSignalFreq(freqToCapacityMap.keySet().stream().toList());

        /*
          First ROP starts with mandatory mode
          There are 6 FR2 and 2 FR1, the first ROP starts with initialization mode
          Only CM data, PM data not available, so invalid Weighted average RRC will return
          gUtranFreqDistribution = [63,37]
          gUtranFreqRef = [653952, 648672]
          mandatoryGUtranFreqRef = [2239999, 2251661, 2083329, 2071667]
         */
        // Set weighted average RRC as NaN to simulate as PM data are not available
        when(weightedAverageRRCService.processWeightedAverageRRCConnUsers(ALLOW_EUTRAN_CELLS, ROP_TIME)).thenReturn(invalidWeightedRRC);
        when(cellCapacityService.processCapacityPerFrequency(EUTRAN_CELL_FDN_1, SECONDARY_CELL_GROUPS, true)).thenReturn(freqToCapacityMap);
        when(cellRelationService.listGUtranSyncSignalFreqByEUtranCell(EUTRAN_CELL_FDN_1)).thenReturn(gUtranSyncSignalFrequencies);

        Map<String, EndcFreqProfileData> eUtranCellToProfileData = calcEndcDistrProfileService.processEndcDistrProfile(ALLOW_EUTRAN_CELLS, EUTRAN_CELL_TO_SCG_MAP, true, ROP_TIME);

        EndcFreqProfileData profileData1 = eUtranCellToProfileData.get(EUTRAN_CELL_FDN_1);
        //ARFCN 653952 havs capacity in percentage of 5.7 / (5.7+3.4) * 100 = 62.63%
        Assertions.assertEquals(63, profileData1.freqDistributionMapping().get(SYNC_SIGNAL_FREQ_FDN + "653952"));
        //ARFCN 648672 havs capacity in percentage of 3.4 / (5.7+3.4) * 100 = 37.36
        Assertions.assertEquals(37, profileData1.freqDistributionMapping().get(SYNC_SIGNAL_FREQ_FDN + "648672"));
        Assertions.assertEquals(2, profileData1.gUtranFreqRef().size());
        Assertions.assertEquals(4, profileData1.mandatoryGUtranFreqRef().size());
        // cmEutranCellLoadChangeRepo not yet created for EUTRAN_CELL_FDD_1
        Assertions.assertTrue(cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).isEmpty());
        // Before 2nd Rop, EndcFreqProfileData is processed and saved in CM, now EUTRAN_CELL_FDD_1 should have latest EndcDistrProfile
        EndcDistrProfile endcDistrProfile = createEndcDistrProfile("endc-distr-profile-1", List.of(653952, 648672), List.of(2239999, 2251661, 2083329, 2071667), List.of(63, 37));
        EUTRAN_CELL_FDD_1.setEndcDistrProfileRef(endcDistrProfile);

        /*
            Second ROP, mandatory mode
            PM Data are available
            cmEutranCellLoadChangeRepo has size 1 from previous ROP
            gUtranFreqDistribution = [x,x,x,x,x,x]
            gUtranFreqRef = [653952, 648672, 2239999, 2251661, 2083329, 2071667]
            mandatoryGUtranFreqRef = [2239999, 2251661, 2083329, 2071667]
         */
        when(weightedAverageRRCService.processWeightedAverageRRCConnUsers(ALLOW_EUTRAN_CELLS, ROP_TIME + 1)).thenReturn(Map.of(EUTRAN_CELL_FDD_1, weightedAverageRRC));
        eUtranCellToProfileData = calcEndcDistrProfileService.processEndcDistrProfile(ALLOW_EUTRAN_CELLS, EUTRAN_CELL_TO_SCG_MAP, true, ROP_TIME + 1);

        EndcFreqProfileData profileData2 = eUtranCellToProfileData.get(EUTRAN_CELL_FDN_1);
        // After this 2nd ROP, load change in percentage for both FR1 and FR2 are saved in cmEutranCellLoadChangeRepo
        Assertions.assertEquals(6, profileData2.gUtranFreqRef().size());
        Assertions.assertEquals(4, profileData2.mandatoryGUtranFreqRef().size());
        Assertions.assertTrue(cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).isPresent());
        Assertions.assertEquals(1, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR1().size());
        Assertions.assertEquals(1, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR2().size());

        profileData2.freqDistributionMapping().forEach((fdn, value) -> {
            Integer arfcn = Integer.valueOf(fdn.substring(fdn.lastIndexOf("=") + 1));
            Assertions.assertEquals(finalOutputMap.get(arfcn), value);
        });
        // Before 3rd Rop, EndcFreqProfileData is processed and saved in CM, now EUTRAN_CELL_FDD_1 should have latest EndcDistrProfile
        endcDistrProfile = createEndcDistrProfile("endc-distr-profile-1", List.of(653952, 648672), List.of(2239999, 2251661, 2083329, 2071667), List.of(8, 8, 21, 25, 16, 22));
        EUTRAN_CELL_FDD_1.setEndcDistrProfileRef(endcDistrProfile);

        /*
            3rd ROP, mandatory mode
            PM Data are available
            cmEutranCellLoadChangeRepo has size 2 from previous ROP
            gUtranFreqDistribution = [x,x,x,x,x,x]
            gUtranFreqRef = [653952, 648672, 2239999, 2251661, 2083329, 2071667]
            mandatoryGUtranFreqRef = [2239999, 2251661, 2083329, 2071667]
         */
        Objects.requireNonNull(setUpTestFile()).get("eutran_cell_second").forEach(object -> {
            Integer arfcn = Integer.parseInt(object.get("ssbFrequency").toString());
            Float cellCapacity = Float.parseFloat(object.get("Cell Capacity or BW").toString());
            Integer finalOutput = Integer.parseInt(object.get("Final Output").toString());
            Float weightedRRC = Float.parseFloat(object.get("Weighted Average RRC Connected Users").toString());

            freqToCapacityMap.put(arfcn, cellCapacity);
            weightedAverageRRC.put(arfcn, weightedRRC);
            finalOutputMap.put(arfcn, finalOutput);
        });
        when(weightedAverageRRCService.processWeightedAverageRRCConnUsers(ALLOW_EUTRAN_CELLS, ROP_TIME + 2)).thenReturn(Map.of(EUTRAN_CELL_FDD_1, weightedAverageRRC));
        when(cellCapacityService.processCapacityPerFrequency(EUTRAN_CELL_FDN_1, SECONDARY_CELL_GROUPS, true)).thenReturn(freqToCapacityMap);
        eUtranCellToProfileData = calcEndcDistrProfileService.processEndcDistrProfile(ALLOW_EUTRAN_CELLS, EUTRAN_CELL_TO_SCG_MAP, true, ROP_TIME + 2);

        EndcFreqProfileData profileData3 = eUtranCellToProfileData.get(EUTRAN_CELL_FDN_1);
        // After this 3rd ROP, load change in percentage for both FR1 and FR2 are saved in cmEutranCellLoadChangeRepo
        Assertions.assertEquals(6, profileData3.gUtranFreqRef().size());
        Assertions.assertEquals(4, profileData3.mandatoryGUtranFreqRef().size());
        Assertions.assertTrue(cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).isPresent());
        Assertions.assertEquals(2, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR1().size());
        Assertions.assertEquals(2, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR2().size());

        profileData3.freqDistributionMapping().forEach((fdn, value) -> {
            Integer arfcn = Integer.valueOf(fdn.substring(fdn.lastIndexOf("=") + 1));
            Assertions.assertEquals(finalOutputMap.get(arfcn), value);
        });

        /*
            4th ROP
            Distribution mode by hardcode setting
            all FR1 in cmEutranCellLoadChangeRepo to > 1 %, and all FR2 in cmEutranCellLoadChangeRepo to < 1 %
            before 3rd Rop, EndcFreqProfileData is processed and saved in CM, now EUTRAN_CELL_FDD_1 should have latest EndcDistrProfile
            PM Data are available
            gUtranFreqDistribution = [x,x]
            gUtranFreqRef = [653952, 648672]
            mandatoryGUtranFreqRef = []
         */
        Optional<EutranCellLoadChange> eutranCellLoadChange = cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId());
        Assertions.assertTrue(eutranCellLoadChange.isPresent());
        eutranCellLoadChange.get().setLoadChangeFR1(List.of(List.of(2F, 2F), List.of(2F, 2F), List.of(43F, 23F)));
        eutranCellLoadChange.get().setLoadChangeFR2(List.of(List.of(-2F, 2F), List.of(2F, 2F), List.of(-43F, 23F)));
        cmEutranCellLoadChangeRepo.saveAndFlush(eutranCellLoadChange.get());

        when(weightedAverageRRCService.processWeightedAverageRRCConnUsers(ALLOW_EUTRAN_CELLS, ROP_TIME + 3)).thenReturn(Map.of(EUTRAN_CELL_FDD_1, weightedAverageRRC));
        eUtranCellToProfileData = calcEndcDistrProfileService.processEndcDistrProfile(ALLOW_EUTRAN_CELLS, EUTRAN_CELL_TO_SCG_MAP, true, ROP_TIME + 3);

        EndcFreqProfileData profileData4 = eUtranCellToProfileData.get(EUTRAN_CELL_FDN_1);
        Assertions.assertEquals(6, profileData4.gUtranFreqRef().size());
        Assertions.assertEquals(0, profileData4.mandatoryGUtranFreqRef().size());
        Assertions.assertTrue(cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).isPresent());
        Assertions.assertEquals(3, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR1().size());
        Assertions.assertEquals(3, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR2().size());
    }

    @ParameterizedTest
    @MethodSource("provideDataToCreateEndcFreqProfile")
    void createEndcProfileDataTest(Map<Integer, Float> freqToFinalDistrMap, List<Integer> availableSyncSignalFreqs, boolean exceptionOccurred) {

        boolean actualExceptionOccurred = false;
        try {
            gUtranSyncSignalFrequencies = createSyncSignalFreq(availableSyncSignalFreqs);
            when(cellRelationService.listGUtranSyncSignalFreqByEUtranCell(EUTRAN_CELL_FDN_1)).thenReturn(gUtranSyncSignalFrequencies);

            EndcFreqProfileData eUtranCellToProfileData = calcEndcDistrProfileService.createEndcProfileData(EUTRAN_CELL_FDN_1, freqToFinalDistrMap, false, true);
            Assertions.assertEquals(availableSyncSignalFreqs.size(), eUtranCellToProfileData.freqDistributionMapping().size());
        } catch (Exception e) {
            actualExceptionOccurred = true;
        }
        Assertions.assertEquals(exceptionOccurred, actualExceptionOccurred);
    }

    private static Stream<Arguments> provideDataToCreateEndcFreqProfile() {
        Map<Integer, Float> freqToCapacityMap_1 = Map.ofEntries(
                Map.entry(3016670, 100F),
                Map.entry(3016671, 400F),
                Map.entry(3016672, 500F),
                Map.entry(3016673, 600F),
                Map.entry(3016674, 700F),
                Map.entry(3016675, 800F),
                Map.entry(3016676, 900F),
                Map.entry(3016677, 1000F),
                Map.entry(3016669, 1100F),
                Map.entry(374567, 1200F),
                Map.entry(385230, 1300F),
                Map.entry(385231, 140F),
                Map.entry(385232, 150F),
                Map.entry(385233, 100F),
                Map.entry(385234, 100F),
                Map.entry(385235, 100F)
        );
        Map<Integer, Float> freqToCapacityMap_2 = Map.ofEntries(
                Map.entry(3016671, 400F),
                Map.entry(3016672, 500F),
                Map.entry(3016673, 600F),
                Map.entry(3016674, 700F),
                Map.entry(3016675, 800F),
                Map.entry(3016676, 900F),
                Map.entry(385227, 1000F),
                Map.entry(3036669, 1100F),
                Map.entry(385229, 1200F),
                Map.entry(385230, 1300F),
                Map.entry(385231, 140F),
                Map.entry(385232, 150F),
                Map.entry(385233, 100F),
                Map.entry(385234, 100F),
                Map.entry(385235, 100F),
                Map.entry(385236, 100F),
                Map.entry(385237, 100F),
                Map.entry(385238, 100F),
                Map.entry(385239, 100F)
        );

        return Stream.of(
                // Available gUtranSyncSignalFrequencies 374567, 3016669 are not matched with freqToCapacityMap, so frequency that cannot find will be skipped
                Arguments.of(freqToCapacityMap_1, List.of(374567, 3016669), false),
                // gUtranFreqRef will be size of 16 but mandatoryGUtranFreqRef's size will be more than 8, exception will be thrown
                Arguments.of(freqToCapacityMap_1, freqToCapacityMap_1.keySet().stream().toList(), true),
                // gUtranFreqRef's size will be more than 16, exception will be thrown
                Arguments.of(freqToCapacityMap_2, freqToCapacityMap_2.keySet().stream().toList(), true));
    }

    @Test
    void loadChangeInPercentTest() {
        weightedAverageRRC = Map.ofEntries(
                Map.entry(2239999, 35F),
                Map.entry(2251661, 30F),
                Map.entry(2083329, 45.3F),
                Map.entry(2071667, 34.4F),
                Map.entry(653952, 20F),
                Map.entry(648672, 7F)
        );
        freqToCapacityMap = Map.ofEntries(
                Map.entry(2239999, 22.7F),
                Map.entry(2251661, 22.7F),
                Map.entry(2083329, 22.7F),
                Map.entry(2071667, 22.7F),
                Map.entry(653952, 5.7F),
                Map.entry(648672, 3.4F));

        Map<Integer, Float> expectedLoadChange = Map.ofEntries(
                Map.entry(2239999, 11.5F),
                Map.entry(2251661, 30.1F),
                Map.entry(2083329, -13.9F),
                Map.entry(2071667, 13.4F),
                Map.entry(653952, -51.2F),
                Map.entry(648672, -16.4F));

        Map<Integer, Float> result = calcEndcDistrProfileService.loadChangeInPercent(weightedAverageRRC, freqToCapacityMap);

        result.forEach((arfcn, loadChange) ->
        {
            float diffByPercent = Math.abs(expectedLoadChange.get(arfcn) - loadChange) / Math.abs(expectedLoadChange.get(arfcn)) * 100;
            // As long as the difference in percentage is less than 1.3%, consider pass
            Assertions.assertTrue(diffByPercent < 1.3F);
        });
    }

    @ParameterizedTest
    @MethodSource("provideFreqDistributionDecimal")
    void convertPercentageToIntegerTest(Map<String, Float> freqDistributionDecimal) {
        Map<String, Integer> result = calcEndcDistrProfileService.convertPercentageToInteger(freqDistributionDecimal);

        Assertions.assertEquals(100, result.values().stream().reduce(0, Integer::sum));
        Assertions.assertEquals(0, result.values().stream().filter(value -> value < 1).toList().size());
    }

    private static Stream<Arguments> provideFreqDistributionDecimal() {
        // after rounding down, the sum is 12+8+30+9+38=97
        Map<String, Float> freqDistr_1 = Map.ofEntries(
                Map.entry("cell_1", 12.3F),
                Map.entry("cell_2", 8.9F),
                Map.entry("cell_3", 30.3F),
                Map.entry("cell_4", 9.83F),
                Map.entry("cell_5", 38.67F)
        );
        // after rounding down, the sum is 1+1+1+1+98=102
        Map<String, Float> freqDistr_2 = Map.ofEntries(
                Map.entry("cell_1", 0.3F),
                Map.entry("cell_2", 0.1F),
                Map.entry("cell_3", 0.1F),
                Map.entry("cell_4", 0.2F),
                Map.entry("cell_5", 98.3F)
        );
        return Stream.of(
                Arguments.of(freqDistr_1),
                Arguments.of(freqDistr_2)
        );
    }

    @ParameterizedTest
    @MethodSource("provideLoadChange")
    void isMandatoryModeAndUpdateTest(List<List<Float>> allLoadChangeFR1, List<List<Float>> allLoadChangeFR2, boolean isMandatoryMode) {

        Map<Integer, Float> currentLoadChange = Map.ofEntries(
                Map.entry(2239999, 11.5F),
                Map.entry(648672, 16.4F));

        if (allLoadChangeFR1.isEmpty()) {
            // this simulates the initial stage where past load changes are not in CM, currentLoadChange will be saved into CM and set default mode to mandatory
            Assertions.assertTrue(calcEndcDistrProfileService.isMandatoryModeAndUpdate(EUTRAN_CELL_FDD_1.getObjectId(), currentLoadChange));
            Assertions.assertTrue(cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).isPresent());
            Assertions.assertEquals(1, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR1().size());
            Assertions.assertEquals(1, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR2().size());
        } else {
            EutranCellLoadChange eutranCellLoadChange = new EutranCellLoadChange(EUTRAN_CELL_FDD_1.getObjectId());
            eutranCellLoadChange.setLoadChangeFR1(allLoadChangeFR1);
            eutranCellLoadChange.setLoadChangeFR2(allLoadChangeFR2);
            cmEutranCellLoadChangeRepo.saveAndFlush(eutranCellLoadChange);

            Assertions.assertEquals(isMandatoryMode, calcEndcDistrProfileService.isMandatoryModeAndUpdate(EUTRAN_CELL_FDD_1.getObjectId(), currentLoadChange));
            Assertions.assertTrue(cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).isPresent());
            Assertions.assertEquals(3, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR1().size());
            Assertions.assertEquals(3, cmEutranCellLoadChangeRepo.findById(EUTRAN_CELL_FDD_1.getObjectId()).get().getLoadChangeFR2().size());
        }
    }

    private static Stream<Arguments> provideLoadChange() {

        return Stream.of(
                Arguments.of(List.of(), List.of(), true),
                // allLoadChangeFR1 insufficient data, allLoadChangeFR2 insufficient data, set true
                Arguments.of(List.of(List.of(-51.2F, -16.4F), List.of(11.5F, 30.1F)), List.of(List.of(11.5F, 30.1F, -13.9F, 13.4F), List.of(-51.2F, -16.4F)), true),
                // allLoadChangeFR1 sufficient data but not all > 1%, allLoadChangeFR2 sufficient data and all > 1%, set true
                Arguments.of(List.of(List.of(-51.2F, -16.4F), List.of(11.5F, 30.1F), List.of(11.5F, 30.1F)), List.of(List.of(11.5F, 30.1F), List.of(11.5F, 30.1F), List.of(51.2F, 16.4F)), true),
                // allLoadChangeFR1 sufficient data and all > 1%, allLoadChangeFR2 sufficient data but not all > 1%, set false
                Arguments.of(List.of(List.of(1.2F, 16.4F), List.of(11.5F, 30.1F), List.of(11.5F, 30.1F)), List.of(List.of(11.5F, -30.1F), List.of(-11.5F, 30.1F), List.of(51.2F, 16.4F)), false),
                // allLoadChangeFR1 all > 1%, allLoadChangeFR2 all > 1%, both modes are trigger but set to mandatory as default
                Arguments.of(List.of(List.of(1.2F, 16.4F), List.of(11.5F, 30.1F), List.of(11.5F, 30.1F)), List.of(List.of(11.5F, 30.1F), List.of(11.5F, 30.1F), List.of(51.2F, 16.4F)), true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEndcDistrProfile")
    void getPreviousDistributionTest(EndcDistrProfile endcDistrProfile, Map<Integer, Integer> expectPreviousDistribution) {

        Assertions.assertEquals(expectPreviousDistribution, calcEndcDistrProfileService.getPreviousDistribution(endcDistrProfile));
    }

    private static Stream<Arguments> provideEndcDistrProfile() {

        EndcDistrProfile endcDistrProfile1 = createEndcDistrProfile("test1", List.of(448672, 648672, 223999, 2239999, 2067732, 6486727), List.of(2239999, 2067732, 6486727), List.of(30, 30, 10, 10, 10, 10));
        Map<Integer, Integer> previousDistributionMap1 = Map.ofEntries(
                Map.entry(448672, 30),
                Map.entry(648672, 30),
                Map.entry(223999, 10),
                Map.entry(2239999, 10),
                Map.entry(2067732, 10),
                Map.entry(6486727, 10)
        );
        EndcDistrProfile endcDistrProfile2 = createEndcDistrProfile("test2", List.of(448672, 648672, 223999), List.of(2239999, 2067732, 6486727), List.of(30, 30, 60));
        Map<Integer, Integer> previousDistributionMap2 = Map.ofEntries(
                Map.entry(448672, 30),
                Map.entry(648672, 30),
                Map.entry(223999, 60)
        );

        EndcDistrProfile endcDistrProfile3 = createEndcDistrProfile("test3", List.of(448672, 648672, 223999), List.of(), List.of(30, 30, 60));
        Map<Integer, Integer> previousDistributionMap3 = Map.ofEntries(
                Map.entry(448672, 30),
                Map.entry(648672, 30),
                Map.entry(223999, 60)
        );
        return Stream.of(
                Arguments.of(new EndcDistrProfile(), new HashMap<>()),
                Arguments.of(null, new HashMap<>()),
                Arguments.of(endcDistrProfile1, previousDistributionMap1),
                Arguments.of(endcDistrProfile2, previousDistributionMap2),
                Arguments.of(endcDistrProfile3, previousDistributionMap3));
    }
}
