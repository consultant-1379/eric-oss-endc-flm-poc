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

import com.ericsson.oss.apps.CoreApplication;
import com.ericsson.oss.apps.CoreApplicationTest;
import com.ericsson.oss.apps.model.GUtranRelationAggregate;
import com.ericsson.oss.apps.model.mom.*;
import com.ericsson.oss.apps.ncmp.NcmpClient;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.ncmp.util.ManagedObjectAggregate;
import com.ericsson.oss.apps.repository.*;
import com.ericsson.oss.apps.topology.IdentityService;
import com.ericsson.oss.apps.topology.model.NRCellId;
import com.ericsson.oss.apps.topology.model.PLMNId;
import com.google.common.collect.MoreCollectors;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@SpringBootTest(classes = {CellRelationServiceTest.TestConfiguration.class, CoreApplicationTest.class, CoreApplication.class},
        properties = {
                "rapp-sdk.topology.base-path=http://localhost:${wiremock.server.port}/topology",
                "rapp-sdk.ncmp.base-path=http://localhost:${wiremock.server.port}/ncmp",
        })
@Sql(scripts = "classpath:db/cm_relation_test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/clean_up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@AutoConfigureWireMock(port = 0)
class CellRelationServiceTest {

    private static final String TOPOLOGY_PATH = "/topology/domains/RAN_LOGICAL/entities/NRCellCU";
    private static final String CM_PATH = "/ncmp/v1/ch/D82C8E788ECA217A81A3CD526FFBF796/data/ds/ncmp-datastore%3Apassthrough-running";
    private static final ManagedObjectId GUTRAN_FREQ_RELATION_ID = ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054815,GUtranFreqRelation=426970-15-20-0-2");
    private static final ManagedObjectId GUTRAN_SYNC_SIGNAL_FEQ_ID = ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,GUtraNetwork=1,GUtranSyncSignalFrequency=630000-33");
    private static final ManagedObjectId LTE_CELL_ID = ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,ENodeBFunction=1,EUtranCellFDD=73054815");
    private static final NRCellId NR_CELL_ID = new NRCellId(new PLMNId(525, 1, 2), 1037902, 23, 1021);
    private static final ManagedObjectId NR_CELL_CU_ID = ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBCUCPFunction=1,NRCellCU=10379021021");
    private static final ManagedObjectId NR_CELL_DU_ID = ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_737908_Textile_Centre,ManagedElement=ESS_737908_Textile_Centre,GNBDUFunction=1,NRCellDU=10379021021-1");
    private static final ManagedObjectId NR_CELL_CU_ID_1 = ManagedObjectId.of("SubNetwork=ONRM_ROOT_MO,SubNetwork=ENB_CITYHALL,MeContext=ESS_730548_Song_Lin_Building,ManagedElement=ESS_730548_Song_Lin_Building,GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1");
    @Autowired
    private CellRelationService cellRelationService;
    @Autowired
    private CmEUtranCellRepo cmEUtranCellRepo;
    @Autowired
    private CmGUtranSyncSignalFrequencyRepo cmGUtranSyncSignalFrequencyRepo;
    @Autowired
    private CmGUtranFreqRelationRepo cmGUtranFreqRelationRepo;
    @Autowired
    private NcmpClient ncmpClient;
    @Autowired
    private IdentityService identityService;

    @Test
    @Transactional
    void testRelationService() {
        stubFor(get(urlPathEqualTo(TOPOLOGY_PATH))
                .withQueryParam("scopeFilter", equalTo("/attributes[@cellLocalId=1021] ; /GNBCUCPFunction/attributes[@gNBId=1037902 and @gNBIdLength=23and contains(@plmnId, '\"mcc\": \"525\"') and contains(@pLMNId, '\"mnc\": \"01\"')]"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("relation/topology.json")));
        stubFor(get(urlPathEqualTo(CM_PATH))
                .withQueryParam("options", and(containing("NRCellDU"), containing("NRCellCU"), containing("GNBCUCPFunction")))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("relation/nrcell.json")));
        // Get lte cell from repo
        Optional<EUtranCell> cell = cmEUtranCellRepo.findById(LTE_CELL_ID);
        Assertions.assertThat(cell.map(ManagedObject::getObjectId)).hasValue(LTE_CELL_ID);
        // Get the only relation for the lte cell and extract the CellId from it
        Optional<NRCellId> globalCellId = cell
                .flatMap(eUtranCell -> cellRelationService.listGUtranRelationByEUtranCell(eUtranCell).stream()
                        .collect(MoreCollectors.toOptional()))
                .map(GUtranRelationAggregate::getTargetGlobalCellId);

        // search list of GUtranSyncSignalFrequency using eUtranCell
        connectFrequencyRelation();
        assert (cell.isPresent());
        List<GUtranSyncSignalFrequency> gUtranSyncSignalFrequencies = cellRelationService.listGUtranSyncSignalFreqByEUtranCell(cell.get().getFdn());
        assertEquals(1, gUtranSyncSignalFrequencies.stream().toList().size());
        Assertions.assertThat(gUtranSyncSignalFrequencies.stream()
                .map(ManagedObject::getObjectId)
                .findFirst()).hasValue(GUTRAN_SYNC_SIGNAL_FEQ_ID);

        // search NRCellCU using GUtranCellRelation
        Optional<NRCellCU> nrCellCU = cell
                .flatMap(eUtranCell -> cellRelationService.listGUtranRelationByEUtranCell(eUtranCell).stream()
                        .collect(MoreCollectors.toOptional()))
                .flatMap(gUtranRelationAggregate -> cellRelationService.getNRCellCUByGUtranCellRelation(gUtranRelationAggregate.gUtranCellRelation()).stream()
                        .collect(MoreCollectors.toOptional()));
        Assertions.assertThat(nrCellCU.map(ManagedObject::getObjectId)).hasValue(NR_CELL_CU_ID_1);

        // Search and sync ManagedObjects by CellId
        Assertions.assertThat(globalCellId).hasValue(NR_CELL_ID)
                .flatMap(cellId -> identityService.getObjectIdFromCellId(cellId)).hasValue(NR_CELL_CU_ID)
                .flatMap(cuMoId -> ncmpClient.getCmResource(cuMoId, NRCellCU.class))
                .map(ManagedObject::getObjectId).hasValue(NR_CELL_CU_ID);

        // Resolve Targeted NRCellCU by CellId
        nrCellCU = globalCellId.flatMap(globalId -> cellRelationService.getNRCellCUByNrGlobalCellId(globalId));
        Assertions.assertThat(nrCellCU.map(ManagedObject::getObjectId)).hasValue(NR_CELL_CU_ID);

        // Get the only NRCellDU from NRCellCU
        Optional<NRCellDU> nrCellDU = nrCellCU
                .flatMap(cu -> cellRelationService.listNRCellDUByNRCellCU(cu).stream()
                        .collect(MoreCollectors.toOptional()));
        Assertions.assertThat(nrCellDU.map(ManagedObject::getObjectId)).hasValue(NR_CELL_DU_ID);

        // Get the NRCellCU from NRCellDU
        Optional<NRCellCU> nrCellCUBackRef = nrCellDU.flatMap(du -> cellRelationService.getNRCellCUByNRCellDU(du));
        Assertions.assertThat(nrCellCUBackRef.map(ManagedObject::getObjectId)).hasValue(NR_CELL_CU_ID);
    }

    @Transactional
    private void connectFrequencyRelation() {
        Optional<GUtranFreqRelation> gUtranFreqRelation = cmGUtranFreqRelationRepo.findById(GUTRAN_FREQ_RELATION_ID);
        Optional<GUtranSyncSignalFrequency> gUtranSyncSignalFrequency = cmGUtranSyncSignalFrequencyRepo.findById(GUTRAN_SYNC_SIGNAL_FEQ_ID);
        if (gUtranFreqRelation.isPresent() && gUtranSyncSignalFrequency.isPresent()) {
            // connect the relation
            gUtranFreqRelation.get().setGUtranSyncSignalFrequencyRef(gUtranSyncSignalFrequency.get());
            return;
        }
        Assertions.fail("repos are empty");
    }

    @Configuration
    public static class TestConfiguration {
        @Bean
        public ManagedObjectAggregate nrRelationAggregate() {
            return new ManagedObjectAggregate() {
                @Override
                public Class<? extends ManagedObject> getKey() {
                    return NRCellCU.class;
                }

                @Override
                public Set<Class<? extends ManagedObject>> getTypes() {
                    return Set.of(NRCellCU.class, NRCellDU.class, GNBCUCPFunction.class);
                }
            };
        }
    }
}
