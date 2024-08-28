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

import com.ericsson.oss.apps.model.SecondaryCellGroup;
import com.ericsson.oss.apps.model.mom.NRCellCU;
import com.ericsson.oss.apps.model.mom.NRCellDU;
import com.ericsson.oss.apps.model.mom.NRSectorCarrier;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MoreCollectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ericsson.oss.apps.model.mom.NRCellDU.ModulationOrder.QAM_256;

@Service
@Slf4j
@RequiredArgsConstructor
public class CellCapacityService {

    private static final Integer NR_OF_MIMO_LAYERS = 1;
    private static final Integer WEIGHT_FACTOR = 1;
    private static final Float R_MAX = 0.92578125F;
    private static final List<Float> DownlinkFrWeightFactors = List.of(0.86F, 0.82F); //FR1, FR2
    private static final List<Float> DlLteSSRatios = List.of(1F, 0.7F); // 0.7 for ESS for calculating downlink cell weight, otherwise 1
    private static final List<Integer> MimoLayerCompensations = List.of(1, 2); //FR1, FR2
    private static final List<Float> TDDValues = List.of(
            0.667F, //DDSU
            0.778F, //DDDSUUDDDD
            0.750F, //DDDSU
            0.625F, //DDDSUDDSUU
            0.778F, //DDDDDDDSUU
            0.5F,   //DDSUU
            0.25F,  //DSUUU
            0.5F,   //DDDSUUDSUU
            0.5556F //DDDSUUUUDD
    );
    private final JsonNode mapper = availableRbsTable();
    private final CellRelationService cellRelationService;
    private final CellFilterService cellFilterService;

    @Getter
    @Setter
    private Map<String, Map<Integer, Float>> freqCapacityMap = new HashMap<>();
    @Getter
    @Setter
    private Map<String, Map<String, Float>> nrCellCapacityMap = new HashMap<>();

    public Map<Integer, Float> processCapacityPerFrequency(String eUtranCellFdn, List<SecondaryCellGroup> secondaryCellGroups, Boolean usingCellWeight) {
        Map<Integer, Float> capacityPerFrequency = new HashMap<>();
        cellFilterService.generateSCGsUsageData(secondaryCellGroups);

        if (cellFilterService.getCellUsage().isEmpty() || cellFilterService.getFrequencyUsage().isEmpty()) {
            log.error("frequencyUsage and cellUsage are not generated");
            return capacityPerFrequency;
        }

        secondaryCellGroups.forEach(scg ->
        {
            final Integer freqUsage = cellFilterService.getFrequencyUsage().get(scg.arfcn());

            // Calculate capacity for each NR Cell within a SCG
            Map<String, Float> cellCapacityMap = Stream.concat(Stream.of(scg.primaryNRCell()), scg.secondaryCells().stream())
                    .collect(Collectors.toMap(ManagedObject::getFdn, cellCU -> calculateCellCapacityPerUsage(cellCU, usingCellWeight).orElse(0F) / freqUsage));

            cellCapacityMap.entrySet().stream()
                    .forEach(entry -> {
                        // Assuming one NR Cell would not work with multiple ARFCN.
                        nrCellCapacityMap.computeIfAbsent(eUtranCellFdn, k -> new HashMap<>());
                        nrCellCapacityMap.get(eUtranCellFdn).merge(entry.getKey(), entry.getValue(), Float::sum);
                    });

            float totalCapacity = cellCapacityMap.values().stream().reduce(0F, Float::sum);

            // For the same frequency, add its capacity all together
            capacityPerFrequency.merge(scg.arfcn(), totalCapacity, Float::sum);
            log.debug("arfcn: {}, current totalCapacity: {}, FrequencyUsage {}, usingCellWeight {}",
                    scg.arfcn(), capacityPerFrequency.get(scg.arfcn()), freqUsage, usingCellWeight);
        });

        freqCapacityMap.put(eUtranCellFdn, capacityPerFrequency);

        return capacityPerFrequency;
    }

    public Optional<Float> calculateCellCapacityPerUsage(NRCellCU nrCellCU, Boolean usingCellWeight) {

        Optional<NRCellDU> nrCellDU = cellRelationService.listNRCellDUByNRCellCU(nrCellCU).stream()
                .collect(MoreCollectors.toOptional());
        Optional<Float> cellCapacity;

        // Option of using cell weight
        if (usingCellWeight) {
            cellCapacity = nrCellDU.flatMap(cellDU ->
                    cellDU.getNRSectorCarrierRef().stream()
                            .peek(nrSectorCarrier -> log.debug("cellDU FDN {}, nrSectorCarrier FDN {}", cellDU.getObjectId().toString(), nrSectorCarrier.getObjectId().toString()))
                            // arfcnDL and bSChannelBwDL must have same value amount for all NRSectorCarriers
                            .findFirst()
                            .map(nrSectorCarrier -> calculateCellWeight(cellDU, nrSectorCarrier) / cellFilterService.getCellUsage().get(nrCellCU)));
        }
        // Option of using bandwidth
        else {
            cellCapacity = nrCellDU.flatMap(cellDU -> cellDU.getNRSectorCarrierRef().stream()
                    .peek(nrSectorCarrier -> log.debug("cellDU FDN {}, nrSectorCarrier FDN {}, BSChannelBwDL {}", cellDU.getObjectId().toString(), nrSectorCarrier.getObjectId().toString(), nrSectorCarrier.getBSChannelBwDL()))
                    // arfcnDL and bSChannelBwDL must have same value amount for all NRSectorCarriers
                    .findFirst()
                    .map(nrSectorCarrier -> ((float) nrSectorCarrier.getBSChannelBwDL() / cellFilterService.getCellUsage().get(nrCellCU))));
        }

        log.debug("nrCellCU FDN: {}, cellCapacityPerUsage: {}, Cell Usage: {}, usingCellWeight {}", nrCellCU.getObjectId().toString(), cellCapacity, cellFilterService.getCellUsage().get(nrCellCU), usingCellWeight);
        return cellCapacity;
    }

    public float calculateCellWeight(NRCellDU nrCellDU, NRSectorCarrier nrSectorCarrier) {

        String frequencyType = (nrCellDU.getBandList().get(0) <= 256) ? "FR1" : "FR2";
        float peakDataRate = getPeakDataRate(nrCellDU, nrSectorCarrier, frequencyType);
        float dlLteSSRatio = (nrSectorCarrier.getEssScLocalId() != null && nrSectorCarrier.getEssScLocalId() != 0) ? DlLteSSRatios.get(1) : DlLteSSRatios.get(0);
        NRCellDU.TddUlDlPatternType patternType = nrCellDU.getTddUlDlPattern();
        float dlTddRatio = Objects.equals(nrSectorCarrier.getArfcnDL(), nrSectorCarrier.getArfcnUL()) ? TDDValues.get(patternType.ordinal()) : 1;
        Integer mimoLayerCompensation = frequencyType.equals("FR1") ? MimoLayerCompensations.get(0) : MimoLayerCompensations.get(1);

        log.debug("frequencyType {} peakDataRate {} dlLteSSRatio {} patternType {} dlTddRatio {} mimoLayerCompensation {}", frequencyType, peakDataRate, dlLteSSRatio, patternType, dlTddRatio, mimoLayerCompensation);
        return peakDataRate * dlLteSSRatio * dlTddRatio * WEIGHT_FACTOR * mimoLayerCompensation;
    }

    private JsonNode availableRbsTable() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // FrequencyType:
            //      bandWidth:
            //          SCS: RBs
            // FR1 (38.101-1 Table 5.3.2-1) FR2 (38.101-2 Table 5.3.2-1)
            InputStream inputStream = new ClassPathResource("availableRbsTable.json").getInputStream();
            return objectMapper.readTree(inputStream);
        } catch (IOException e) {
            log.error("Couldn't find availableRbsTable.json", e);
        }
        return null;
    }

    private float getPeakDataRate(NRCellDU nrCellDU, NRSectorCarrier nrSectorCarrier, String frequencyType) {
        Integer availableRbs = nrCellDU.getDlAvailableCrbs();
        Integer bandWidth = nrSectorCarrier.getBSChannelBwDL();
        Integer subCarrierSpacing = nrCellDU.getSubCarrierSpacing();
        int modulationOrder = 6;

        if (availableRbs == null || availableRbs == 0) {
            assert mapper != null;
            availableRbs = Integer.parseInt(mapper.get(frequencyType).get(bandWidth.toString()).get(subCarrierSpacing.toString()).toString());
        }

        if ((frequencyType.equals("FR2") && Objects.equals(nrCellDU.getDlMaxSupportedModOrder(), QAM_256)) || (frequencyType.equals("FR1") && Objects.equals(nrCellDU.getDl256QamEnabled(), true))) {
            modulationOrder = 8;
        }
        float nrOfSymbolsPerSecond = (float) (availableRbs * 12 * 14 * 1000 * subCarrierSpacing) / 15;
        float downlinkFrWeightFactor = frequencyType.equals("FR1") ? DownlinkFrWeightFactors.get(0) : DownlinkFrWeightFactors.get(1);
        return (nrOfSymbolsPerSecond * modulationOrder * NR_OF_MIMO_LAYERS * downlinkFrWeightFactor * R_MAX);
    }
}
