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

import com.ericsson.oss.apps.api.controller.ReportsApi;
import com.ericsson.oss.apps.api.model.AllowListReportData;
import com.ericsson.oss.apps.api.model.CellReportData;
import com.ericsson.oss.apps.api.model.CellReportDataProfileData;
import com.ericsson.oss.apps.api.model.EndcDistrProfileReportData;
import com.ericsson.oss.apps.api.model.GUtranSyncSignalFrequencyReportData;
import com.ericsson.oss.apps.api.model.ReportData;
import com.ericsson.oss.apps.api.model.ReportDataRequest;
import com.ericsson.oss.apps.api.model.SCellReportDataValue;
import com.ericsson.oss.apps.api.model.ScgReportData;
import com.ericsson.oss.apps.model.mom.EndcDistrProfile;
import com.ericsson.oss.apps.model.mom.GUtranSyncSignalFrequency;
import com.ericsson.oss.apps.model.report.AllowedEUtranCellReportTuple;
import com.ericsson.oss.apps.model.report.AllowedMoReportTuple;
import com.ericsson.oss.apps.model.report.CellDataReportTuple;
import com.ericsson.oss.apps.model.report.Report;
import com.ericsson.oss.apps.model.report.SCellReportTuple;
import com.ericsson.oss.apps.model.report.ScgReportTuple;
import com.ericsson.oss.apps.repository.ReportDataRepo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ReportsApiControllerImpl implements ReportsApi {
    private final ReportDataRepo reportDataRepo;

    @Override
    public ResponseEntity<Map<String, ReportData>> getReports(@Valid ReportDataRequest reportDataRequest) {
        Integer nRops = reportDataRequest.getnRops();
        Long startTime = reportDataRequest.getStartTimeStamp();
        Long endTime = reportDataRequest.getEndTimeStamp();

        log.info("ReportDataRequest: nRops={}, startTimeStamp={}, endTimeStamp={}", nRops, startTime, endTime);

        Optional<List<Report>> reportsInDb;

        // TODO - need revisit the parameter list of the REST API.
        if (startTime == null && endTime == null) {
            reportsInDb = reportDataRepo.getLatestReports(nRops);
        }
        else {
            if (startTime != null && endTime != null) {
                reportsInDb = reportDataRepo.getReportsBetween(startTime, endTime);
            }
            else {
                if (startTime != null) {
                    reportsInDb = reportDataRepo.getReportsAfter(nRops, startTime);
                }
                else {
                    reportsInDb = reportDataRepo.getReportsBefore(nRops, endTime);
                }
            }
        }

        if (reportsInDb.isPresent() && !reportsInDb.get().isEmpty()) {
            Map<String, ReportData> respData = reportsInDb.get().stream()
                    .collect(Collectors.toMap(report -> String.valueOf(report.getRopTimeStamp()), this::constructReportData));
            return ResponseEntity.ok(respData);
        }

        log.info("Reports not found with parameter: nRops={}, startTimeStamp={}, endTimeStamp={}", nRops, startTime, endTime);
        return new ResponseEntity<>(HttpStatusCode.valueOf(400));
    }

    private ReportData constructReportData(Report report) {
        List<AllowListReportData> allowListReportData = report.getAllowList().stream()
                .map(this::constructAllowListReportData)
                .toList();

        Map<String, AllowedEUtranCellReportTuple> scgListMap = report.getAllowEutranCells().stream()
                .collect(Collectors.toMap(tuple -> tuple.getReportDataId().getObjectFdn(), tuple -> tuple));

        Map<String, CellReportData> cellReportDataMap = report.getCellData().stream()
                .collect(Collectors.toMap(tuple -> tuple.getReportDataId().getObjectFdn(),
                                          tuple -> new CellReportData()
                                                .scgList(constructScgList(scgListMap.get(tuple.getReportDataId().getObjectFdn())))
                                                .profileData(constructProfileData(tuple))));

        return new ReportData()
                .allowList(allowListReportData)
                .cellData(cellReportDataMap);
    }

    private AllowListReportData constructAllowListReportData(AllowedMoReportTuple reportTuple) {
        return new AllowListReportData()
                .fdn(reportTuple.getReportDataId().getObjectFdn())
                .isBlocked(reportTuple.getIsBlocked());
    }

    private List<ScgReportData> constructScgList(AllowedEUtranCellReportTuple cellTuple) {
        return cellTuple.getScgData().stream()
                .map(this::constructScgReportData)
                .toList();
    }

    private ScgReportData constructScgReportData(ScgReportTuple scgTuple) {
        return new ScgReportData()
                .arfcn(scgTuple.getScgDataId().getArfcn())
                .freqTag(getFreqTag(scgTuple.getScgDataId().getArfcn()))
                .totalCapacity(scgTuple.getTotalCapacity().doubleValue())
                .totalLoad(scgTuple.getTotalLoad().doubleValue())
                .sCells(scgTuple.getSCells().stream().map(sCell -> Map.of(sCell.getReportDataId().getObjectFdn(), constructSCellReportDataValue(sCell))).toList());
    }

    private SCellReportDataValue constructSCellReportDataValue(SCellReportTuple scellTuple) {
        return new SCellReportDataValue()
                .capacity(scellTuple.getCellCapacity().doubleValue())
                .load(scellTuple.getCellLoad().doubleValue());
    }

    private String getFreqTag(Integer arfcn) {
        // Construct frequency tag like FR1_n77_3300MHz
        final String FREQ_TAG_FORMAT = "%s_n%d_%.0fMHz";

        String freqType = NrArfcnUtils.nrArfcnFreqType(arfcn);
        int band = NrArfcnUtils.getFirstBand(arfcn);
        double freq = NrArfcnUtils.nrArfcnToFreq(arfcn);

        return String.format(FREQ_TAG_FORMAT, freqType, band, freq);
    }

    private CellReportDataProfileData constructProfileData(CellDataReportTuple profileTuple) {
        return new CellReportDataProfileData()
                .oldProfileRef(profileTuple.getOldProfileRef())
                .newProfile(constructProfile(profileTuple.getProfileToWrite()))
                .newProfileCreated(profileTuple.getNewProfileCreated())
                .status(CellReportDataProfileData.StatusEnum.valueOf(profileTuple.getStatus().toString()));
    }

    private EndcDistrProfileReportData constructProfile(EndcDistrProfile profile) {
        return new EndcDistrProfileReportData()
                .fdn(profile.getFdn())
                .endcUserThreshold(profile.getEndcUserThreshold())
                .gUtranFreqDistribution(profile.getGUtranFreqDistribution())
                .gUtranFreqRef(profile.getGUtranFreqRef().stream().map(this::constructFrequencyData).toList())
                .mandatoryGUtranFreqRef(profile.getMandatoryGUtranFreqRef().stream().map(this::constructFrequencyData).toList());
    }

    private GUtranSyncSignalFrequencyReportData constructFrequencyData(GUtranSyncSignalFrequency frequency) {
        return new GUtranSyncSignalFrequencyReportData()
                .fdn(frequency.getFdn())
                .arfcn(frequency.getArfcn())
                .band(frequency.getBand());
    }
}
