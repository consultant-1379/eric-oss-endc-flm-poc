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

import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.pmrop.MoRopId;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.PmNRCellCURepo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeightedAverageRRCService {
    private final CellHitRateService cellHitRateService;
    private final PmNRCellCURepo pmNRCellCURepo;

    @Getter
    @Setter
    private Map<String, Map<Integer, Float>> freqLoadMap = new HashMap<>();
    @Getter
    @Setter
    private Map<String, Map<String, Float>> nrCellLoadMap = new HashMap<>();

    public Map<EUtranCell, Map<Integer, Float>> processWeightedAverageRRCConnUsers(List<ManagedObjectId> allowEutranCells, long ropTime) {
        Map<EUtranCell, Map<Integer, Float>> hitRate = cellHitRateService.processEutranCellHitRate(allowEutranCells, ropTime).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateRRCConnUsersPerFrequency(entry.getKey().getFdn(), entry.getValue(), ropTime)
                ));

        freqLoadMap = hitRate.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getFdn(), Map.Entry::getValue));

        return hitRate;
    }

    private Map<Integer, Float> calculateRRCConnUsersPerFrequency(String eUtranCellFdn, Map<Integer, Map<ManagedObjectId, Double>> frequencyToHitRateMap, long ropTime) {
        Map<Integer, Float> frequencyToWeightedAverageRRCMap = new HashMap<>();
        // For each frequency, calculate the sum of averaged RRC weight
        frequencyToHitRateMap.forEach((arfcn, nrCellToHitRateMap) ->
        {
            // For a single frequency, loops through all NRCellCU and sum the averaged RRC weight
            Map<String, Float> cellLoadMap = nrCellToHitRateMap.entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> getNRCellAverageRRCWeight(entry.getKey(), entry.getValue(), ropTime)));
            nrCellLoadMap.merge(eUtranCellFdn, cellLoadMap, this::mergeTwoLoadMap);

            Float weightedValue = cellLoadMap.values().stream().reduce(0F, Float::sum);

            log.debug("ropTime {} arfcn {} weightedValue {}", ropTime, arfcn, weightedValue);
            frequencyToWeightedAverageRRCMap.put(arfcn, weightedValue);
        });
        // Frequency -> RRC weight mapping
        return frequencyToWeightedAverageRRCMap;
    }

    private Float getNRCellAverageRRCWeight(ManagedObjectId objectID, Double hitRate, long ropTime) {
        MoRopId moRopId = new MoRopId(objectID, ropTime);
        Optional<Double> pmRrcConnLevelSamp = pmNRCellCURepo.findPmRrcConnLevelSampByMoRopId(moRopId);
        Optional<Double> pmRrcConnLevelSumEnDc = pmNRCellCURepo.findPmRrcConnLevelSumEnDcByMoRopId(moRopId);

        // calculate the averaged RRC weight for a single NRCellCU, if counter not exits or hitRate is NaN, return NaN
        if (pmRrcConnLevelSamp.isPresent() && pmRrcConnLevelSamp.get() >= 0 && pmRrcConnLevelSumEnDc.isPresent() && pmRrcConnLevelSumEnDc.get() >= 0 && !Double.isNaN(hitRate)) {
            log.debug("nrCellCU FDN {} ropTime {}, hitRate {} pmRrcConnLevelSumEnDc {} pmRrcConnLevelSamp {}",
                    objectID.toString(), ropTime, hitRate, pmRrcConnLevelSumEnDc.get(), pmRrcConnLevelSamp.get());
            return hitRate.floatValue() * (pmRrcConnLevelSumEnDc.get().floatValue() / pmRrcConnLevelSamp.get().floatValue());
        }
        log.warn("nrCellCU FDN {} ropTime {}, hitRate {}", objectID.toString(), ropTime, hitRate);
        return Float.NaN;
    }

    private Map<String, Float> mergeTwoLoadMap(Map<String, Float> oldMapValue, Map<String, Float> newMapValue) {
        newMapValue.entrySet().stream()
                .forEach(entry -> oldMapValue.merge(entry.getKey(), entry.getValue(), Float::sum));

        return oldMapValue;
    }
}
