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
package com.ericsson.oss.apps.streaming.counters;

import com.ericsson.oss.apps.model.entities.Subscription;
import com.ericsson.oss.apps.model.pmrop.*;
import com.ericsson.oss.apps.repository.*;
import com.google.common.collect.MoreCollectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PmCounterProcessor {

    private final PmNRCellCURepo pmNrCellCuRepo;
    private final PmNRCellDURepo pmNrCellDuRepo;
    private final PmGNBDUFunctionRepo pmGNBDUFunctionRepo;
    private final PmEUtranCellRepo pmEUtranCellRepo;
    private final PmGUtranCellRelationRepo pmGUtranCellRelationRepo;
    private final PmGUtranFreqRelationRepo pmGUtranFreqRelationRepo;
    private final CmSubscriptionRepo cmSubscriptionRepo;

    public void processCounters(List<? extends PmRop> pmRecordList) {

        log.info("Counters received: {}", pmRecordList);
        Map<Class<?>, List<Object>> records = pmRecordList.stream().collect(Collectors.groupingBy(Object::getClass));

        List<PmRopNRCellCU> nrCellCUList = (List<PmRopNRCellCU>) (List<?>) records.getOrDefault(PmRopNRCellCU.class, new ArrayList<>());
        List<PmRopNRCellDU> nrCellDUList = (List<PmRopNRCellDU>) (List<?>) records.getOrDefault(PmRopNRCellDU.class, new ArrayList<>());
        List<PmRopGNBDUFunction> gnbduFunctionList = (List<PmRopGNBDUFunction>) (List<?>) records.getOrDefault(PmRopGNBDUFunction.class, new ArrayList<>());
        List<PmRopGUtranCellRelation> gUtranCellRelationList = (List<PmRopGUtranCellRelation>) (List<?>) records.getOrDefault(PmRopGUtranCellRelation.class, new ArrayList<>());
        List<PmRopGUtranFreqRelation> gUtranFreqRelationList = (List<PmRopGUtranFreqRelation>) (List<?>) records.getOrDefault(PmRopGUtranFreqRelation.class, new ArrayList<>());
        List<PmRopEUtranCell> eUtranCellList = (List<PmRopEUtranCell>) (List<?>) records.getOrDefault(PmRopEUtranCell.class, new ArrayList<>());

        pmNrCellCuRepo.saveAll(nrCellCUList);
        pmNrCellDuRepo.saveAll(nrCellDUList);
        pmGNBDUFunctionRepo.saveAll(gnbduFunctionList);
        pmGUtranCellRelationRepo.saveAll(gUtranCellRelationList);
        pmGUtranFreqRelationRepo.saveAll(gUtranFreqRelationList);
        pmEUtranCellRepo.saveAll(eUtranCellList);
    }

    public boolean isValidPmCounter(String nodeFDN, boolean isNRNode) {
        if (isNRNode) {
            return cmSubscriptionRepo.findAll().stream()
                    .collect(MoreCollectors.toOptional())
                    .map(Subscription::getNrNodeList)
                    .orElseGet(Collections::emptyList)
                    .contains(nodeFDN);
        }
        return cmSubscriptionRepo.findAll().stream()
                .collect(MoreCollectors.toOptional())
                .map(Subscription::getENodebList)
                .orElseGet(Collections::emptyList)
                .contains(nodeFDN);
    }
}
