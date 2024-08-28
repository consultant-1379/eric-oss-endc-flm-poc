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

import com.ericsson.oss.apps.ncmp.NcmpConfiguration;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@EnableAutoConfiguration
@AutoConfigureWireMock(port = 0)
@SpringBootTest(properties = {"rapp-sdk.topology.base-path=http://localhost:${wiremock.server.port}/oss-core-ws/rest"},
        classes = {ClientConfig.class, NcmpConfiguration.class, CommonTopologyConfiguration.class})
public class CtsRestClientTest {

    private static final String NODE_SCENARIO = "NODE_FETCH";
    private static final UrlPathPattern NODE_TOPOLOGY_PATH  =  urlPathEqualTo("/oss-core-ws/rest/ctw/enodeb");

    private static final String CELL_SCENARIO = "CELL_FETCH";
    private static final UrlPathPattern CELL_TOPOLOGY_PATH  =  urlPathEqualTo("/oss-core-ws/rest/ctw/ltecell");

    @Autowired
    private CommonTopologyService commonTopologyService;

    @Test
    void getNodes() {
        stubFor(get(NODE_TOPOLOGY_PATH).inScenario(NODE_SCENARIO)
                .whenScenarioStateIs(STARTED)
                .withQueryParams(getParameters(0L))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("response_1.json"))
                .willSetStateTo("next"));
        stubFor(get(NODE_TOPOLOGY_PATH).inScenario(NODE_SCENARIO)
                .whenScenarioStateIs("next")
                .withQueryParams(getParameters(84L))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("response_2.json"))
                .willSetStateTo("lastNode"));
        stubFor(get(NODE_TOPOLOGY_PATH).inScenario(NODE_SCENARIO)
                .whenScenarioStateIs("lastNode")
                .withQueryParams(getParameters(92L))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("response_3.json")));

        List<ManagedObjectId> expectedFdnList = List.of(
                ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=NETSimW,ManagedElement=LTE74dg2ERBST00064,ENodeBFunction=1"),
                ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=NETSimW,ManagedElement=LTE52dg2ERBST00078,ENodeBFunction=1")
        );

        List<ManagedObjectId> fdnList = commonTopologyService.fetchAllNodeFdn();

        Assertions.assertEquals(expectedFdnList, fdnList);
        verify(3, getRequestedFor(urlPathEqualTo("/oss-core-ws/rest/ctw/enodeb")));
    }

    @Test
    void getCells() {

        stubFor(get(CELL_TOPOLOGY_PATH).inScenario(CELL_SCENARIO)
                .whenScenarioStateIs(STARTED)
                .withQueryParams(getCellParameters("TDD"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("lte_response_1.json"))
                .willSetStateTo("fddCell"));
        stubFor(get(CELL_TOPOLOGY_PATH).inScenario(CELL_SCENARIO)
                .whenScenarioStateIs("fddCell")
                .withQueryParams(getCellParameters("FDD"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("lte_response_2.json")));

        List<ManagedObjectId> expectedFdnList = List.of(
                ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=NETSimW,ManagedElement=LTE74dg2ERBST00059,ENodeBFunction=1,EUtranCellTDD=LTE74dg2ERBST00059-1"),
                ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=NETSimW,ManagedElement=LTE63dg2ERBST00065,ENodeBFunction=1,EUtranCellFDD=LTE63dg2ERBST00065-1")
        );

        List<ManagedObjectId> fdnList = commonTopologyService.fetchAllCellFdn();

        Assertions.assertEquals(expectedFdnList, fdnList);
        verify(2, getRequestedFor(urlPathEqualTo("/oss-core-ws/rest/ctw/ltecell")));
    }

    private static Map<String, StringValuePattern> getCellParameters(String cellType) {
        return Map.of("type", equalTo(cellType));
    }

    private static Map<String, StringValuePattern> getParameters(Long id) {
        return Map.of(
                "fs", equalTo("attrs"),
                "sort", equalTo("objectInstId"),
                "criteria", equalTo(String.format("(objectInstId > %dL)", id))
        );
    }
}
