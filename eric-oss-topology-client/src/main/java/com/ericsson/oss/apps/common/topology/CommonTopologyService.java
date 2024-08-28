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
package com.ericsson.oss.apps.common.topology;

import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.ncmp.util.CmHandleResolver;
import com.ericsson.oss.apps.common.topology.model.ExternalId;
import com.ericsson.oss.apps.common.topology.model.CommonTopologyObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ericsson.oss.apps.common.topology.IdentityUtils.*;

@Slf4j
@RequiredArgsConstructor
public class CommonTopologyService {

    private static final String NODE_TOPOLOGY_CACHE_KEY = "fetchAllNodeFdn";
    private static final List<String> NODE_RESOURCE_KEYS = List.of("ManagedElement", "ENodeBFunction");

    private static final String CELL_TOPOLOGY_CACHE_KEY = "fetchAllCellFdn";
    private static final List<String> TDDCELL_RESOURCE_KEYS = List.of("ManagedElement", "ENodeBFunction", "EUtranCellTDD");
    private static final List<String> FDDCELL_RESOURCE_KEYS = List.of("ManagedElement", "ENodeBFunction", "EUtranCellFDD");


    private final CtsRestClient ctsRestClient;
    private final CmHandleResolver cmHandleResolver;

    @Scheduled(cron = "${rapp-sdk.topology.cache.eviction-rate:0 0 0 * * *}")
    @CacheEvict(value = NODE_TOPOLOGY_CACHE_KEY, allEntries = true)
    public void evictAllNodeCacheValues() {
        log.debug("Cache: {} evicted", NODE_TOPOLOGY_CACHE_KEY);
    }

    @Scheduled(cron = "${rapp-sdk.topology.cache.eviction-rate:0 0 0 * * *}")
    @CacheEvict(value = CELL_TOPOLOGY_CACHE_KEY, allEntries = true)
    public void evictAllCellCacheValues() {
        log.debug("Cache: {} evicted", CELL_TOPOLOGY_CACHE_KEY);
    }

    @Cacheable(value = NODE_TOPOLOGY_CACHE_KEY)
    public List<ManagedObjectId> fetchAllNodeFdn() {
        return ctsRestClient.fetchNodes()
                .map(this::extractNodeFdn)
                .toStream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Cacheable(value = CELL_TOPOLOGY_CACHE_KEY)
    public List<ManagedObjectId> fetchAllCellFdn() {

        List<ManagedObjectId> tddList = ctsRestClient.fetchCells("TDD")
                .map(topologyObject -> extractCellFdn(topologyObject, TDDCELL_RESOURCE_KEYS))
                .toStream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        log.info("Retrieved TDD List of size {}", tddList.size());

        List<ManagedObjectId> fddList = ctsRestClient.fetchCells("FDD")
                .map(topologyObject -> extractCellFdn(topologyObject, FDDCELL_RESOURCE_KEYS))
                .toStream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        log.info("Retrieved FDD List of size {}", tddList.size());

        return Stream.concat(tddList.stream(), fddList.stream())
                .collect(Collectors.toList());
    }

    private Optional<ManagedObjectId> extractNodeFdn(CommonTopologyObject networkTopologyObject) {
        List<String> values = Arrays.asList(networkTopologyObject.getName().split("/"));
        ExternalId externalId = networkTopologyObject.getExternalId();

        Optional<ManagedObjectId> nodeFdn = generateDnPrefixKeyCombinations(values.size() - NODE_RESOURCE_KEYS.size())
                .peek(keys -> keys.addAll(NODE_RESOURCE_KEYS))
                .map(keys -> buildFdn(keys, values))
                .map(ManagedObjectId::of)
                .filter(fdn -> verifyCmHandle(externalId, fdn))
                .findFirst();

        if (nodeFdn.isEmpty()) {
            log.warn("Couldn't find Fdn for {}", externalId);
        }
        return nodeFdn;
    }

    private Optional<ManagedObjectId> extractCellFdn(CommonTopologyObject networkTopologyObject, List<String> cellResourceKeys) {
        List<String> values = Arrays.asList(networkTopologyObject.getName().split("/"));
        ExternalId externalId = networkTopologyObject.getExternalId();

        Optional<ManagedObjectId> cellFdn = generateDnPrefixKeyCombinations(values.size() - cellResourceKeys.size())
                .peek(keys -> keys.addAll(cellResourceKeys))
                .map(keys -> buildFdn(keys, values))
                .map(ManagedObjectId::of)
                .filter(fdn -> verifyCmHandle(externalId, fdn))
                .findFirst();

        if (cellFdn.isEmpty()) {
            log.warn("Couldn't find Fdn for {}", externalId);
        }

        return cellFdn;
    }

    private boolean verifyCmHandle(ExternalId externalId, ManagedObjectId objectId) {
        return cmHandleResolver.getCmHandle(objectId)
                .map(e -> externalId.cmHandle().equals(e))
                .orElseGet(() -> {
                    log.warn("CmHandler resolver algorithm failed");
                    return false;
                });
    }
}
