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

import com.ericsson.oss.apps.CoreApplication;
import com.ericsson.oss.apps.model.pmrop.*;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CoreApplication.class, PmApiControllerImpl.class})
public class PmApiControllerImplTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @Autowired
    PmNRCellCURepo pmNRCellCURepo;
    @Autowired
    PmNRCellDURepo pmNRCellDURepo;
    @Autowired
    PmEUtranCellRepo pmEUtranCellRepo;
    @Autowired
    PmGNBDUFunctionRepo pmGNBDUFunctionRepo;
    @Autowired
    PmGUtranCellRelationRepo pmGUtranCellRelationRepo;
    @Autowired
    PmGUtranFreqRelationRepo pmGUtranFreqRelationRepo;

    private static final ManagedObjectId FDD_CELL_RESOURCE = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035,ENodeBFunction=1,EUtranCellFDD=LTE31dg2ERBS00035-1");
    private static final ManagedObjectId TDD_CELL_RESOURCE = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00045,ManagedElement=LTE31dg2ERBS00045,ENodeBFunction=1,EUtranCellTDD=LTE31dg2ERBS00045-1");

    private static final long ROP_END_TIME_IN_EPOC = 1674817560000L;

    @BeforeEach
    public void setUp() {
        pmNRCellCURepo.deleteAll();
        pmNRCellDURepo.deleteAll();
        pmEUtranCellRepo.deleteAll();
        pmGNBDUFunctionRepo.deleteAll();
        pmGUtranCellRelationRepo.deleteAll();
        pmGUtranFreqRelationRepo.deleteAll();

        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    //TODO:  See if these counter tests can be made paramaterized.
    @Test
    void getNRCellCUCounters() throws Exception {
        //Add data to pmNRCellCURepo
        PmRopNRCellCU pmRopNRCellCU = new PmRopNRCellCU();

        pmRopNRCellCU.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmNRCellCURepo.save(pmRopNRCellCU);
        pmRopNRCellCU.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmNRCellCURepo.save(pmRopNRCellCU);

        // Adding 15 minutes (15 * 60 seconds)
        pmRopNRCellCU.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmNRCellCURepo.save(pmRopNRCellCU);
        pmRopNRCellCU.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmNRCellCURepo.save(pmRopNRCellCU);

        String expectedJson = new String(Files.readAllBytes(Paths.get("./src/test/resources/__files/nrCellCUCounters.json")));

        mvc.perform(get("/v1/pm/metrics/nrcellcu")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getNRCellDUCounters() throws Exception {
        //Add data to pmNRCellDURepo
        PmRopNRCellDU pmRopNRCellDU = new PmRopNRCellDU();

        pmRopNRCellDU.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmNRCellDURepo.save(pmRopNRCellDU);
        pmRopNRCellDU.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmNRCellDURepo.save(pmRopNRCellDU);

        // Adding 15 minutes (15 * 60 seconds)
        pmRopNRCellDU.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmNRCellDURepo.save(pmRopNRCellDU);
        pmRopNRCellDU.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmNRCellDURepo.save(pmRopNRCellDU);

        String expectedJson = new String(Files.readAllBytes(Paths.get("./src/test/resources/__files/nrCellDUCounters.json")));

        mvc.perform(get("/v1/pm/metrics/nrcelldu")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

    }

    @Test
    void getEUtranCellCounters() throws Exception {
        //Add data to pmRopEUtranCell
        PmRopEUtranCell pmRopEUtranCell = new PmRopEUtranCell();

        pmRopEUtranCell.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmEUtranCellRepo.save(pmRopEUtranCell);
        pmRopEUtranCell.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmEUtranCellRepo.save(pmRopEUtranCell);

        // Adding 15 minutes (15 * 60 seconds)
        pmRopEUtranCell.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmEUtranCellRepo.save(pmRopEUtranCell);
        pmRopEUtranCell.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmEUtranCellRepo.save(pmRopEUtranCell);

        String expectedJson = new String(Files.readAllBytes(Paths.get("./src/test/resources/__files/eUtranCellCounters.json")));

        mvc.perform(get("/v1/pm/metrics/eutrancell")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

    }

    @Test
    void getGNBDUFunctionCounters() throws Exception {
        //Add data to pmRopGNBDUFunction
        PmRopGNBDUFunction pmRopGNBDUFunction = new PmRopGNBDUFunction();

        pmRopGNBDUFunction.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmGNBDUFunctionRepo.save(pmRopGNBDUFunction);
        pmRopGNBDUFunction.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmGNBDUFunctionRepo.save(pmRopGNBDUFunction);

        // Adding 15 minutes (15 * 60 seconds)
        pmRopGNBDUFunction.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmGNBDUFunctionRepo.save(pmRopGNBDUFunction);
        pmRopGNBDUFunction.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmGNBDUFunctionRepo.save(pmRopGNBDUFunction);

        String expectedJson = new String(Files.readAllBytes(Paths.get("./src/test/resources/__files/gNBDUFunctionCounters.json")));

        mvc.perform(get("/v1/pm/metrics/gnbdufunction")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

    }

    @Test
    void getGUtranCellRelationCounters() throws Exception {
        //Add data to pmRopGUtranCellRelation
        PmRopGUtranCellRelation pmRopGUtranCellRelation = new PmRopGUtranCellRelation();

        pmRopGUtranCellRelation.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmGUtranCellRelationRepo.save(pmRopGUtranCellRelation);
        pmRopGUtranCellRelation.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmGUtranCellRelationRepo.save(pmRopGUtranCellRelation);

        // Adding 15 minutes (15 * 60 seconds)
        pmRopGUtranCellRelation.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmGUtranCellRelationRepo.save(pmRopGUtranCellRelation);
        pmRopGUtranCellRelation.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmGUtranCellRelationRepo.save(pmRopGUtranCellRelation);

        String expectedJson = new String(Files.readAllBytes(Paths.get("./src/test/resources/__files/gUtranCellRelationCounters.json")));

        mvc.perform(get("/v1/pm/metrics/gutrancellrelation")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

    }

    @Test
    void getGUtranFreqRelationCounters() throws Exception {
        //Add data to pmRopGUtranFreqRelation
        PmRopGUtranFreqRelation pmRopGUtranFreqRelation = new PmRopGUtranFreqRelation();

        pmRopGUtranFreqRelation.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmGUtranFreqRelationRepo.save(pmRopGUtranFreqRelation);
        pmRopGUtranFreqRelation.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC));
        pmGUtranFreqRelationRepo.save(pmRopGUtranFreqRelation);

        // Adding 15 minutes (15 * 60 seconds)
        pmRopGUtranFreqRelation.setMoRopId(new MoRopId(FDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmGUtranFreqRelationRepo.save(pmRopGUtranFreqRelation);
        pmRopGUtranFreqRelation.setMoRopId(new MoRopId(TDD_CELL_RESOURCE, ROP_END_TIME_IN_EPOC + (15 * 60 * 1000)));
        pmGUtranFreqRelationRepo.save(pmRopGUtranFreqRelation);

        String expectedJson = new String(Files.readAllBytes(Paths.get("./src/test/resources/__files/gUtranFreqRelationCounters.json")));

        mvc.perform(get("/v1/pm/metrics/gutranfreqrelation")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

    }

    private static Stream<Arguments> counterRequests() throws Exception {

        return Stream.of(
                Arguments.of("/v1/pm/metrics/nrcellcu"),
                Arguments.of("/v1/pm/metrics/nrcelldu")
        );
    }

    @ParameterizedTest
    @MethodSource("counterRequests")
    void emptyCounters(String url) throws Exception {

        mvc.perform(get(url).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}


