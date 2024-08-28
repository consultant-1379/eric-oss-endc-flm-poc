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

import com.ericsson.oss.apps.model.GUtranRelationAggregate;
import com.ericsson.oss.apps.model.mom.*;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmExternalGNodeBFunctionRepo;
import com.ericsson.oss.apps.repository.CmGNBCUCPFunctionRepo;
import com.ericsson.oss.apps.topology.model.NRCellId;
import com.ericsson.oss.apps.topology.model.PLMNId;
import com.google.common.collect.MoreCollectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * This class contains utility functions which are useful in resolving different type of cell-relations/links.
 * This class isn't covering the synchronization of any missing data, so make this work properly
 * all the targeted instances has to be synced up/preloaded into the database ahead.
 */
@Service
@RequiredArgsConstructor
public class CellRelationService {

    private static final String OBJECT_ID_PARAMETER = "objectIds";
    private final CmExternalGNodeBFunctionRepo cmExternalGNodeBFunctionRepo;
    private final CmGNBCUCPFunctionRepo cmGNBCUCPFunctionRepo;
    private final EntityManager entityManager;

    public Optional<NRCellCU> getNRCellCUByNRCellDU(NRCellDU nrCellDU) {
        TypedQuery<NRCellCU> query = entityManager.createQuery("""
                SELECT cellCU FROM NRCellCU cellCU JOIN NRCellDU cellDU ON
                 cellCU.nCI = cellDU.nCI AND cellCU.objectId.meFdn = cellDU.objectId.meFdn
                  WHERE cellDU.objectId = :objectIds""", NRCellCU.class);
        query.setParameter(OBJECT_ID_PARAMETER, nrCellDU.getObjectId());
        return query.getResultStream().findFirst();
    }

    public List<NRCellDU> listNRCellDUByNRCellCU(NRCellCU nrCellCU) {
        TypedQuery<NRCellDU> query = entityManager.createQuery("""
                SELECT cellDU FROM NRCellDU cellDU JOIN NRCellCU cellCU ON
                 cellCU.nCI = cellDU.nCI AND cellCU.objectId.meFdn = cellDU.objectId.meFdn
                  WHERE cellCU.objectId = :objectIds""", NRCellDU.class);
        query.setParameter(OBJECT_ID_PARAMETER, nrCellCU.getObjectId());
        return query.getResultList();
    }

    public List<GUtranRelationAggregate> listGUtranRelationByEUtranCell(EUtranCell eUtranCell) {
        TypedQuery<GUtranRelationAggregate> query = entityManager.createQuery("""
                        SELECT new GUtranRelationAggregate(gUtranFreqRelation, gUtranCellRelation, externalGUtranCell, externalGNodeBFunction) FROM EUtranCell eUtranCell
                         JOIN GUtranFreqRelation gUtranFreqRelation ON eUtranCell.objectId.meFdn = gUtranFreqRelation.objectId.meFdn AND eUtranCell.objectId.resRef = gUtranFreqRelation.parentResRef
                          JOIN GUtranCellRelation gUtranCellRelation ON gUtranFreqRelation.objectId.meFdn = gUtranCellRelation.objectId.meFdn AND gUtranFreqRelation.objectId.resRef = gUtranCellRelation.parentResRef
                           JOIN ExternalGUtranCell externalGUtranCell ON gUtranCellRelation.neighborCellRef.objectId = externalGUtranCell.objectId
                            JOIN ExternalGNodeBFunction externalGNodeBFunction ON externalGUtranCell.objectId.meFdn = externalGNodeBFunction.objectId.meFdn AND externalGUtranCell.parentResRef = externalGNodeBFunction.objectId.resRef
                             WHERE eUtranCell.objectId = :objectIds""",
                GUtranRelationAggregate.class);

        query.setParameter(OBJECT_ID_PARAMETER, eUtranCell.getObjectId());
        return query.getResultList();
    }

    public List<GUtranSyncSignalFrequency> listGUtranSyncSignalFreqByEUtranCell(String eUtranCellFdn) {
        TypedQuery<GUtranFreqRelation> query = entityManager.createQuery("""
                        SELECT gUtranFreqRelation FROM GUtranFreqRelation gUtranFreqRelation
                            JOIN EUtranCell eUtranCell ON
                            eUtranCell.objectId.meFdn = gUtranFreqRelation.objectId.meFdn AND eUtranCell.objectId.resRef = gUtranFreqRelation.parentResRef
                            WHERE eUtranCell.objectId = :objectIds""",
                GUtranFreqRelation.class);

        query.setParameter(OBJECT_ID_PARAMETER, ManagedObjectId.of(eUtranCellFdn));
        return query.getResultList().stream().map(GUtranFreqRelation::getGUtranSyncSignalFrequencyRef).toList();
    }

    public Optional<NRCellCU> getNRCellCUByNrGlobalCellId(NRCellId cellId) {
        String queryExpression = """
                SELECT nRCellCU FROM NRCellCU nRCellCU JOIN GNBCUCPFunction gNBCUCPFunction ON
                 nRCellCU.objectId.meFdn = gNBCUCPFunction.objectId.meFdn AND
                  nRCellCU.parentResRef = gNBCUCPFunction.objectId.resRef WHERE
                   nRCellCU.cellLocalId = :cellId AND gNBCUCPFunction.gNBId = :nodeId AND
                    gNBCUCPFunction.gNBIdLength = :nodeIdLength""";
        Map<String, Object> parameters = new HashMap<>(Map.of(
                "cellId", cellId.getLocalCellId(),
                "nodeId", cellId.getNodeId(),
                "nodeIdLength", cellId.getNodeIdLength()
        ));
        // NRCellCU has the capability to override the used PLMN id, the COALESCE function is taking care of this when it happens.
        if (cellId.isGlobal()) {
            queryExpression += " AND COALESCE(nRCellCU.primaryPLMNId.mcc, gNBCUCPFunction.pLMNId.mcc) = :mcc " +
                    "AND COALESCE(nRCellCU.primaryPLMNId.mnc, gNBCUCPFunction.pLMNId.mnc) = :mnc " +
                    "AND COALESCE(nRCellCU.primaryPLMNId.mncLength, gNBCUCPFunction.pLMNId.mncLength) = :mncLength";
            PLMNId plmnId = cellId.getPlmnId();

            parameters.put("mcc", plmnId.getMcc());
            parameters.put("mnc", plmnId.getMnc());
            parameters.put("mncLength", plmnId.getMncLength());
        }

        TypedQuery<NRCellCU> query = entityManager.createQuery(queryExpression, NRCellCU.class);
        parameters.forEach(query::setParameter);

        return query.getResultStream()
                .collect(MoreCollectors.toOptional());
    }

    public List<NRCellCU> getNRCellCUByGNBCUCPFunction(GNBCUCPFunction gnbcucpFunction) {
        TypedQuery<NRCellCU> query = entityManager.createQuery(
                "SELECT nrCellCU FROM NRCellCU nrCellCU " +
                        "JOIN GNBCUCPFunction gnbcucpFunction " +
                        "ON nrCellCU.objectId.meFdn = gnbcucpFunction.objectId.meFdn AND nrCellCU.parentResRef = gnbcucpFunction.objectId.resRef " +
                        "WHERE gnbcucpFunction.objectId = :objectIds",
                NRCellCU.class);

        query.setParameter(OBJECT_ID_PARAMETER, gnbcucpFunction.getObjectId());
        return query.getResultList();
    }

    public Optional<NRCellCU> getNRCellCUByGUtranCellRelation(GUtranCellRelation gUtranCellRelation) {

        return cmExternalGNodeBFunctionRepo.findById(gUtranCellRelation.getNeighborCellRef().getObjectId().fetchParentId()).stream()
                .map(ExternalGNodeBFunction::getGNodeBId)
                .flatMap(gNodeBId -> cmGNBCUCPFunctionRepo.findByGNBId(gNodeBId).stream())
                .flatMap(gnbcucpFunction -> getNRCellCUByGNBCUCPFunction(gnbcucpFunction).stream())
                .filter(nrCell -> Objects.equals(nrCell.getCellLocalId(), gUtranCellRelation.getNeighborCellRef().getLocalCellId()))
                .findFirst();
    }

    public List<NRCellRelation> listNRCellRelationByNRCellCU(NRCellCU nrCellCU) {
        TypedQuery<NRCellRelation> query = entityManager.createQuery(
                "SELECT nrCellRelation FROM NRCellRelation nrCellRelation " +
                        "JOIN NRCellCU nrCellCU " +
                        "ON nrCellRelation.objectId.meFdn = nrCellCU.objectId.meFdn AND nrCellRelation.parentResRef = nrCellCU.objectId.resRef " +
                        "WHERE nrCellCU.objectId = :objectIds",
                NRCellRelation.class);

        query.setParameter(OBJECT_ID_PARAMETER, nrCellCU.getObjectId());
        return query.getResultList();
    }

}
