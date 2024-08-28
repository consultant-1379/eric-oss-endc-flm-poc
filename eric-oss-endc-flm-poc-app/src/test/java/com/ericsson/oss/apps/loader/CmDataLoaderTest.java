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
package com.ericsson.oss.apps.loader;

import com.ericsson.oss.apps.CoreApplication;
import com.ericsson.oss.apps.CoreApplicationTest;
import com.ericsson.oss.apps.config.CmDataLoaderConf;
import com.ericsson.oss.apps.execution.ExecutionContext;
import com.ericsson.oss.apps.handler.CmDataLoaderHandler;
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.model.mom.EUtranCellFDD;
import com.ericsson.oss.apps.model.mom.EUtranCellTDD;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.*;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "rapp-sdk.ncmp.base-path=http://localhost:${wiremock.server.port}/ncmp",
        "app.scheduling.enable=false"
}, classes = {CmDataLoaderConf.class, CoreApplicationTest.class, CoreApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureWireMock(port = 0)
class CmDataLoaderTest {
    @Autowired
    private CmEndcDistrProfileRepo cmEndcDistrProfileRepo;
    @Autowired
    private CmGUtranSyncSignalFrequencyRepo cmGUtranSyncSignalFrequencyRepo;
    @Autowired
    private CmEUtranCellFDDRepo cmEUtranCellFDDRepo;
    @Autowired
    private CmEUtranCellTDDRepo cmEUtranCellTDDRepo;
    @Autowired
    private CmSectorCarrierRepo cmSectorCarrierRepo;
    @Autowired
    private CmGUtranFreqRelationRepo cmGUtranFreqRelationRepo;
    @Autowired
    private CmGUtranCellRelationRepo cmGUtranCellRelationRepo;
    @Autowired
    private CmTermPointToGNBRepo cmTermPointToGNBRepo;
    @Autowired
    private CmNRCellCURepo cmNRCellCURepo;
    @Autowired
    private CmExternalNRCellCURepo cmExternalNRCellCURepo;
    @Autowired
    private CmGNBCUCPFunctionRepo cmGNBCUCPFunctionRepo;
    @Autowired
    private CmExternalGNBCUCPFunctionRepo cmExternalGNBCUCPFunctionRepo;
    @Autowired
    private CmNRCellRelationRepo cmNRCellRelationRepo;
    @Autowired
    private CmNRCellDURepo cmNRCellDURepo;
    @Autowired
    private CmNRSectorCarrierRepo cmNRSectorCarrierRepo;
    @Autowired
    private CmFeatureStateRepo cmFeatureStateRepo;
    @Autowired
    private CmAllowListRepo cmAllowListRepo;
    @Autowired
    private CmDataLoader cmDataLoader;
    @Autowired
    private CmDataLoaderHandler cmDataLoaderHandler;

    private final ExecutionContext context = new ExecutionContext(1234L);

    private static final ManagedObjectId FDD_CELL_RESOURCE = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,ENodeBFunction=1,EUtranCellFDD=LTE31dg2ERBS00035-1");
    private static final ManagedObjectId TDD_CELL_RESOURCE = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,ENodeBFunction=1,EUtranCellTDD=LTE31dg2ERBS00035-10");
    private static final ManagedObjectId GNBCUCPFunction = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,GNBCUCPFunction=1");
    private static final ManagedObjectId CELL_RESOURCE_NOT_EXISTS_IN_JSON = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,ENodeBFunction=1,EUtranCellFDD=LTE31dg2ERBS00035-3");
    private static final Map<String, StringValuePattern> QUERY_PARAMETERS = Map.of(
            "resourceIdentifier", equalTo("/"),
            "options", matching("fields=.+")
    );

    @ParameterizedTest
    @MethodSource("fdnProvider")
    void cmLoaderWithResponse(ManagedObjectId fdn) {
        ncmpStub(ncmpFileResponse("endcDistrProfile.json"));

        context.setAllowList(List.of(new AllowedMo(fdn, true, true, false)));

        cmDataLoaderHandler.handle(context);
        assertRepo(2);

        EUtranCellFDD eUtranCellFDD = cmEUtranCellFDDRepo.findById(FDD_CELL_RESOURCE).orElse(null);
        assertNotNull(eUtranCellFDD);
        assertNotNull(eUtranCellFDD.getEndcDistrProfileRef());
        assertNotNull(eUtranCellFDD.getEndcDistrProfileRef().getGUtranFreqRef());
        assertNotNull(eUtranCellFDD.getEndcDistrProfileRef().getMandatoryGUtranFreqRef());


        assertEquals(11, eUtranCellFDD.getCellId());
        assertEquals(1, eUtranCellFDD.getEndcDistrProfileRef().getEndcUserThreshold());
        assertEquals(62000, eUtranCellFDD.getEndcDistrProfileRef().getMandatoryGUtranFreqRef().get(0).getArfcn());
        assertEquals(63000, eUtranCellFDD.getEndcDistrProfileRef().getGUtranFreqRef().get(0).getArfcn());
    }

    @Test
    void cmLoaderMultipleAllowed() {
        ncmpStub(ncmpFileResponse("resources.json"));

        List<AllowedMo> allowedList = List.of(new AllowedMo(FDD_CELL_RESOURCE, true, true, false),
                new AllowedMo(TDD_CELL_RESOURCE, true, true, true));
        context.setAllowList(allowedList);

        cmDataLoaderHandler.handle(context);

        assertEquals(1, cmEUtranCellFDDRepo.findAll().size());
        EUtranCellFDD eUtranCellFDD = cmEUtranCellFDDRepo.findById(FDD_CELL_RESOURCE).orElse(null);
        assertNotNull(eUtranCellFDD);
        assertEquals(1, eUtranCellFDD.getCellId());

        assertEquals(1, cmEUtranCellTDDRepo.findAll().size());
        EUtranCellTDD eUtranCellTDD = cmEUtranCellTDDRepo.findById(TDD_CELL_RESOURCE).orElse(null);
        assertNotNull(eUtranCellTDD);
        assertEquals(10, eUtranCellTDD.getCellId());

        assertEquals(2, cmSectorCarrierRepo.findAll().size());
        assertEquals(1, cmGUtranFreqRelationRepo.findAll().size());
        assertEquals(1, cmGUtranCellRelationRepo.findAll().size());

        assertEquals(1, cmNRCellCURepo.findAll().size());
        assertEquals(2, cmNRCellDURepo.findAll().size());

        assertEquals(2, cmNRSectorCarrierRepo.findAll().size());
        assertEquals(1, cmTermPointToGNBRepo.findAll().size());

        assertEquals(2, cmFeatureStateRepo.findAll().size());
    }

    @Test
    void cmLoaderFetchExternalNRData() {
        ncmpStub(ncmpFileResponse("resources.json"));
        cmDataLoader.fetchExternalNRData(GNBCUCPFunction);

        assertEquals(1, cmNRCellCURepo.findAll().size());
        assertEquals(1, cmExternalNRCellCURepo.findAll().size());
        assertEquals(1, cmGNBCUCPFunctionRepo.findAll().size());
        assertEquals(1, cmExternalGNBCUCPFunctionRepo.findAll().size());
        assertEquals(1, cmNRCellRelationRepo.findAll().size());
    }

    @Test
    void cmLoaderWithEmptyNodeList() {
        cmDataLoaderHandler.handle(context);
        assertRepo(0);
    }

    @Test
    void cmLoaderWithEmptyMo() {
        ncmpStub(ncmpStringResponse(""));
        List<AllowedMo> allowedList = List.of(new AllowedMo(FDD_CELL_RESOURCE, true, true, false));
        context.setAllowList(allowedList);
        cmDataLoaderHandler.handle(context);
        assertRepo(0);
    }

    @Test
    void cmLoaderErrorTest() {
        ncmpStub(ncmpFileResponse(""));
        List<AllowedMo> allowedList = List.of(new AllowedMo(FDD_CELL_RESOURCE, true, true, false));
        context.setAllowList(allowedList);
        cmDataLoaderHandler.handle(context);
        assertRepo(0);
    }

    static Stream<ManagedObjectId> fdnProvider() {
        return Stream.of(FDD_CELL_RESOURCE, CELL_RESOURCE_NOT_EXISTS_IN_JSON);
    }

    private void ncmpStub(ResponseDefinitionBuilder response) {
        var ncmpUrlPattern = urlPathEqualTo("/ncmp/v1/ch/5FAD235EF83832777ED607AC4F83A152/data/ds/ncmp-datastore%3Apassthrough-running");
        stubFor(get(ncmpUrlPattern)
                .withQueryParams(QUERY_PARAMETERS)
                .willReturn(response));
    }

    private ResponseDefinitionBuilder ncmpFileResponse(String filename) {
        return aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBodyFile(filename);
    }

    private ResponseDefinitionBuilder ncmpStringResponse(String response) {
        return aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(response);
    }


    private void assertRepo(int expectedCount) {
        assertEquals(expectedCount, cmEndcDistrProfileRepo.findAll().size());
        assertEquals(expectedCount, cmGUtranSyncSignalFrequencyRepo.findAll().size());
        assertEquals(expectedCount, cmEUtranCellFDDRepo.findAll().size());
    }
}
