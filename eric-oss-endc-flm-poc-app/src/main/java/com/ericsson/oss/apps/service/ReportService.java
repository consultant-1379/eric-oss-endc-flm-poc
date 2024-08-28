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

import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.ericsson.oss.apps.model.EndcFreqProfileData;
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.model.mom.EUtranCell;
import com.ericsson.oss.apps.model.mom.EndcDistrProfile;
import com.ericsson.oss.apps.model.report.AllowedEUtranCellReportTuple;
import com.ericsson.oss.apps.model.report.AllowedMoReportTuple;
import com.ericsson.oss.apps.model.report.CellDataReportTuple;
import com.ericsson.oss.apps.model.report.EndcDistrProfileDataStatus;
import com.ericsson.oss.apps.model.report.Report;
import com.ericsson.oss.apps.model.report.SCellReportTuple;
import com.ericsson.oss.apps.model.report.ScgReportTuple;
import com.ericsson.oss.apps.repository.CmEUtranCellRepo;
import com.ericsson.oss.apps.repository.ReportAllowEUtranCellsRepo;
import com.ericsson.oss.apps.repository.ReportAllowListRepo;
import com.ericsson.oss.apps.repository.ReportCellDataRepo;
import com.ericsson.oss.apps.repository.ReportDataRepo;
import com.ericsson.oss.apps.repository.ReportSCellRepo;
import com.ericsson.oss.apps.repository.ReportSecondaryCellGroupRepo;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportDataRepo reportDataRepo;
    private final ReportAllowListRepo allowListRepo;
    private final ReportAllowEUtranCellsRepo allowEutranCellsRepo;
    private final ReportCellDataRepo cellDataRepo;
    private final ReportSecondaryCellGroupRepo scgDataRepo;
    private final ReportSCellRepo sCellRepo;

    private final CmEUtranCellRepo eUtranCellRepo;

    private final CellCapacityService cellCapacityService;
    private final WeightedAverageRRCService weightedAverageRRCService;

    @Transactional
    public void createNewReport(long ropTimeStamp, List<AllowedMo> allowList,
            Map<String, List<SecondaryCellGroup>> scgMap, Map<String, EndcFreqProfileData> cellToProfileMap) {
        Report report = new Report(ropTimeStamp);

        log.debug("Creating report for ROP: {}", ropTimeStamp);

        report.setAllowList(constructAllowedMoReportTuples(ropTimeStamp, allowList));
        report.setAllowEutranCells(constructAllowedEUtranCellReportTuples(ropTimeStamp, scgMap));
        report.setCellData(constructCellDataReportTuples(ropTimeStamp, scgMap, cellToProfileMap));

        reportDataRepo.save(report);

        // clean up capacity and load data, which are calcuated per-ROP.
        cellCapacityService.setFreqCapacityMap(new HashMap<>());
        cellCapacityService.setNrCellCapacityMap(new HashMap<>());
        weightedAverageRRCService.setFreqLoadMap(new HashMap<>());
        weightedAverageRRCService.setNrCellLoadMap(new HashMap<>());
    }

    @Transactional
    public Report getReportByRop(long ropTimeStamp) {
        Optional<Report> reportInDb = reportDataRepo.findById(ropTimeStamp);

        if (reportInDb.isPresent()) {
            return reportInDb.get();
        }

        return null;
    }

    @Transactional
    public void updateReportCellData(CellDataReportTuple cellData) {
        cellDataRepo.save(cellData);
    }

    private List<AllowedMoReportTuple> constructAllowedMoReportTuples(long ropTimeStamp, List<AllowedMo> allowList) {
        List<AllowedMoReportTuple> allowedMos = allowList.stream()
                .map(allowedMo -> allowedMoToReportTuple(ropTimeStamp, allowedMo))
                .toList();

        if (!allowedMos.isEmpty()) {
            allowListRepo.saveAll(allowedMos);
        }

        return allowedMos;
    }

    private AllowedMoReportTuple allowedMoToReportTuple(long ropTimeStamp, AllowedMo allowedMo) {
        AllowedMoReportTuple tuple = new AllowedMoReportTuple(ropTimeStamp, allowedMo.getObjectId().toString());

        tuple.setReadOnly(allowedMo.getReadOnly());
        tuple.setIsCell(allowedMo.getIsCell());
        tuple.setIsTdd(allowedMo.getIsTdd());
        tuple.setIsBlocked(allowedMo.getIsBlocked());

        return tuple;
    }

    private List<AllowedEUtranCellReportTuple> constructAllowedEUtranCellReportTuples(long ropTimeStamp,
            Map<String, List<SecondaryCellGroup>> scgMap) {
        List<AllowedEUtranCellReportTuple> cells = scgMap.entrySet().stream()
                .map(entry -> constructEUtranCellTuple(ropTimeStamp, entry.getKey(), entry.getValue()))
                .toList();

        if (!cells.isEmpty()) {
            allowEutranCellsRepo.saveAll(cells);
        }

        return cells;
    }

    private AllowedEUtranCellReportTuple constructEUtranCellTuple(long ropTimeStamp, String objectFdn, List<SecondaryCellGroup> scgList) {
        AllowedEUtranCellReportTuple tuple = new AllowedEUtranCellReportTuple(ropTimeStamp, objectFdn);

        // Data in scgList are stored as SCG per psCell, convert it into SCG per Frequency.
        Map<Integer, HashSet<String>> arfcnToSCellMap = new HashMap<>();

        for (SecondaryCellGroup scg: scgList) {
            Integer arfcn = scg.arfcn();

            arfcnToSCellMap.computeIfAbsent(arfcn, k -> new HashSet<>());

            HashSet<String> sCellSet = arfcnToSCellMap.get(arfcn);
            sCellSet.addAll(scg.secondaryCells().stream().map(ManagedObject::getFdn).toList());
            sCellSet.add(scg.primaryNRCell().getFdn());
        }

        List<ScgReportTuple> scgReportTuples = arfcnToSCellMap.entrySet().stream()
                .map(entry -> constructSCellTuples(ropTimeStamp, objectFdn, entry.getKey(), entry.getValue()))
                .toList();

        if (!scgReportTuples.isEmpty()) {
            scgDataRepo.saveAll(scgReportTuples);
            tuple.setScgData(scgReportTuples);
        }

        return tuple;
    }

    private ScgReportTuple constructSCellTuples(long ropTimeStamp, String pCellFdn, Integer arfcn, HashSet<String> scells) {
        ScgReportTuple scgTuple = new ScgReportTuple(ropTimeStamp, pCellFdn, arfcn);

        final Float totalCapacity = cellCapacityService.getFreqCapacityMap().getOrDefault(pCellFdn, new HashMap<>()).getOrDefault(arfcn, 0F);
        final Float totalLoad = weightedAverageRRCService.getFreqLoadMap().getOrDefault(pCellFdn, new HashMap<>()).getOrDefault(arfcn, 0F);

        scgTuple.setTotalCapacity(totalCapacity);
        scgTuple.setTotalLoad(totalLoad);

        List<SCellReportTuple> sCellTuples = scells.stream()
                .map(sCellFdn -> new SCellReportTuple(ropTimeStamp, sCellFdn,
                        cellCapacityService.getNrCellCapacityMap().getOrDefault(pCellFdn, new HashMap<>()).getOrDefault(sCellFdn, 0F),
                        weightedAverageRRCService.getNrCellLoadMap().getOrDefault(pCellFdn, new HashMap<>()).getOrDefault(sCellFdn, 0F)))
                .toList();

        if (!sCellTuples.isEmpty()) {
            sCellRepo.saveAll(sCellTuples);
            scgTuple.setSCells(sCellTuples);
        }

        return scgTuple;
    }

    private List<CellDataReportTuple> constructCellDataReportTuples(long ropTimeStamp,
            Map<String, List<SecondaryCellGroup>> scgMap, Map<String, EndcFreqProfileData> cellToProfileMap) {
        List<CellDataReportTuple> cellData = new ArrayList<>();

        List<EUtranCell> allowedEutranCells = eUtranCellRepo.findAllById(scgMap.entrySet().stream().map(Map.Entry::getKey).map(ManagedObjectId::of).toList());

        for (EUtranCell cell: allowedEutranCells) {
            String cellFdn = cell.getFdn();
            if (cellToProfileMap.containsKey(cellFdn)) {
                cellData.add(profileDataToReportTuple(ropTimeStamp, cell, cellToProfileMap.get(cellFdn)));
            }
            else {
                // For cells not in cellToProfileMap, copy the existing profile into report.
                CellDataReportTuple tuple = new CellDataReportTuple(ropTimeStamp, cellFdn);
                EndcDistrProfile oldProfile = cell.getEndcDistrProfileRef();
                tuple.setProfileToWrite(oldProfile);
                tuple.setStatus(EndcDistrProfileDataStatus.UNCHANGED);
                if (oldProfile != null) {
                    tuple.setOldProfileRef(oldProfile.getFdn());
                }
                cellData.add(tuple);
            }
        }

        if (!cellData.isEmpty()) {
            cellDataRepo.saveAll(cellData);
        }

        return cellData;
    }

    private CellDataReportTuple profileDataToReportTuple(long ropTimeStamp, EUtranCell cell, EndcFreqProfileData profileData) {
        CellDataReportTuple tuple = new CellDataReportTuple(ropTimeStamp, cell.getFdn());

        EndcDistrProfile profile = cell.getEndcDistrProfileRef();
        if (profile != null) {
            tuple.setOldProfileRef(profile.getFdn());
        }
        else {
            // By this time there is no ManagedObjectId generated for the new profile.
            log.debug("Create an empty profile structure");
            profile = new EndcDistrProfile();
        }

        profile.setGUtranFreqRef(profileData.gUtranFreqRef());
        profile.setMandatoryGUtranFreqRef(profileData.mandatoryGUtranFreqRef());

        List<Integer> distributions = profileData.gUtranFreqRef().stream()
            .map(freq -> profileData.freqDistributionMapping().get(freq.getFdn()))
            .toList();
        profile.setGUtranFreqDistribution(distributions);

        tuple.setProfileToWrite(profile);

        return tuple;
    }
}
