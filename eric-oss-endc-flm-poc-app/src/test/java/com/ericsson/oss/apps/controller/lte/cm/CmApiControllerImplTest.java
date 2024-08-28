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
package com.ericsson.oss.apps.controller.lte.cm;

import com.ericsson.oss.apps.CoreApplication;
import com.ericsson.oss.apps.controller.cm.lte.CmApiControllerImpl;
import com.ericsson.oss.apps.repository.CmAllowListRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {CoreApplication.class, CmApiControllerImpl.class})
public class CmApiControllerImplTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    CmAllowListRepo cmAllowListRepo;
    private MockMvc mvc;

    @BeforeEach
    public void setUp() {
        cmAllowListRepo.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private static Stream<Arguments> sendAllowListArgs() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("controller/AllowList.json").getInputStream();
        JsonNode mapper = objectMapper.readTree(inputStream);

        return Stream.of(
                Arguments.of(status().isOk(), (mapper.get("inputAllowList1").toString()), (mapper.get("expectedAllowList1").toString())),
                Arguments.of(status().isBadRequest(), (mapper.get("inputAllowList2").toString()), (mapper.get("expectedAllowList2").toString())),
                Arguments.of(status().isOk(), (mapper.get("inputAllowList3").toString()), (mapper.get("expectedAllowList3").toString()))
        );
    }

    @ParameterizedTest
    @MethodSource("sendAllowListArgs")
    void sendAllowList(ResultMatcher status, String inputAllowList, String expectedAllowList) throws Exception {
        mvc.perform(put("/v1/cm/lte/allowlist").contentType(MediaType.APPLICATION_JSON).content(inputAllowList))
                .andExpect(status)
                .andExpect(content().json(expectedAllowList));
    }

    @Test
    void getAllowList() throws Exception {
        mvc.perform(get("/v1/cm/lte/allowlist").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "Enodebs": [],
                            "EUtranCells": []
                        }
                        """));
    }
}
