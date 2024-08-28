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

import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.common.topology.CommonTopologyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(TopologyApiControllerImpl.class)
public class TopologyApiControllerImplTest {

    private static final String TEST_FDN = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=NETSimW,ManagedElement=LTE74dg2ERBST00064,ENodeBFunction=1";
    private static final String TEST_CELL_FDN = "SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=NETSimW,ManagedElement=LTE74dg2ERBST00064,ENodeBFunction=1,EUtranCellFDD=LTE63dg2ERBST00064-1";

    @MockBean
    private CommonTopologyService commonTopologyService;
    @MockBean
    private WebClient webClient;

    @Autowired
    private MockMvc mvc;

    @Test
    void getAllNodeFdn() throws Exception {
        List<ManagedObjectId> nodes = List.of(ManagedObjectId.of(TEST_FDN));
        Mockito.when(commonTopologyService.fetchAllNodeFdn()).thenReturn(nodes);
        mvc.perform(get("/v1/topology/lte/nodes")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("""
                        [{"name":"LTE74dg2ERBST00064","fdn":"SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=NETSimW,ManagedElement=LTE74dg2ERBST00064,ENodeBFunction=1"}]"""));
        verify(commonTopologyService, times(1)).fetchAllNodeFdn();
    }

    @Test
    void getAllCellFdn() throws Exception {
        List<ManagedObjectId> cells = List.of(ManagedObjectId.of(TEST_CELL_FDN));
        Mockito.when(commonTopologyService.fetchAllCellFdn()).thenReturn(cells);
        mvc.perform(get("/v1/topology/lte/cells")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("""
                        [{"name":"LTE74dg2ERBST00064","fdn":"SubNetwork=Europe,SubNetwork=Ireland,SubNetwork=NETSimW,ManagedElement=LTE74dg2ERBST00064,ENodeBFunction=1,EUtranCellFDD=LTE63dg2ERBST00064-1"}]"""));
        verify(commonTopologyService, times(1)).fetchAllCellFdn();
    }
}

