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
package com.ericsson.oss.apps.controller.reports;

import com.ericsson.oss.apps.CoreApplication;
import com.ericsson.oss.apps.model.mom.EndcDistrProfile;
import com.ericsson.oss.apps.model.mom.GUtranSyncSignalFrequency;
import com.ericsson.oss.apps.model.report.AllowedEUtranCellReportTuple;
import com.ericsson.oss.apps.model.report.AllowedMoReportTuple;
import com.ericsson.oss.apps.model.report.CellDataReportTuple;
import com.ericsson.oss.apps.model.report.EndcDistrProfileDataStatus;
import com.ericsson.oss.apps.model.report.Report;
import com.ericsson.oss.apps.model.report.SCellReportTuple;
import com.ericsson.oss.apps.model.report.ScgReportTuple;
import com.ericsson.oss.apps.repository.ReportAllowEUtranCellsRepo;
import com.ericsson.oss.apps.repository.ReportAllowListRepo;
import com.ericsson.oss.apps.repository.ReportCellDataRepo;
import com.ericsson.oss.apps.repository.ReportDataRepo;
import com.ericsson.oss.apps.repository.ReportSCellRepo;
import com.ericsson.oss.apps.repository.ReportSecondaryCellGroupRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {CoreApplication.class, ReportsApiControllerImpl.class})
public class ReportsApiControllerImplTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    @Autowired
    private ReportDataRepo reportDataRepo;
    @Autowired
    private ReportAllowListRepo allowListRepo;
    @Autowired
    private ReportAllowEUtranCellsRepo allowEutranCellsRepo;
    @Autowired
    private ReportCellDataRepo cellDataRepo;
    @Autowired
    private ReportSecondaryCellGroupRepo scgDataRepo;
    @Autowired
    private ReportSCellRepo sCellRepo;

    private static final String JSON_REPORT_FILE = "./src/test/resources/__files/reports/reports.json";
    private static final String REPORT_URL = "/v1/reports";

    private static final long ROP_TIMESTAMP1 = 1674817560000L;
    private static final long ROP_TIMESTAMP2 = 1674818460000L;

    private static final String ME_FDN = "SubNetwork=A,SubNetwork=B,MeContext=C,ManagedElement=D";
    private static final String ENB_FDN1 = ME_FDN + ",ENodeBFunction=1";
    private static final String ENB_FDN2 = ME_FDN + ",ENodeBFunction=2";
    private static final String CELL_FDN1 = ENB_FDN1 + ",EUtranCellFDD=1";
    private static final String CELL_FDN2 = ENB_FDN1 + ",EUtranCellTDD=1";
    private static final String CELL_FDN3 = ENB_FDN2 + ",EUtranCellFDD=1";

    private static final String GNB1_FDN = ME_FDN + ",GNBCUCPFunction=1";
    private static final String NR_CELL_FDN10 = GNB1_FDN + ",NRCellCU=10";
    private static final String NR_CELL_FDN11 = GNB1_FDN + ",NRCellCU=11";
    private static final String NR_CELL_FDN20 = GNB1_FDN + ",NRCellCU=20";
    private static final String NR_CELL_FDN21 = GNB1_FDN + ",NRCellCU=21";
    private static final String NR_CELL_FDN30 = GNB1_FDN + ",NRCellCU=30";
    private static final String NR_CELL_FDN31 = GNB1_FDN + ",NRCellCU=31";
    private static final String NR_CELL_FDN40 = GNB1_FDN + ",NRCellCU=40";
    private static final String NR_CELL_FDN41 = GNB1_FDN + ",NRCellCU=41";
    private static final String NR_CELL_FDN50 = GNB1_FDN + ",NRCellCU=50";
    private static final String NR_CELL_FDN51 = GNB1_FDN + ",NRCellCU=51";
    private static final String NR_CELL_FDN60 = GNB1_FDN + ",NRCellCU=60";
    private static final String NR_CELL_FDN61 = GNB1_FDN + ",NRCellCU=61";

    private static final String PROFILE_FDN_1 = ENB_FDN1 + ",EndcDistrProfile=1";
    private static final String PROFILE_FDN_2 = ENB_FDN1 + ",EndcDistrProfile=rApp_2";
    private static final String PROFILE_FDN_3 = ENB_FDN2 + ",EndcDistrProfile=rApp_1";

    private static final String GU_NETWORK_FDN1 = ENB_FDN1 + ",GUtraNetwork=1";
    private static final String GU_NETWORK_FDN2 = ENB_FDN2 + ",GUtraNetwork=1";
    private static final String FREQ_FDN_11 = GU_NETWORK_FDN1 + ",GUtranSyncSignalFrequency=1";
    private static final String FREQ_FDN_12 = GU_NETWORK_FDN1 + ",GUtranSyncSignalFrequency=2";
    private static final String FREQ_FDN_13 = GU_NETWORK_FDN1 + ",GUtranSyncSignalFrequency=3";
    private static final String FREQ_FDN_21 = GU_NETWORK_FDN2 + ",GUtranSyncSignalFrequency=1";
    private static final String FREQ_FDN_22 = GU_NETWORK_FDN2 + ",GUtranSyncSignalFrequency=2";

    private static final Integer ARFCN11 = 620000;
    private static final Integer ARFCN12 = 620100;
    private static final Integer ARFCN13 = 620200;
    private static final Integer ARFCN21 = 621000;
    private static final Integer ARFCN22 = 622000;

    private static GUtranSyncSignalFrequency freq11 = new GUtranSyncSignalFrequency(FREQ_FDN_11);
    private static GUtranSyncSignalFrequency freq12 = new GUtranSyncSignalFrequency(FREQ_FDN_12);
    private static GUtranSyncSignalFrequency freq13 = new GUtranSyncSignalFrequency(FREQ_FDN_13);
    private static GUtranSyncSignalFrequency freq21 = new GUtranSyncSignalFrequency(FREQ_FDN_21);
    private static GUtranSyncSignalFrequency freq22 = new GUtranSyncSignalFrequency(FREQ_FDN_22);

    private static AllowedMoReportTuple allowListItem11 = new AllowedMoReportTuple(ROP_TIMESTAMP1, CELL_FDN1);
    private static AllowedMoReportTuple allowListItem12 = new AllowedMoReportTuple(ROP_TIMESTAMP1, CELL_FDN2);
    private static AllowedMoReportTuple allowListItem13 = new AllowedMoReportTuple(ROP_TIMESTAMP1, ENB_FDN2);
    private static AllowedMoReportTuple allowListItem21 = new AllowedMoReportTuple(ROP_TIMESTAMP2, CELL_FDN1);
    private static AllowedMoReportTuple allowListItem22 = new AllowedMoReportTuple(ROP_TIMESTAMP2, CELL_FDN2);
    private static AllowedMoReportTuple allowListItem23 = new AllowedMoReportTuple(ROP_TIMESTAMP2, ENB_FDN2);

    private static SCellReportTuple sCell110 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN10, 10F, 110F);
    private static SCellReportTuple sCell111 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN11, 10F, 111F);
    private static SCellReportTuple sCell120 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN20, 20F, 120F);
    private static SCellReportTuple sCell121 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN21, 20F, 121F);
    private static SCellReportTuple sCell130 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN30, 30F, 130F);
    private static SCellReportTuple sCell131 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN31, 30F, 131F);
    private static SCellReportTuple sCell140 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN40, 40F, 140F);
    private static SCellReportTuple sCell141 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN41, 40F, 141F);
    private static SCellReportTuple sCell150 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN50, 50F, 150F);
    private static SCellReportTuple sCell151 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN51, 50F, 151F);
    private static SCellReportTuple sCell160 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN60, 60F, 160F);
    private static SCellReportTuple sCell161 = new SCellReportTuple(ROP_TIMESTAMP1, NR_CELL_FDN61, 60F, 161F);
    private static SCellReportTuple sCell210 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN10, 10F, 210F);
    private static SCellReportTuple sCell211 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN11, 10F, 211F);
    private static SCellReportTuple sCell220 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN20, 20F, 220F);
    private static SCellReportTuple sCell221 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN21, 20F, 221F);
    private static SCellReportTuple sCell230 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN30, 30F, 230F);
    private static SCellReportTuple sCell231 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN31, 30F, 231F);
    private static SCellReportTuple sCell240 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN40, 40F, 240F);
    private static SCellReportTuple sCell241 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN41, 40F, 241F);
    private static SCellReportTuple sCell250 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN50, 50F, 250F);
    private static SCellReportTuple sCell251 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN51, 50F, 251F);
    private static SCellReportTuple sCell260 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN60, 60F, 260F);
    private static SCellReportTuple sCell261 = new SCellReportTuple(ROP_TIMESTAMP2, NR_CELL_FDN61, 60F, 261F);

    private static ScgReportTuple scg111 = new ScgReportTuple(ROP_TIMESTAMP1, CELL_FDN1, ARFCN11);
    private static ScgReportTuple scg112 = new ScgReportTuple(ROP_TIMESTAMP1, CELL_FDN1, ARFCN12);
    private static ScgReportTuple scg121 = new ScgReportTuple(ROP_TIMESTAMP1, CELL_FDN2, ARFCN11);
    private static ScgReportTuple scg122 = new ScgReportTuple(ROP_TIMESTAMP1, CELL_FDN2, ARFCN13);
    private static ScgReportTuple scg131 = new ScgReportTuple(ROP_TIMESTAMP1, CELL_FDN3, ARFCN21);
    private static ScgReportTuple scg132 = new ScgReportTuple(ROP_TIMESTAMP1, CELL_FDN3, ARFCN22);
    private static ScgReportTuple scg211 = new ScgReportTuple(ROP_TIMESTAMP2, CELL_FDN1, ARFCN11);
    private static ScgReportTuple scg212 = new ScgReportTuple(ROP_TIMESTAMP2, CELL_FDN1, ARFCN12);
    private static ScgReportTuple scg221 = new ScgReportTuple(ROP_TIMESTAMP2, CELL_FDN2, ARFCN11);
    private static ScgReportTuple scg222 = new ScgReportTuple(ROP_TIMESTAMP2, CELL_FDN2, ARFCN13);
    private static ScgReportTuple scg231 = new ScgReportTuple(ROP_TIMESTAMP2, CELL_FDN3, ARFCN21);
    private static ScgReportTuple scg232 = new ScgReportTuple(ROP_TIMESTAMP2, CELL_FDN3, ARFCN22);

    private static AllowedEUtranCellReportTuple allowCellItem11 = new AllowedEUtranCellReportTuple(ROP_TIMESTAMP1, CELL_FDN1);
    private static AllowedEUtranCellReportTuple allowCellItem12 = new AllowedEUtranCellReportTuple(ROP_TIMESTAMP1, CELL_FDN2);
    private static AllowedEUtranCellReportTuple allowCellItem13 = new AllowedEUtranCellReportTuple(ROP_TIMESTAMP1, CELL_FDN3);
    private static AllowedEUtranCellReportTuple allowCellItem21 = new AllowedEUtranCellReportTuple(ROP_TIMESTAMP2, CELL_FDN1);
    private static AllowedEUtranCellReportTuple allowCellItem22 = new AllowedEUtranCellReportTuple(ROP_TIMESTAMP2, CELL_FDN2);
    private static AllowedEUtranCellReportTuple allowCellItem23 = new AllowedEUtranCellReportTuple(ROP_TIMESTAMP2, CELL_FDN3);

    private static CellDataReportTuple cellData11 = new CellDataReportTuple(ROP_TIMESTAMP1, CELL_FDN1);
    private static CellDataReportTuple cellData12 = new CellDataReportTuple(ROP_TIMESTAMP1, CELL_FDN2);
    private static CellDataReportTuple cellData13 = new CellDataReportTuple(ROP_TIMESTAMP1, CELL_FDN3);
    private static CellDataReportTuple cellData21 = new CellDataReportTuple(ROP_TIMESTAMP2, CELL_FDN1);
    private static CellDataReportTuple cellData22 = new CellDataReportTuple(ROP_TIMESTAMP2, CELL_FDN2);
    private static CellDataReportTuple cellData23 = new CellDataReportTuple(ROP_TIMESTAMP2, CELL_FDN3);

    private static EndcDistrProfile profile11 = new EndcDistrProfile(PROFILE_FDN_1);
    private static EndcDistrProfile profile12 = new EndcDistrProfile(PROFILE_FDN_2);
    private static EndcDistrProfile profile13 = new EndcDistrProfile(PROFILE_FDN_3);
    private static EndcDistrProfile profile21 = new EndcDistrProfile(PROFILE_FDN_1);

    private static String expectedJson;

    @BeforeAll
    void initialize() throws Exception {
        allowListItem11.setReadOnly(false);
        allowListItem11.setIsCell(true);
        allowListItem11.setIsTdd(false);
        allowListItem11.setIsBlocked(false);
        allowListItem12.setReadOnly(false);
        allowListItem12.setIsCell(true);
        allowListItem12.setIsTdd(true);
        allowListItem12.setIsBlocked(false);
        allowListItem13.setReadOnly(false);
        allowListItem13.setIsCell(false);
        allowListItem13.setIsTdd(false);
        allowListItem13.setIsBlocked(false);
        allowListItem21.setReadOnly(false);
        allowListItem21.setIsCell(true);
        allowListItem21.setIsTdd(false);
        allowListItem21.setIsBlocked(false);
        allowListItem22.setReadOnly(false);
        allowListItem22.setIsCell(true);
        allowListItem22.setIsTdd(true);
        allowListItem22.setIsBlocked(false);
        allowListItem23.setReadOnly(false);
        allowListItem23.setIsCell(false);
        allowListItem23.setIsTdd(false);
        allowListItem23.setIsBlocked(false);

        scg111.setTotalCapacity(20F);
        scg111.setTotalLoad(221F);
        scg112.setTotalCapacity(40F);
        scg112.setTotalLoad(241F);
        scg121.setTotalCapacity(60F);
        scg121.setTotalLoad(261F);
        scg122.setTotalCapacity(80F);
        scg122.setTotalLoad(281F);
        scg131.setTotalCapacity(100F);
        scg131.setTotalLoad(301F);
        scg132.setTotalCapacity(120F);
        scg132.setTotalLoad(321F);
        scg211.setTotalCapacity(20F);
        scg211.setTotalLoad(421F);
        scg212.setTotalCapacity(40F);
        scg212.setTotalLoad(441F);
        scg221.setTotalCapacity(60F);
        scg221.setTotalLoad(461F);
        scg222.setTotalCapacity(80F);
        scg222.setTotalLoad(481F);
        scg231.setTotalCapacity(100F);
        scg231.setTotalLoad(501F);
        scg232.setTotalCapacity(120F);
        scg232.setTotalLoad(521F);

        scg111.setSCells(List.of(sCell110, sCell111));
        scg112.setSCells(List.of(sCell120, sCell121));
        scg121.setSCells(List.of(sCell130, sCell131));
        scg122.setSCells(List.of(sCell140, sCell141));
        scg131.setSCells(List.of(sCell150, sCell151));
        scg132.setSCells(List.of(sCell160, sCell161));
        scg211.setSCells(List.of(sCell210, sCell211));
        scg212.setSCells(List.of(sCell220, sCell221));
        scg221.setSCells(List.of(sCell230, sCell231));
        scg222.setSCells(List.of(sCell240, sCell241));
        scg231.setSCells(List.of(sCell250, sCell251));
        scg232.setSCells(List.of(sCell260, sCell261));

        allowCellItem11.setScgData(List.of(scg111, scg112));
        allowCellItem12.setScgData(List.of(scg121, scg122));
        allowCellItem13.setScgData(List.of(scg131, scg132));
        allowCellItem21.setScgData(List.of(scg211, scg212));
        allowCellItem22.setScgData(List.of(scg221, scg222));
        allowCellItem23.setScgData(List.of(scg231, scg232));

        freq11.setArfcn(ARFCN11);
        freq11.setBand(77);
        freq12.setArfcn(ARFCN12);
        freq12.setBand(77);
        freq13.setArfcn(ARFCN13);
        freq13.setBand(77);
        freq21.setArfcn(ARFCN21);
        freq21.setBand(77);
        freq22.setArfcn(ARFCN22);
        freq22.setBand(77);

        profile11.setEndcUserThreshold(0);
        profile11.setGUtranFreqDistribution(List.of(50, 50));
        profile11.setGUtranFreqRef(List.of(freq11, freq12));
        profile12.setEndcUserThreshold(0);
        profile12.setGUtranFreqDistribution(List.of(30, 70));
        profile12.setGUtranFreqRef(List.of(freq11, freq13));
        profile13.setEndcUserThreshold(0);
        profile13.setGUtranFreqDistribution(List.of(30, 70));
        profile13.setGUtranFreqRef(List.of(freq21, freq22));
        profile21.setEndcUserThreshold(0);
        profile21.setGUtranFreqDistribution(List.of(57, 43));
        profile21.setGUtranFreqRef(List.of(freq11, freq12));

        cellData11.setOldProfileRef(PROFILE_FDN_1);
        cellData11.setProfileToWrite(profile11);
        cellData11.setStatus(EndcDistrProfileDataStatus.SUCCESS);
        cellData12.setOldProfileRef(PROFILE_FDN_1);
        cellData12.setProfileToWrite(profile12);
        cellData12.setNewProfileCreated(true);
        cellData12.setStatus(EndcDistrProfileDataStatus.SUCCESS);
        cellData13.setProfileToWrite(profile13);
        cellData13.setStatus(EndcDistrProfileDataStatus.FAILED_AT_CREATING_PROFILE);
        cellData21.setOldProfileRef(PROFILE_FDN_1);
        cellData21.setProfileToWrite(profile21);
        cellData21.setStatus(EndcDistrProfileDataStatus.SUCCESS);
        cellData22.setOldProfileRef(PROFILE_FDN_2);
        cellData22.setProfileToWrite(profile12);
        cellData22.setStatus(EndcDistrProfileDataStatus.UNCHANGED);
        cellData23.setProfileToWrite(profile13);
        cellData23.setNewProfileCreated(true);
        cellData23.setStatus(EndcDistrProfileDataStatus.SUCCESS);

        Report report1 = new Report(ROP_TIMESTAMP1);
        report1.setAllowList(List.of(allowListItem11, allowListItem12, allowListItem13));
        report1.setAllowEutranCells(List.of(allowCellItem11, allowCellItem12, allowCellItem13));
        report1.setCellData(List.of(cellData11, cellData12, cellData13));
        Report report2 = new Report(ROP_TIMESTAMP2);
        report2.setAllowList(List.of(allowListItem21, allowListItem22, allowListItem23));
        report2.setAllowEutranCells(List.of(allowCellItem21, allowCellItem22, allowCellItem23));
        report2.setCellData(List.of(cellData21, cellData22, cellData23));


        sCellRepo.saveAll(List.of(sCell110, sCell111, sCell120, sCell121, sCell130, sCell131,
                                  sCell140, sCell141, sCell150, sCell151, sCell160, sCell161,
                                  sCell210, sCell211, sCell220, sCell221, sCell230, sCell231,
                                  sCell240, sCell241, sCell250, sCell251, sCell260, sCell261));
        scgDataRepo.saveAll(List.of(scg111, scg112, scg121, scg122, scg131, scg132,
                                    scg211, scg212, scg221, scg222, scg231, scg232));
        cellDataRepo.saveAll(List.of(cellData11, cellData12, cellData13, cellData21, cellData22, cellData23));
        allowEutranCellsRepo.saveAll(List.of(allowCellItem11, allowCellItem12, allowCellItem13,
                                             allowCellItem21, allowCellItem22, allowCellItem23));
        allowListRepo.saveAll(List.of(allowListItem11, allowListItem12, allowListItem13,
                                      allowListItem21, allowListItem22, allowListItem23));
        reportDataRepo.saveAll(List.of(report1, report2));

        expectedJson = new String(Files.readAllBytes(Paths.get(JSON_REPORT_FILE)));
    }

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getLatestReports() throws Exception {
        mvc.perform(get(REPORT_URL).contentType(MediaType.APPLICATION_JSON).content("{\"nRops\": 100}"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getReportsBetween() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(expectedJson).get(String.valueOf(ROP_TIMESTAMP2));
        String expectedResult = "{\"" + ROP_TIMESTAMP2 + "\":" + node.toString() + "}";

        mvc.perform(get(REPORT_URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"startTimeStamp\":" + (ROP_TIMESTAMP2 - 1000) + ", \"endTimeStamp\":" + (ROP_TIMESTAMP2 + 1000) + "}" ))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content().json(expectedResult));
    }

    @Test
    void getReportsBefore() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(expectedJson).get(String.valueOf(ROP_TIMESTAMP1));
        String expectedResult = "{\"" + ROP_TIMESTAMP1 + "\":" + node.toString() + "}";

        mvc.perform(get(REPORT_URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"endTimeStamp\":" + (ROP_TIMESTAMP2 - 1000) + "}" ))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content().json(expectedResult));
    }

    @Test
    void getReportsAfter() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(expectedJson).get(String.valueOf(ROP_TIMESTAMP1));
        String expectedResult = "{\"" + ROP_TIMESTAMP1 + "\":" + node.toString() + "}";

        mvc.perform(get(REPORT_URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"startTimeStamp\":" + ROP_TIMESTAMP1 + "}" ))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content().json(expectedResult));
    }

    @Test
    void getReportsError() throws Exception {
        mvc.perform(get(REPORT_URL).contentType(MediaType.APPLICATION_JSON)
                .content("{\"startTimeStamp\":" + (ROP_TIMESTAMP2 + 1000) + "}" ))
                .andExpect(status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }
}
