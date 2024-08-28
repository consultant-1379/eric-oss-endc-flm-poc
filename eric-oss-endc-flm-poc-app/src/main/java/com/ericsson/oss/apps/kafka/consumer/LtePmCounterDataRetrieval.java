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
package com.ericsson.oss.apps.kafka.consumer;

import com.ericsson.oss.apps.model.pmrop.*;
import com.ericsson.oss.apps.streaming.counters.PmCounterDeserializer;
import com.ericsson.oss.apps.streaming.counters.PmCounterProcessor;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class LtePmCounterDataRetrieval extends CustomBatchMessageListener {
    private final Counter kafkaLtePmCounterBatchesReceived;
    private final Counter kafkaLtePmCounterValidHeaderReceived;
    private final Counter kafkaLtePmCounterValidCounterReceived;
    private final Counter kafkaLtePmCounterValidFormatReceived;
    private final Counter kafkaLtePmCounterRecordsReceived;
    private final Counter ltePmCounterInternalStored;

    private final PmCounterDeserializer deserializer;
    private final PmCounterProcessor pmCounterProcessor;

    private static final String MO_TYPE = "moType";
    private static final String NODE_FDN = "nodeFDN";
    private static final String CELL_RELATION_SCHEMA = "GUtranCellRelation";
    private static final String FREQ_RELATION_SCHEMA = "GUtranFreqRelation";
    private static final String EUTRAN_CELL_SCHEMA = "EUtranCell";

    public static final Map<String, Optional<Set<String>>> HEADER_MAP = Map.of(
            MO_TYPE, Optional.of(Set.of(CELL_RELATION_SCHEMA, FREQ_RELATION_SCHEMA, EUTRAN_CELL_SCHEMA)),
            NODE_FDN, Optional.empty());

    @Value("${rapp-sdk.kafka.consumers.ltePmCounters.topics}")
    private String schemaTopic;

    @Override
    public void retrieveBatchData(List<ConsumerRecord<String, byte[]>> records) {
        if (records == null) {
            return;
        }

        kafkaLtePmCounterBatchesReceived.increment();
        List<? extends PmRop> pmRopRecord = records.parallelStream()
                .peek(consumerRecord -> kafkaLtePmCounterRecordsReceived.increment())
                .peek(consumerRecord -> log.trace("LTE PM Counter trace key={}, headers={}", consumerRecord.key(), consumerRecord.headers()))
                .filter(this::isValidHeader)
                .peek(consumerRecord -> kafkaLtePmCounterValidHeaderReceived.increment())
                .filter(consumerRecord -> pmCounterProcessor.isValidPmCounter(new String(this.getHeaderMap(consumerRecord.headers()).get("nodeFDN").value(), StandardCharsets.UTF_8), false))
                .peek(consumerRecord -> kafkaLtePmCounterValidCounterReceived.increment())
                .flatMap(this::processRecord)
                .peek(consumerRecord -> kafkaLtePmCounterValidFormatReceived.increment())
                .toList();

        if (!pmRopRecord.isEmpty()) {
            pmCounterProcessor.processCounters(pmRopRecord);
            ltePmCounterInternalStored.increment();
        }
    }

    @Override
    public Map<String, Optional<Set<String>>> getHeaderValueSetCheckMap() {
        return HEADER_MAP;
    }

    public Stream processRecord(ConsumerRecord<String, byte[]> consumerRecord) {
        Map<String, Header> headersMap = this.getHeaderMap(consumerRecord.headers());
        String header = new String(headersMap.get(MO_TYPE).value(), StandardCharsets.UTF_8);

        return switch (header) {
            case CELL_RELATION_SCHEMA ->
                    deserializer.avroSpecificDeserializer(consumerRecord, schemaTopic, PmRopGUtranCellRelation.class).stream();
            case FREQ_RELATION_SCHEMA ->
                    deserializer.avroSpecificDeserializer(consumerRecord, schemaTopic, PmRopGUtranFreqRelation.class).stream();
            case EUTRAN_CELL_SCHEMA ->
                    deserializer.avroSpecificDeserializer(consumerRecord, schemaTopic, PmRopEUtranCell.class).stream();

            default -> null;
        };
    }
}
