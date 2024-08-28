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

import com.ericsson.oss.apps.model.GUtranRelationAggregate;
import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.mom.NRCellCU;
import com.ericsson.oss.apps.model.pmrop.MoRopId;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.PmGUtranCellRelationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CellHitRateService {

    private final CellFilterService cellFilterService;
    private final PmGUtranCellRelationRepo pmGUtranCellRelationRepo;
    private final CellRelationService cellRelationService;

    public Map<EUtranCell, Map<Integer, Map<ManagedObjectId, Double>>> processEutranCellHitRate(List<ManagedObjectId> allowEutranCells, long ropTime) {
        Map<EUtranCell, List<GUtranRelationAggregate>> gUtranRelationAggregateMap = cellFilterService.getGUtranRelationMapping(allowEutranCells);

        return gUtranRelationAggregateMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> calculateHitRatePerEutranCell(entry.getValue(), ropTime)
                ));
    }

    private Map<Integer, Map<ManagedObjectId, Double>> calculateHitRatePerEutranCell(List<GUtranRelationAggregate> gUtranRelationAggregates, long ropTime) {
        Map<Integer, Double> frequencyToTotalHitRateMap = new HashMap<>();
        Map<ManagedObjectId, Double> nrCellCUToHitRateMap = new HashMap<>();
        Map<Integer, List<ManagedObjectId>> frequencyToNRCellCUMap = new HashMap<>();
        Map<Integer, Map<ManagedObjectId, Double>> finalHitRate = new HashMap<>();

        for (GUtranRelationAggregate gUtranRelationAggregate : gUtranRelationAggregates) {
            Optional<NRCellCU> nrCellCU = cellRelationService.getNRCellCUByGUtranCellRelation(gUtranRelationAggregate.gUtranCellRelation());

            if (nrCellCU.isPresent() && cellFilterService.isSubCarrierSpacingValid(nrCellCU.get())) {
                MoRopId moRopId = new MoRopId(gUtranRelationAggregate.gUtranCellRelation().getObjectId(), ropTime);
                Optional<Double> pmEndcSetupScgUeAtt = pmGUtranCellRelationRepo.findPmEndcSetupScgUeAttByMoRopId(moRopId);
                Integer arfcn = gUtranRelationAggregate.gUtranFreqRelation().getGUtranSyncSignalFrequencyRef().getArfcn();
                frequencyToNRCellCUMap.computeIfAbsent(arfcn, k -> new ArrayList<>()).add(nrCellCU.get().getObjectId());

                if (pmEndcSetupScgUeAtt.isPresent() && pmEndcSetupScgUeAtt.get() >= 0) {
                    log.debug("ropTime {} arfcn {} pmEndcSetupScgUeAtt {} gUtranCellRelation fdn {} ",
                            ropTime, arfcn, pmEndcSetupScgUeAtt, gUtranRelationAggregate.gUtranCellRelation().getObjectId().toString());
                    frequencyToTotalHitRateMap.merge(arfcn, pmEndcSetupScgUeAtt.get(), Double::sum);
                    nrCellCUToHitRateMap.put(nrCellCU.get().getObjectId(), pmEndcSetupScgUeAtt.get());
                } else {
                    // PM counter is invalid, still saving it but using NaN. This will be used for debugging in future
                    nrCellCUToHitRateMap.put(nrCellCU.get().getObjectId(), Double.NaN);
                    log.warn("pmCounter does not exist! ropTime {} arfcn {} gUtranCellRelation fdn {} ",
                            ropTime, arfcn, gUtranRelationAggregate.gUtranCellRelation().getObjectId().toString());
                }
            }
        }

        frequencyToNRCellCUMap.forEach((arfcn, nrCells) ->
        {
            // cellRelation to hitRate for a specific frequency
            Map<ManagedObjectId, Double> hitRatePerFrequency = new HashMap<>();
            nrCells.forEach(nrCell ->
                    hitRatePerFrequency.put(nrCell, nrCellCUToHitRateMap.get(nrCell) / frequencyToTotalHitRateMap.get(arfcn))
            );
            finalHitRate.put(arfcn, hitRatePerFrequency);
        });
        // return hit rate in decimal but should add up to 1
        return finalHitRate;
    }
}
