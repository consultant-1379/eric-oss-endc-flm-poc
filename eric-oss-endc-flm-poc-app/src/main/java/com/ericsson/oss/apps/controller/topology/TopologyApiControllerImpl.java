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
package com.ericsson.oss.apps.controller.topology;

import com.ericsson.oss.apps.api.controller.TopologyApi;
import com.ericsson.oss.apps.api.model.Fdn;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.common.topology.CommonTopologyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class TopologyApiControllerImpl implements TopologyApi {
    private final CommonTopologyService commonTopologyService;

    @Override
    public ResponseEntity<List<Fdn>> getAllNodeFdn() {
        log.info("Retrieving Topology Nodes");
        List<Fdn> nodeList = commonTopologyService.fetchAllNodeFdn().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());

        if (!nodeList.isEmpty()) {
            return new ResponseEntity<>(nodeList, HttpStatusCode.valueOf(200));
        }
        return new ResponseEntity<>(HttpStatusCode.valueOf(400));
    }

    @Override
    public ResponseEntity<List<Fdn>> getAllCellFdn() {
        log.info("Retrieving Topology Cells");
        List<Fdn> cellList = commonTopologyService.fetchAllCellFdn().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());

        if (!cellList.isEmpty()) {
            return new ResponseEntity<>(cellList, HttpStatusCode.valueOf(200));
        }
        return new ResponseEntity<>(HttpStatusCode.valueOf(400));
    }

    private Fdn convertToDto(ManagedObjectId objectId) {
        return new Fdn(objectId.fetchMEId().fetchDNValue(), objectId.toString());
    }
}
