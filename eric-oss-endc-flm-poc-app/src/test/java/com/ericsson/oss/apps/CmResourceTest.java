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
package com.ericsson.oss.apps;

import com.ericsson.oss.apps.model.mom.*;
import com.ericsson.oss.apps.ncmp.NcmpClient;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.ncmp.model.Toggle;
import com.ericsson.oss.apps.repository.*;
import com.ericsson.oss.apps.topology.model.PLMNId;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(properties = {
        "rapp-sdk.ncmp.base-path=http://localhost:${wiremock.server.port}/ncmp",
}, classes = {CoreApplication.class, CoreApplicationTest.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureWireMock(port = 0)
class CmResourceTest {
    //LTE
    private static final String ENODEB_CM_HANDLE = "5FAD235EF83832777ED607AC4F83A152";
    private static final UrlPathPattern eNodeBNcmpUrlPattern = urlPathMatching(String.format("/ncmp/v1/ch/%s/data/ds/ncmp-datastore.*", ENODEB_CM_HANDLE));
    private static final String ENODEB_REFERENCE = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=LTE31dg2ERBS00035,ManagedElement=LTE31dg2ERBS00035";
    private static final ManagedObjectId EUTRAN_CELL_RESOURCE = new ManagedObjectId(ENODEB_REFERENCE, "ENodeBFunction=1,EUtranCellFDD=LTE31dg2ERBS00035-1");
    private static final ManagedObjectId GUTRAN_FREQ_RELATION_RESOURCE = new ManagedObjectId(ENODEB_REFERENCE, "ENodeBFunction=1,EUtranCellFDD=LTE31dg2ERBS00035-1,GUtranFreqRelation=4");
    private static final ManagedObjectId GUTRAN_CELL_RELATION_RESOURCE = new ManagedObjectId(ENODEB_REFERENCE, "ENodeBFunction=1,EUtranCellFDD=LTE31dg2ERBS00035-1,GUtranFreqRelation=4,GUtranCellRelation=8");
    private static final String SECTOR_CARRIER_REF = "ENodeBFunction=1,SectorCarrier=6-1";
    private static final ManagedObjectId SECTOR_CARRIER_RESOURCE = new ManagedObjectId(ENODEB_REFERENCE, SECTOR_CARRIER_REF);
    private static final String RESERVED_BY_REF = "vsDataENodeBFunction=1,vsDataEUtranCellFDD=1-1";
    private static final ManagedObjectId GUTRAN_NEIGHBOR_CELL_REF_RESOURCE = new ManagedObjectId(ENODEB_REFERENCE, "ENodeBFunction=1,ExternalGNodeBFunction=1,ExternalGUtranCell=NR03gNodeBRadio00002-1");
    private final List<PLMNId> testPLMNs = new ArrayList<>(List.of(new PLMNId(353, 56, 2),
            new PLMNId(353, 58, 2)));

    //NR
    private static final String NR_CM_HANDLE = "92F1CB35798FD7D13BCC6FF825D89CD6";
    private static final UrlPathPattern nrNcmpUrlPattern = urlPathMatching(String.format("/ncmp/v1/ch/%s/data/ds/ncmp-datastore.*", NR_CM_HANDLE));
    private static final String NR_REFERENCE = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR03gNodeBRadio00002,ManagedElement=NR03gNodeBRadio00002";
    private static final ManagedObjectId NR_CU_CELL_RESOURCE = new ManagedObjectId(NR_REFERENCE, "GNBCUCPFunction=1,NRCellCU=NR03gNodeBRadio00002-1");
    private static final ManagedObjectId NR_DU_CELL_RESOURCE = new ManagedObjectId(NR_REFERENCE, "GNBDUFunction=1,NRCellDU=NR03gNodeBRadio00002-1");

    @Autowired
    private NcmpClient ncmpClient;
    @Autowired
    private CmNRCellDURepo nrCellDURepo;
    @Autowired
    private CmNRCellCURepo nrCellCURepo;
    @Autowired
    private CmNRFrequencyRepo nrFrequencyRepo;
    @Autowired
    private CmNRSectorCarrierRepo nrSectorCarrierRepo;
    @Autowired
    private CmEUtranCellFDDRepo eUtranCellFddRepo;
    @Autowired
    private CmGUtranCellRelationRepo gUtranCellRelationRepo;

    public void startMockServer(String filename,
                                UrlPathPattern ncmpUrlPattern,
                                String options) {

        Map<String, StringValuePattern> queryParameters = Map.of(
                "resourceIdentifier", equalTo("/"),
                "options", equalTo(options)
        );

        stubFor(WireMock.get(ncmpUrlPattern)
                .withQueryParams(queryParameters)
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile(filename)));
    }

    @Test
    void getCmResourceEUtranFDD() {
        startMockServer("eutrancellfdd.json",
                eNodeBNcmpUrlPattern,
                "fields=EUtranCellFDD/attributes(*)");

        Optional<EUtranCellFDD> optionalEUtranCellFDD = ncmpClient.getCmResource(EUTRAN_CELL_RESOURCE, EUtranCellFDD.class);

        assertThat(optionalEUtranCellFDD.map(ManagedObject::getObjectId)).hasValue(EUTRAN_CELL_RESOURCE);

        Optional<EUtranCellFDD> eUtranCellFDD = eUtranCellFddRepo.findById(EUTRAN_CELL_RESOURCE);

        assertThat(eUtranCellFDD).hasValueSatisfying(cell -> {
            assertEquals(EUTRAN_CELL_RESOURCE, cell.getObjectId());
            assertEquals(1, cell.getCellId());
            assertNull(cell.getAvailabilityStatus());
            assertEquals(Toggle.DISABLED, cell.getOperationalState());
            assertEquals(3, cell.getEarfcndl());
            assertEquals(18003, cell.getEarfcnul());
            assertEquals(3, cell.getFreqBand());

            assertEquals(2, cell.getEndcAllowedPlmnList().size());
            assertEquals(testPLMNs.get(0), cell.getEndcAllowedPlmnList().get(0));
            assertEquals(testPLMNs.get(1), cell.getEndcAllowedPlmnList().get(1));

            assertNull(cell.getEndcSetupDlPktAgeThr());
            assertNull(cell.getEndcSetupDLPktVolThr());
            assertEquals(15000, cell.getDlChannelBandwidth());
            assertEquals(10000, cell.getUlChannelBandwidth());
            assertNotNull(cell.getSectorCarrierRef());

            SectorCarrier sectorCarrierRef = cell.getSectorCarrierRef().get(0);
            assertEquals(SECTOR_CARRIER_REF, sectorCarrierRef.getObjectId().getResRef());
            assertEquals(ENODEB_REFERENCE, sectorCarrierRef.getObjectId().getMeFdn());
        });

        //SectorCarrier
        Optional<SectorCarrier> sectorCarrier = ncmpClient.getCmResource(SECTOR_CARRIER_RESOURCE, SectorCarrier.class);

        assertThat(sectorCarrier).hasValueSatisfying(carrier -> {
            assertEquals(SECTOR_CARRIER_RESOURCE, carrier.getObjectId());

            ManagedObjectId reservedBy = carrier.getReservedBy().get(0);
            assertEquals(RESERVED_BY_REF, reservedBy.getResRef());
            assertEquals(ENODEB_REFERENCE, reservedBy.getMeFdn());
            assertEquals(0, carrier.getEssScLocalId());
            assertEquals(0, carrier.getEssScPairId());
        });

        //GUTRAN FREQ RELATION
        Optional<GUtranFreqRelation> gUtranFreqRelation = ncmpClient.getCmResource(GUTRAN_FREQ_RELATION_RESOURCE, GUtranFreqRelation.class);

        assertThat(gUtranFreqRelation).hasValueSatisfying(relation -> {
            assertEquals(GUTRAN_FREQ_RELATION_RESOURCE, relation.getObjectId());

            assertEquals(5, relation.getEndcB1MeasPriority());
            assertEquals(7, relation.getCellReselectionPriority());
            assertEquals(-1, relation.getConnectedModeMobilityPrio());
        });
    }

    @Test
    void getCmResourceGUtranCellRelation() {
        startMockServer("eutrancellfdd.json",
                eNodeBNcmpUrlPattern,
                "fields=GUtranCellRelation/attributes(*)");

        Optional<GUtranCellRelation> optionalGUtranCellRelation = ncmpClient.getCmResource(GUTRAN_CELL_RELATION_RESOURCE, GUtranCellRelation.class);

        assertThat(optionalGUtranCellRelation.map(ManagedObject::getObjectId)).hasValue(GUTRAN_CELL_RELATION_RESOURCE);

        Optional<GUtranCellRelation> gUtranCellRelation = gUtranCellRelationRepo.findById(GUTRAN_CELL_RELATION_RESOURCE);

        assertThat(gUtranCellRelation).hasValueSatisfying(relation -> {
            assertEquals(GUTRAN_CELL_RELATION_RESOURCE, relation.getObjectId());

            assertTrue(relation.getIsHoAllowed());
            assertTrue(relation.getIsRemovedAllowed());

            ExternalGUtranCell externalGUtranCell = relation.getNeighborCellRef();
            assertEquals(GUTRAN_NEIGHBOR_CELL_REF_RESOURCE, externalGUtranCell.getObjectId());

            assertFalse(relation.getEssEnabled());
            assertTrue(relation.getIsEndcAllowed());
            assertTrue(relation.getEssCellScPairs().isEmpty());
        });
    }

    @Test
    void getNrCellDUCmResource() {
        startMockServer("nrcell.json", nrNcmpUrlPattern,
                "fields=NRCellDU/attributes(*)");

        Optional<NRCellDU> optionalCellDU = ncmpClient.getCmResource(NR_DU_CELL_RESOURCE, NRCellDU.class);

        assertThat(optionalCellDU.map(ManagedObject::getObjectId)).hasValue(NR_DU_CELL_RESOURCE);

        Optional<NRCellDU> cellDU = nrCellDURepo.findById(NR_DU_CELL_RESOURCE);
        assertThat(cellDU).hasValueSatisfying(cell -> {
            assertEquals(NR_REFERENCE, cell.getObjectId().getMeFdn());

            //NRSectorCarrier
            NRSectorCarrier nrSectorCarrier = cell.getNRSectorCarrierRef().get(0);
            assertEquals(NR_REFERENCE, nrSectorCarrier.getObjectId().getMeFdn());
            assertEquals(2, nrSectorCarrierRepo.findAll().size());
        });
    }

    @Test
    void getNrCellCUCmResource() {
        startMockServer("nrcell.json", nrNcmpUrlPattern,
                "fields=NRCellCU/attributes(*)");

        Optional<NRCellCU> optionalCellCU = ncmpClient.getCmResource(NR_CU_CELL_RESOURCE, NRCellCU.class);

        assertThat(optionalCellCU.map(ManagedObject::getObjectId)).hasValue(NR_CU_CELL_RESOURCE);

        Optional<NRCellCU> cellCU = nrCellCURepo.findById(NR_CU_CELL_RESOURCE);
        assertThat(cellCU).hasValueSatisfying(cell -> {
            assertNotNull(cellCU);
            assertEquals(NR_REFERENCE, cell.getObjectId().getMeFdn());
            assertEquals(216, cell.getPrimaryPLMNId().getMcc());
            assertEquals(30, cell.getPrimaryPLMNId().getMnc());
            assertEquals(111, cell.getCellLocalId());

            //NRFrequency
            NRFrequency nrFrequency = cell.getNRFrequencyRef();
            assertEquals(NR_REFERENCE, nrFrequency.getObjectId().getMeFdn());
            assertEquals(2, nrFrequencyRepo.findAll().size());
        });
    }
}
