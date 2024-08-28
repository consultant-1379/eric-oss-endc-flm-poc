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
import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.mom.EndcDistrProfile;
import com.ericsson.oss.apps.model.mom.GUtranSyncSignalFrequency;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmEutranCellLoadChangeRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalcEndcDistrProfileService {

    private static final Integer MIN_FR2_FREQ = 2016667;
    private static final Integer GUTRAN_FREQ_REF_SIZE = 16;
    private static final Integer MANDATORY_GUTRAN_FREQ_REF_SIZE = 8;
    private final CellCapacityService cellCapacityService;
    private final CellRelationService cellRelationService;
    private final WeightedAverageRRCService weightedAverageRRCService;
    private final CmEutranCellLoadChangeRepo cmEutranCellLoadChangeRepo;

    public Map<String, EndcFreqProfileData> processEndcDistrProfile(List<ManagedObjectId> allowEutranCells,
                                                                    Map<String, List<SecondaryCellGroup>> eUtranCellToSCGsMap,
                                                                    boolean useCellWeight,
                                                                    long ropTime) {

        Map<String, EndcFreqProfileData> eUtranCellToProfileData = calcEndcProfileByRRC(allowEutranCells, eUtranCellToSCGsMap, ropTime, useCellWeight);

        if (eUtranCellToProfileData.isEmpty()) {
            log.info("PM data not available, using CM data");
            return calcEndcProfileByCapacity(eUtranCellToSCGsMap, useCellWeight);
        }
        log.info("PM data available");
        return eUtranCellToProfileData;
    }

    public Map<String, EndcFreqProfileData> calcEndcProfileByRRC(List<ManagedObjectId> allowEutranCells,
                                                                 Map<String, List<SecondaryCellGroup>> eUtranCellToSCGsMap,
                                                                 long ropTime,
                                                                 boolean useCellWeight) {
        Map<EUtranCell, Map<Integer, Float>> weightedAverageRRCMap = weightedAverageRRCService.processWeightedAverageRRCConnUsers(allowEutranCells, ropTime);
        Map<String, EndcFreqProfileData> eUtranCellToProfileData = new HashMap<>();

        if (isPMDataAvail(weightedAverageRRCMap)) {
            // Below for loop iterate each eUtran cell, and generate EndcFreqProfileData separately
            weightedAverageRRCMap.forEach((cell, weightedRRC) ->
            {
                List<SecondaryCellGroup> secondaryCellGroups = eUtranCellToSCGsMap.get(cell.getFdn());
                // Mapping from frequency to cell capacity
                Map<Integer, Float> frequencyToCapacityMap = cellCapacityService.processCapacityPerFrequency(cell.getFdn(), secondaryCellGroups, useCellWeight);
                // Mapping from frequency to load change
                Map<Integer, Float> loadChangeInPercent = loadChangeInPercent(weightedRRC, frequencyToCapacityMap);
                // Mapping from frequency to previous distribution in %
                Map<Integer, Integer> previousDistribution = getPreviousDistribution(cell.getEndcDistrProfileRef());
                // Mapping from frequency to new distribution in %
                Map<Integer, Float> newDistributionInPercent = getNewDistribution(frequencyToCapacityMap, loadChangeInPercent, previousDistribution);

                boolean isMandatoryMode = isMandatoryModeAndUpdate(cell.getObjectId(), loadChangeInPercent);
                Map<Integer, Float> scaledOutput = getScaledOutput(isMandatoryMode, weightedRRC, newDistributionInPercent);

                log.debug("frequencyToCapacityMap {}, loadChangeInPercent {}, newDistributionInPercent {} isMandatoryMode {} scaledOutput {}",
                        frequencyToCapacityMap, loadChangeInPercent, newDistributionInPercent, isMandatoryMode, scaledOutput);

                try {
                    String cellFdn = cell.getFdn();
                    eUtranCellToProfileData.put(cellFdn, createEndcProfileData(cellFdn, scaledOutput, false, isMandatoryMode));
                } catch (Exception e) {
                    log.error("Total percentage over 100% or gUtranFreqRef over 16", e);
                }
            });
        }
        return eUtranCellToProfileData;
    }

    public Map<String, EndcFreqProfileData> calcEndcProfileByCapacity(Map<String, List<SecondaryCellGroup>> eUtranCellToSCGsMap,
                                                                      boolean useCellWeight) {
        Map<String, EndcFreqProfileData> eUtranCellToProfileData = new HashMap<>();
        eUtranCellToSCGsMap.forEach((eutranCellFdn, secondaryCellGroups) ->
        {
            Map<Integer, Float> frequencyToCapacityMap = cellCapacityService.processCapacityPerFrequency(eutranCellFdn, secondaryCellGroups, useCellWeight);

            try {
                eUtranCellToProfileData.put(eutranCellFdn, createEndcProfileData(eutranCellFdn, frequencyToCapacityMap, true, false));
            } catch (Exception e) {
                log.error("Total percentage over 100% or gUtranFreqRef over 16", e);
            }
        });

        return eUtranCellToProfileData;
    }

    public EndcFreqProfileData createEndcProfileData(String eutranCellFdn, Map<Integer, Float> freqToFinalDistrMap, boolean isInitial, boolean isMandatoryMode) throws Exception {

        Map<String, Float> freqToDistrDecimal = new HashMap<>();
        List<GUtranSyncSignalFrequency> gUtranFreqRef = new ArrayList<>();
        List<GUtranSyncSignalFrequency> mandatoryGUtranFreqRef = new ArrayList<>();

        Map<Integer, GUtranSyncSignalFrequency> freqToGutranSyncSignalMap =
                cellRelationService.listGUtranSyncSignalFreqByEUtranCell(eutranCellFdn).stream()
                        .collect(Collectors.toMap(
                                GUtranSyncSignalFrequency::getArfcn,
                                syncSignalfreq -> syncSignalfreq));

        freqToFinalDistrMap.forEach((arfcn, finalDistr) ->
        {
            if (!freqToGutranSyncSignalMap.containsKey(arfcn)) {
                log.error("arfcn key {} cannot be found, capacity {}", arfcn, finalDistr);
                return;
            }
            GUtranSyncSignalFrequency gUtranSyncSignalFrequency = freqToGutranSyncSignalMap.get(arfcn);
            /*
            Initial mode: FR1 frequencies in gUtranFreqRef, and FR2 frequencies in mandatoryGUtranFreqRef
            Mandatory mode: FR1 and FR2 frequencies in gUtranFreqRef, FR2 frequencies in gUtranFreqRef
            Distribution mode: FR1 and FR2 frequencies in gUtranFreqRef, and mandatoryGUtranFreqRef sets empty
             */
            if (isInitial && arfcn >= MIN_FR2_FREQ) {
                mandatoryGUtranFreqRef.add(gUtranSyncSignalFrequency);
            } else if (isInitial || (isMandatoryMode && arfcn >= MIN_FR2_FREQ)) {
                gUtranFreqRef.add(gUtranSyncSignalFrequency);
                freqToDistrDecimal.put(gUtranSyncSignalFrequency.getObjectId().toString(), finalDistr);
                if (arfcn >= MIN_FR2_FREQ) {
                    mandatoryGUtranFreqRef.add(gUtranSyncSignalFrequency);
                }
            } else {
                gUtranFreqRef.add(gUtranSyncSignalFrequency);
                freqToDistrDecimal.put(gUtranSyncSignalFrequency.getObjectId().toString(), finalDistr);
            }
            log.debug("arfcn {} capacity {}", arfcn, finalDistr);
        });

        log.debug("eutranCell fdn {} freqToDistrDecimal {} gUtranFreqRef {} mandatoryGUtranFreqRef {} GUtranSyncSignalFrequency {} isMandatoryMode {}",
                eutranCellFdn, freqToDistrDecimal.size(), gUtranFreqRef.size(), mandatoryGUtranFreqRef.size(), freqToGutranSyncSignalMap.size(), isMandatoryMode);

        Float totalDistribution = freqToDistrDecimal.values().stream().reduce(0F, Float::sum);
        // percentage in decimals
        freqToDistrDecimal.replaceAll((fdn, value) -> value / totalDistribution * 100);
        Map<String, Integer> freqToDistrInteger = convertPercentageToInteger(freqToDistrDecimal);

        if (freqToDistrInteger.values().stream().reduce(0, Integer::sum) != 100) {
            throw new IllegalArgumentException();
        }
        if (gUtranFreqRef.size() > GUTRAN_FREQ_REF_SIZE) {
            throw new IllegalArgumentException();
        }
        if (mandatoryGUtranFreqRef.size() > MANDATORY_GUTRAN_FREQ_REF_SIZE) {
            throw new IllegalArgumentException();
        }

        return new EndcFreqProfileData(freqToDistrInteger, gUtranFreqRef, mandatoryGUtranFreqRef);
    }

    public Map<String, Integer> convertPercentageToInteger(Map<String, Float> freqToDistrDecimal) {

        Map<String, Integer> freqToDistrInteger = freqToDistrDecimal.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        // minimum of 1% is required
                        entry -> Math.max(1, (int) Math.floor(entry.getValue()))));

        // After rounding and setting minimum value as 1, total percentage can be more or less than 100.
        int diff = 100 - freqToDistrInteger.values().stream().reduce(0, Integer::sum);

        // This is sorted in descending order using value after decimal point, for example [3.9, 12,5, 1,1]
        List<String> sortedDistrByDecimal = freqToDistrDecimal.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparing((Float value) -> 1 - (value % 1))))
                .map(Map.Entry::getKey)
                .toList()
                .subList(0, Math.abs(diff));

        // This is sorted in descending order using value after decimal point, for example [12,5, 3.9, 1,1]
        List<String> sortedDistrByInteger = freqToDistrDecimal.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .toList();

        if (diff > 0) {
            sortedDistrByDecimal.forEach(fdn -> freqToDistrInteger.merge(fdn, 1, Integer::sum));
        } else if (diff < 0) {
            // if diff < 0, then we have to take away some percentages away
            // To avoid making percentage like 1% back to 0, only take away from the largest percentage
            freqToDistrInteger.merge(sortedDistrByInteger.get(0), diff, Integer::sum);
        }
        return freqToDistrInteger;
    }

    private boolean isPMDataAvail(Map<EUtranCell, Map<Integer, Float>> weightedAverageRRC) {
        // as default, pm counters hold value of NaN
        // after collecting values that are not NaN in weightedAverageRRC, if list is not empty, then PM data are available
        return !weightedAverageRRC.values().stream()
                .flatMap(entry -> entry.values().stream())
                .filter(value -> !Float.isNaN(value))
                .toList().isEmpty();
    }

    public Map<Integer, Float> loadChangeInPercent(Map<Integer, Float> weightedAverageRRC,
                                                   Map<Integer, Float> cellCapacity) {

        float totalWeightedRRC = weightedAverageRRC.values().stream().reduce(0F, Float::sum);
        // proportional Weighted RRC = Cell Capaity(%) / 100 * Total Weighted Average RRC
        Map<Integer, Float> proportionalWeightedRRC = cellCapacity.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() / 100 * totalWeightedRRC
                ));
        // load change In Percent = (proportional - original weighted RRC) / original weighted RRC
        // can be negative values
        return weightedAverageRRC.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (proportionalWeightedRRC.get(entry.getKey()) - entry.getValue()) / entry.getValue() * 100
                ));
    }

    public boolean isMandatoryModeAndUpdate(ManagedObjectId objectId, Map<Integer, Float> loadChange) {
        // Filter and map load change values for FR2 frequencies.
        List<Float> newLoadChangeFR2 = loadChange.entrySet().stream()
                .filter(entry -> entry.getKey() >= MIN_FR2_FREQ)
                .map(Map.Entry::getValue)
                .toList();

        // Filter and map load change values for FR1 frequencies.
        List<Float> newLoadChangeFR1 = loadChange.entrySet().stream()
                .filter(entry -> entry.getKey() < MIN_FR2_FREQ)
                .map(Map.Entry::getValue)
                .toList();

        // Retrieve the EutranCellLoadChange object by ID.
        Optional<EutranCellLoadChange> eutranCellLoadChange = cmEutranCellLoadChangeRepo.findById(objectId);

        if (eutranCellLoadChange.isPresent()) {
            boolean distributionMode = false;
            boolean mandatoryMode = false;
            int numRopTimeToStore = eutranCellLoadChange.get().getNumRopTimeToStore();
            List<List<Float>> allLoadChangeFR1 = eutranCellLoadChange.get().getLoadChangeFR1();
            List<List<Float>> allLoadChangeFR2 = eutranCellLoadChange.get().getLoadChangeFR2();

            // the List has enough past load change data for FR1, if not true, then data is not insufficient
            if (allLoadChangeFR1.size() == numRopTimeToStore) {
                // check all FR1 that have load change > 1%
                distributionMode = allLoadChangeFR1.stream()
                        .flatMap(Collection::stream)
                        .filter(each -> each <= 1)
                        .toList().isEmpty();
                // remove oldest data and add new data
                allLoadChangeFR1.remove(0);
            }

            // Update the load change values for FR2 frequencies, if not true, then data is not insufficient
            if (allLoadChangeFR2.size() == numRopTimeToStore) {
                // check all FR2 that have load change > 1%
                mandatoryMode = allLoadChangeFR2.stream()
                        .flatMap(Collection::stream)
                        .filter(each -> each <= 1)
                        .toList().isEmpty();
                // remove oldest data and add new data
                allLoadChangeFR2.remove(0);
                allLoadChangeFR2.add(newLoadChangeFR2);
            }

            allLoadChangeFR1.add(newLoadChangeFR1);
            allLoadChangeFR2.add(newLoadChangeFR2);

            // Save the updated EutranCellLoadChange object.
            eutranCellLoadChange.get().setLoadChangeFR1(allLoadChangeFR1);
            eutranCellLoadChange.get().setLoadChangeFR2(allLoadChangeFR2);
            cmEutranCellLoadChangeRepo.save(eutranCellLoadChange.get());

            // when both FR1 and FR2 have insufficient data, set to mandatory mode
            if (!distributionMode && !mandatoryMode) {
                return true;
            }

            // Return true if both modes have the same value.
            if (distributionMode == mandatoryMode) {
                log.error("Both FR1 and FR2 have load change > 1%, setting to default mandatory mode");
                return true;
            }

            // Return the value of mandatoryMode.
            return mandatoryMode;

        } else {
            // this is initial stage, Create a new EutranCellLoadChange object and save it.
            EutranCellLoadChange newEutranCellLoadChange = new EutranCellLoadChange(objectId);
            newEutranCellLoadChange.setLoadChangeFR1(List.of(newLoadChangeFR1));
            newEutranCellLoadChange.setLoadChangeFR2(List.of(newLoadChangeFR2));
            cmEutranCellLoadChangeRepo.save(newEutranCellLoadChange);
            // by default, set to mandatory mode
            return true;
        }
    }

    public Map<Integer, Integer> getPreviousDistribution(EndcDistrProfile endcDistrProfile) {
        // get the previous distribution
        Map<Integer, Integer> previousDistributionMap = new HashMap<>();
        if (endcDistrProfile != null) {
            List<GUtranSyncSignalFrequency> gUtranFreqRefFR1 = endcDistrProfile.getGUtranFreqRef();
            List<GUtranSyncSignalFrequency> gUtranFreqRefFR2 = endcDistrProfile.getMandatoryGUtranFreqRef();
            List<Integer> distributionList = endcDistrProfile.getGUtranFreqDistribution();

            // The order here is important, FR1 + FR2
            List<Integer> frequencyList = Stream.concat(gUtranFreqRefFR1.stream(), gUtranFreqRefFR2.stream())
                    .map(GUtranSyncSignalFrequency::getArfcn)
                    .toList();

            for (int i = 0; i < distributionList.size(); i++) {
                previousDistributionMap.put(frequencyList.get(i), distributionList.get(i));
            }
        }
        return previousDistributionMap;
    }

    public Map<Integer, Float> getNewDistribution(Map<Integer, Float> cellCapacity, Map<Integer, Float> loadChangeInPercent, Map<Integer, Integer> previousDistribution) {
        Map<Integer, Float> newDistributionInPercent = new HashMap<>();
        cellCapacity.forEach((arfcn, capacity) -> {
            if (previousDistribution.containsKey(arfcn)) {
                newDistributionInPercent.put(arfcn, previousDistribution.get(arfcn) * (1 + loadChangeInPercent.get(arfcn) / 100));
            } else {
                newDistributionInPercent.put(arfcn, capacity * (1 + loadChangeInPercent.get(arfcn) / 100));
            }
        });
        return newDistributionInPercent;
    }

    public Map<Integer, Float> getScaledOutput(boolean isMandatoryMode, Map<Integer, Float> weightedRRC, Map<Integer, Float> newDistributionInPercent) {
        /*
            The frequency group 1 and 2 are calculated based on the weighted RRC of each frequency group.
            frequency group1: NR cells that SCS >= 120kHz which are FR2
            frequency group2: NR cells that SCS < 120kHz which are FR1, of course, low band cells are excluded
            So, we can use ARFCN to differentiate the frequency groups
         */
        float sumDistributionFG1 = newDistributionInPercent.entrySet().stream()
                .filter(entry -> entry.getKey() >= MIN_FR2_FREQ)
                .map(Map.Entry::getValue)
                .reduce(0F, Float::sum);
        float sumDistributionFG2 = newDistributionInPercent.entrySet().stream()
                .filter(entry -> entry.getKey() < MIN_FR2_FREQ)
                .map(Map.Entry::getValue)
                .reduce(0F, Float::sum);


        if (isMandatoryMode) {
            Map<Integer, Float> scaledOutput = new HashMap<>();
            float totalWeightedRRC = weightedRRC.values().stream().reduce(0F, Float::sum);

            float frequencyGroupRatio1 = weightedRRC.entrySet().stream()
                    .filter(entry -> entry.getKey() >= MIN_FR2_FREQ)
                    .map(Map.Entry::getValue)
                    .reduce(0F, Float::sum) / totalWeightedRRC;
            float frequencyGroupRatio2 = weightedRRC.entrySet().stream()
                    .filter(entry -> entry.getKey() < MIN_FR2_FREQ)
                    .map(Map.Entry::getValue)
                    .reduce(0F, Float::sum) / totalWeightedRRC;

            newDistributionInPercent.forEach((arfcn, newDistr) -> {
                // for each frequency, scaled output = FG ratio * New Distribution / ∑(New Distribution) of that FG
                if (arfcn >= MIN_FR2_FREQ) {
                    scaledOutput.put(arfcn, frequencyGroupRatio1 * newDistr / sumDistributionFG1);
                } else {
                    scaledOutput.put(arfcn, frequencyGroupRatio2 * newDistr / sumDistributionFG2);
                }
            });

            return scaledOutput;

        } else {

            // for each frequency, scaled output = New Distribution / ∑(New Distribution) of both FGs
            return newDistributionInPercent.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue() / (sumDistributionFG1 + sumDistributionFG2)
                    ));
        }
    }
}