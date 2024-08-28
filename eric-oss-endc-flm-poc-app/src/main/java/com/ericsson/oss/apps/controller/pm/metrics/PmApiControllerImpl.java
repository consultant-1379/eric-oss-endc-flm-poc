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
package com.ericsson.oss.apps.controller.pm.metrics;

import com.ericsson.oss.apps.api.controller.PmApi;
import com.ericsson.oss.apps.api.model.Counter;
import com.ericsson.oss.apps.model.pmrop.PmRop;
import com.ericsson.oss.apps.model.pmrop.PmRopEUtranCell;
import com.ericsson.oss.apps.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class PmApiControllerImpl implements PmApi {

    private final PmNRCellCURepo pmNRCellCURepo;
    private final PmNRCellDURepo pmNRCellDURepo;
    private final PmEUtranCellRepo pmEUtranCellRepo;
    private final PmGNBDUFunctionRepo pmGNBDUFunctionRepo;
    private final PmGUtranCellRelationRepo pmGUtranCellRelationRepo;
    private final PmGUtranFreqRelationRepo pmGUtranFreqRelationRepo;
    private final ObjectMapper mapper;

    @Override
    public ResponseEntity<Map<String, Map<String, Counter>>> getNRCellCUCounters() {
        log.info("Retrieving NRCellCU Counters");
        return fetchCounters(pmNRCellCURepo.findAll());
    }

    @Override
    public ResponseEntity<Map<String, Map<String, Counter>>> getNRCellDUCounters() {
        log.info("Retrieving NRCellDU Counters");
        return fetchCounters(pmNRCellDURepo.findAll());
    }

    @Override
    public ResponseEntity<Map<String, Map<String, Counter>>> getEUtranCellCounters() {
        log.info("Retrieving EUtranCell Counters");
        return fetchCounters(pmEUtranCellRepo.findAll());
    }

    @Override
    public ResponseEntity<Map<String, Map<String, Counter>>> getGNBDUFunctionCounters() {
        log.info("Retrieving GNBDUFunction Counters");
        return fetchCounters(pmGNBDUFunctionRepo.findAll());
    }

    @Override
    public ResponseEntity<Map<String, Map<String, Counter>>> getGUtranCellRelationCounters() {
        log.info("Retrieving GUtranCellRelation Counters");
        return fetchCounters(pmGUtranCellRelationRepo.findAll());
    }

    @Override
    public ResponseEntity<Map<String, Map<String, Counter>>> getGUtranFreqRelationCounters() {
        log.info("Retrieving GUtranFreqRelation Counters");
        return fetchCounters(pmGUtranFreqRelationRepo.findAll());
    }

    ResponseEntity<Map<String, Map<String, Counter>>> fetchCounters(List<? extends PmRop> cellList)
    {
        Map<String, Map<String, Counter>> counters;
        try{
            counters = fetchAllData(cellList);
            if (!counters.isEmpty()) {
                return ResponseEntity.ok(counters);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Failed to get PmCounters ", e);
        }
        return new ResponseEntity<>(HttpStatusCode.valueOf(400));
    }

    private Map<String, Map<String, Counter>> fetchAllData(List<? extends PmRop> cellList) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Map<String, Map<String, Counter>> result = new HashMap<>();

        for (PmRop cell : cellList) {
            String fdn = cell.getMoRopId().getObjectId().toString();
            long ropTime = cell.getMoRopId().getRopTime();
            Counter counter = getPmCounters(cell);

            // Get or create the map for the current timestamp
            Map<String, Counter> valueMap = result.computeIfAbsent(Long.toString(ropTime), k -> new HashMap<>());
            valueMap.put(fdn, counter);
        }

        return result;
    }

    private Counter getPmCounters(PmRop cell) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Map<String, Double> pmCounter = new HashMap();

        for (Field field : cell.getClass().getDeclaredFields()) {
            String fieldName = field.getName();

            if(fieldName.startsWith("pm")) {
                String counterName = fieldName.substring(0,1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
                String getMethodName = "get" + counterName;
                Method getterMethod = cell.getClass().getMethod(getMethodName);
                pmCounter.put(counterName, (Double) getterMethod.invoke(cell));
            }
        }

        return new Counter(pmCounter);
    }
}
