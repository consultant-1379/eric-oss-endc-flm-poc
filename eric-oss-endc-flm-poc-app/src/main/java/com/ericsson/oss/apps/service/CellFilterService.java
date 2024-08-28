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

import com.ericsson.oss.apps.loader.CmDataLoader;
import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.GUtranRelationAggregate;
import com.ericsson.oss.apps.model.mom.*;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.*;
import com.ericsson.oss.apps.topology.IdentityService;
import com.ericsson.oss.apps.topology.model.NRCellId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@Getter
@RequiredArgsConstructor
public class CellFilterService {

    private final CmEUtranCellRepo cmEUtranCellRepo;
    private final CmGNBCUCPFunctionRepo cmGNBCUCPFunctionRepo;
    private final CmNRCellCURepo cmNRCellCURepo;
    private final CmExternalGNBCUCPFunctionRepo cmExternalGNBCUCPFunctionRepo;
    private final CmExternalNRCellCURepo cmExternalNRCellCURepo;
    private final CellRelationService cellRelationService;
    private final IdentityService identityService;
    private final CmDataLoader cmDataLoader;
    private Map<Integer, Integer> frequencyUsage;
    private Map<NRCellCU, Integer> cellUsage;
    private static final Integer MIN_SUB_CARRIER_SPACING = 30;

    public Map<String, List<SecondaryCellGroup>> fetchSecondaryCellGroups(List<ManagedObjectId> allowEutranCells) {

        Map<EUtranCell, List<GUtranRelationAggregate>> gUtranRelationAggregateMap = getGUtranRelationMapping(allowEutranCells);
        Map<String, List<SecondaryCellGroup>> eutranCellToSCGsMap = new HashMap<>();

        gUtranRelationAggregateMap.forEach((eUtranCell, gUtranRelationAggregates) ->
        {
            List<SecondaryCellGroup> secondaryCellGroups = gUtranRelationAggregates.stream()
                    .filter(gUtranRelationAggre -> identityService.getObjectIdFromCellId(gUtranRelationAggre.getTargetGlobalCellId()).isPresent())
                    .flatMap(gUtranRelationAggre -> findPrimaryCells(gUtranRelationAggre.getTargetGlobalCellId(), gUtranRelationAggre.gUtranFreqRelation().getGUtranSyncSignalFrequencyRef().getArfcn()).stream())
                    .toList();
            log.debug("eutranCell {}, SCGs size {}", eUtranCell.getObjectId().toString(), secondaryCellGroups.size());
            eutranCellToSCGsMap.put(eUtranCell.getFdn(), secondaryCellGroups);
        });
        return eutranCellToSCGsMap;
    }

    public Map<EUtranCell, List<GUtranRelationAggregate>> getGUtranRelationMapping(List<ManagedObjectId> allowEutranCells) {

        Map<EUtranCell, List<GUtranRelationAggregate>> gUtranRelationAggregateMap = cmEUtranCellRepo.findAllById(allowEutranCells).stream()
                // ExtGutranCellRef must be empty, otherwise, cell is non-ENDC anchor or is used as config-based EN-DC which we should exclude from the scope.
                .filter(eUtranCell -> !eUtranCell.getEndcAllowedPlmnList().isEmpty() && eUtranCell.getExtGUtranCellRef().isEmpty())
                .peek(eUtranCell -> log.debug("eUtranCell fdn: {}", eUtranCell.getObjectId().toString()))
                .collect(Collectors.toMap(
                        eUtranCell -> eUtranCell,
                        eUtranCell -> cellRelationService.listGUtranRelationByEUtranCell(eUtranCell).stream()
                                .filter(gUtranRelationAggregate -> gUtranRelationAggregate.gUtranFreqRelation().getEndcB1MeasPriority() != -1 &&
                                        gUtranRelationAggregate.gUtranCellRelation().getIsEndcAllowed())
                                .toList()));

        log.debug("allowEutranCells {}, gUtranRelationAggregateMap size: {}", allowEutranCells.size(), gUtranRelationAggregateMap.size());
        return gUtranRelationAggregateMap;
    }

    private Optional<SecondaryCellGroup> findPrimaryCells(NRCellId nrCellId, Integer arfcn) {

        identityService.getObjectIdFromCellId(nrCellId).ifPresent(cmDataLoader::fetchExternalNRData);
        // This is PsCell
        Optional<NRCellCU> nrCellCU = cmGNBCUCPFunctionRepo.findByGNBId(nrCellId.getNodeId()).stream()
                .flatMap(gnbcucpFunction -> cellRelationService.getNRCellCUByGNBCUCPFunction(gnbcucpFunction).stream())
                .filter(nrCell -> nrCell.getCellLocalId() == nrCellId.getLocalCellId())
                .filter(nrCell -> {
                    if (!nrCell.getPSCellCapable()) {
                        log.warn("NRCellCU is not psCell(Possible wrong configuration), LocalCellId {}", nrCell.getCellLocalId());
                        return false;
                    }
                    return true;
                })
                // when create SCGs, Only check if PsCell's SCS >= 30kHz, sCells does not need this requirement
                .filter(this::isSubCarrierSpacingValid)
                .findFirst();

        if (nrCellCU.isPresent()) {
            // find sCell from nrCellCU
            List<NRCellCU> secondaryNRCells = cellRelationService.listNRCellRelationByNRCellCU(nrCellCU.get()).stream()
                    .filter(nrCellRelation -> nrCellRelation.getCoverageIndicator() != NRCellRelation.SupportedCoverage.NONE && nrCellRelation.getSCellCandidate() != NRCellRelation.SCellCandidate.NOT_ALLOWED)
                    .flatMap(each -> findSecondaryCells(each).stream())
                    .toList();
            return Optional.of(new SecondaryCellGroup(nrCellCU.get(), secondaryNRCells, arfcn));
        }
        return Optional.empty();
    }

    private Optional<NRCellCU> findSecondaryCells(NRCellRelation nrCellRelation) {
        Optional<NRCellCU> secondaryCell = cmNRCellCURepo.findById(nrCellRelation.getNRCellRef());
        Optional<ExternalNRCellCU> externalNRCellCU = cmExternalNRCellCURepo.findById(nrCellRelation.getNRCellRef());

        if (externalNRCellCU.isPresent()) {
            secondaryCell = cmExternalGNBCUCPFunctionRepo.findById(externalNRCellCU.get().getObjectId().fetchParentId()).stream()
                    .flatMap(externalGNBCUCPFunction -> cmGNBCUCPFunctionRepo.findByGNBId(externalGNBCUCPFunction.getGNBId()).stream())
                    .flatMap(gnbcucpFunction -> cellRelationService.getNRCellCUByGNBCUCPFunction(gnbcucpFunction).stream())
                    .filter(nrCell -> Objects.equals(nrCell.getCellLocalId(), externalNRCellCU.get().getCellLocalId()))
                    .findFirst();
        }
        return secondaryCell;
    }

    public void generateSCGsUsageData(List<SecondaryCellGroup> secondaryCellGroups) {

        this.frequencyUsage = new HashMap<>();
        this.cellUsage = new HashMap<>();
        secondaryCellGroups.forEach(scg ->
        {
            this.frequencyUsage.merge(scg.arfcn(), 1, Integer::sum);
            Stream.concat(Stream.of(scg.primaryNRCell()), scg.secondaryCells().stream())
                    .toList()
                    .forEach(cellCU -> this.cellUsage.merge(cellCU, 1, Integer::sum));
        });
    }

    public boolean isSubCarrierSpacingValid(NRCellCU cellCU) {
        return cellRelationService.listNRCellDUByNRCellCU(cellCU).stream().findFirst()
                .filter(cellDU-> cellDU.getSubCarrierSpacing() >= MIN_SUB_CARRIER_SPACING)
                .isPresent();
    }
}