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
package com.ericsson.oss.apps.handler;

import com.ericsson.oss.apps.CoreApplication;
import com.ericsson.oss.apps.CoreApplicationTest;
import com.ericsson.oss.apps.config.CmDataLoaderConf;
import com.ericsson.oss.apps.execution.ExecutionContext;

import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.*;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "rapp-sdk.ncmp.base-path=http://localhost:${wiremock.server.port}/ncmp",
        "app.scheduling.enable=false"
}, classes = {CmDataLoaderConf.class, CoreApplicationTest.class, CoreApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureWireMock(port = 0)
class CellSuitabilityCheckTest {

    @Autowired
    private CmFeatureStateRepo cmFeatureStateRepo;
    @Autowired
    private CmDataLoaderHandler cmDataLoaderHandler;
    @Autowired
    private CellSuitabilityCheckHandler cellSuitabilityCheckHandler;

    private static final ManagedObjectId FDD_CELL_RESOURCE = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,ENodeBFunction=1,EUtranCellFDD=LTE31dg2ERBS00035-1");
    private static final ManagedObjectId TDD_CELL_RESOURCE = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,ENodeBFunction=1,EUtranCellTDD=LTE31dg2ERBS00035-10");
    private static final Map<String, StringValuePattern> QUERY_PARAMETERS = Map.of(
            "resourceIdentifier", equalTo("/"),
            "options", matching("fields=.+")
    );

    @Test
    void featureAllowed() {

        var ncmpUrlPattern = urlPathEqualTo("/ncmp/v1/ch/5FAD235EF83832777ED607AC4F83A152/data/ds/ncmp-datastore%3Apassthrough-running");
        stubFor(get(ncmpUrlPattern)
                .withQueryParams(QUERY_PARAMETERS)
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("resources.json")));

        ExecutionContext context = new ExecutionContext(1234L);

        // Set Allowed List
        List<AllowedMo> allowedList = List.of(new AllowedMo(FDD_CELL_RESOURCE, true, true, false),
                new AllowedMo(TDD_CELL_RESOURCE, true, true, true));
        context.setAllowList(allowedList);

        // Load CM Data
        cmDataLoaderHandler.handle(context);

        // Run Suitability Check
        cellSuitabilityCheckHandler.handle(context);

        assertEquals(2, context.getAllowEutranCells().size());
        assertEquals(2, cmFeatureStateRepo.findAll().size());
        for (AllowedMo allowedMo : context.getAllowList()) {
            assertEquals(false, allowedMo.getIsBlocked());
        }
    }
}

