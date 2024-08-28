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

import com.ericsson.oss.apps.api.model.AllowList;
import com.ericsson.oss.apps.api.model.EUtranCell;
import com.ericsson.oss.apps.model.entities.AllowedMo;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class CmService {

    private final CmAllowListRepo cmAllowListRepo;

    private final CmEndcDistrProfileRepo cmEndcDistrProfileRepo;
    private final CmEUtranCellRepo cmEUtranCellRepo;
    private final CmExternalGNBCUCPFunctionRepo cmExternalGNBCUCPFunctionRepo;
    private final CmExternalGNodeBFunctionRepo cmExternalGNodeBFunctionRepo;
    private final CmExternalGUtranCellRepo cmExternalGUtranCellRepo;
    private final CmExternalNRCellCURepo cmExternalNRCellCURepo;
    private final CmGNBCUCPFunctionRepo cmGNBCUCPFunctionRepo;
    private final CmGUtranCellRelationRepo cmGUtranCellRelationRepo;
    private final CmGUtranFreqRelationRepo cmGUtranFreqRelationRepo;
    private final CmGUtranSyncSignalFrequencyRepo cmGUtranSyncSignalFrequencyRepo;
    private final CmNRCellCURepo cmNRCellCURepo;
    private final CmNRCellDURepo cmNRCellDURepo;
    private final CmNRCellRelationRepo cmNRCellRelationRepo;
    private final CmNRSectorCarrierRepo cmNRSectorCarrierRepo;

    private static final Pattern tddFdnPattern = Pattern.compile(".*EUtranCellTDD=[\\-\\w]+$");

    public void cleanCmData() {
        cmEndcDistrProfileRepo.deleteAll();
        cmEUtranCellRepo.deleteAll();
        cmExternalGNBCUCPFunctionRepo.deleteAll();
        cmExternalGNodeBFunctionRepo.deleteAll();
        cmExternalGUtranCellRepo.deleteAll();
        cmExternalNRCellCURepo.deleteAll();
        cmGNBCUCPFunctionRepo.deleteAll();
        cmGUtranCellRelationRepo.deleteAll();
        cmGUtranFreqRelationRepo.deleteAll();
        cmGUtranSyncSignalFrequencyRepo.deleteAll();
        cmNRCellCURepo.deleteAll();
        cmNRCellDURepo.deleteAll();
        cmNRCellRelationRepo.deleteAll();
        cmNRSectorCarrierRepo.deleteAll();
    }

    @Transactional
    public List<AllowedMo> loadCmAllowListRepo() {
        return cmAllowListRepo.findAll();
    }

    public List<ManagedObjectId> getAllowedEutranCells(List<AllowedMo> allowList) {
        // find all eUtran cells from these allowed nodes in the list
        List<ManagedObjectId> eutranCellFromNode = allowList.stream()
                .filter(allowedMo -> !allowedMo.getIsBlocked())
                .filter(allowedMo -> !allowedMo.getIsCell())
                .map(AllowedMo::getObjectId)
                .flatMap(nodeID -> cmEUtranCellRepo.findAll().stream()
                        .map(ManagedObject::getObjectId)
                        .filter(objectId -> Objects.equals(nodeID, objectId.fetchParentId())))
                .distinct()
                .toList();
        // Collect allowed eUtran cells from the allowed list
        List<ManagedObjectId> eutranCellFromList = allowList.stream()
                .filter(allowedMo -> !allowedMo.getIsBlocked())
                .filter(AllowedMo::getIsCell)
                .map(AllowedMo::getObjectId)
                .toList();

        // merge two list and remove duplicated ones
        List<ManagedObjectId> allowEutranCells = Stream.concat(eutranCellFromList.stream(), eutranCellFromNode.stream())
                .distinct()
                .toList();

        log.debug("eutranCellFromNode {} eutranCellFromList {} allowEutranCells {}", eutranCellFromNode.size(), eutranCellFromList.size(), allowEutranCells.size());

        return allowEutranCells;
    }

    @Transactional
    public AllowList getAllowList() {
        AllowList allowList = new AllowList();

        allowList.setEutranCells(cmAllowListRepo.findByIsCell(true).stream()
                .flatMap(entity -> entity.asEutranCell().stream())
                .toList());
        allowList.setEnodebs(cmAllowListRepo.findByIsCell(false).stream()
                .flatMap(entity -> entity.asEnodeb().stream())
                .toList());

        return allowList;
    }

    @Transactional
    public AllowList saveAllowList(AllowList allowList) {

        List<AllowedMo> allowedMos = new java.util.ArrayList<>((allowList.getEnodebs().stream()
                .map(enodeb -> new AllowedMo(new ManagedObjectId(enodeb.getFdn()), enodeb.getReadOnly(), false, false))
                .toList()));

        allowedMos.addAll(allowList.getEutranCells().stream()
                .map(this::configureCell)
                .toList());

        cmAllowListRepo.deleteAll();
        cmAllowListRepo.saveAll(allowedMos);
        return allowList;
    }

    private AllowedMo configureCell(EUtranCell eUtranCell) {
        Boolean isTDD = tddFdnPattern.matcher(eUtranCell.getFdn()).matches();
        return new AllowedMo(new ManagedObjectId(eUtranCell.getFdn()), eUtranCell.getReadOnly(), true, isTDD);
    }
}