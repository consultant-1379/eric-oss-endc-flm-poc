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
public class NrPmCounterDataRetrieval extends CustomBatchMessageListener {
    private final Counter kafkaNrPmCounterBatchesReceived;
    private final Counter kafkaNrPmCounterValidHeaderReceived;
    private final Counter kafkaNrPmCounterValidCounterReceived;
    private final Counter kafkaNrPmCounterValidFormatReceived;
    private final Counter kafkaNrPmCounterRecordsReceived;
    private final Counter nrPmCounterInternalStored;

    private final PmCounterDeserializer deserializer;
    private final PmCounterProcessor pmCounterProcessor;

    private static final String MO_TYPE = "moType";
    private static final String NODE_FDN = "nodeFDN";
    private static final String NRCELLCU_SCHEMA = "NRCellCU_GNBCUCP";
    private static final String NRCELLDU_SCHEMA = "NRCellDU_GNBDU";
    private static final String GNBDU_FUNCTION_SCHEMA = "GNBDUFunction_GNBDU";

    public static final Map<String, Optional<Set<String>>> HEADER_SET = Map.of(
            MO_TYPE, Optional.of(Set.of(NRCELLCU_SCHEMA, NRCELLDU_SCHEMA, GNBDU_FUNCTION_SCHEMA)),
            NODE_FDN, Optional.empty());

    @Value("${rapp-sdk.kafka.consumers.nrPmCounters.topics}")
    private String schemaTopic;

    @Override
    public void retrieveBatchData(List<ConsumerRecord<String, byte[]>> records) {
        if (records == null) {
            return;
        }

        kafkaNrPmCounterBatchesReceived.increment();
        List<? extends PmRop> pmRopRecordList = records.parallelStream()
                .peek(consumerRecord -> kafkaNrPmCounterRecordsReceived.increment())
                .peek(consumerRecord -> log.trace("NR PM Counter trace key={}, headers={}", consumerRecord.key(), consumerRecord.headers()))
                .filter(this::isValidHeader)
                .peek(consumerRecord -> kafkaNrPmCounterValidHeaderReceived.increment())
                .filter(consumerRecord -> pmCounterProcessor.isValidPmCounter(new String(this.getHeaderMap(consumerRecord.headers()).get("nodeFDN").value(), StandardCharsets.UTF_8), true))
                .peek(consumerRecod -> kafkaNrPmCounterValidCounterReceived.increment())
                .flatMap(this::processRecord)
                .peek(consumerRecord -> kafkaNrPmCounterValidFormatReceived.increment())
                .toList();

        if (!pmRopRecordList.isEmpty()) {
            pmCounterProcessor.processCounters(pmRopRecordList);
            nrPmCounterInternalStored.increment();
        }
    }

    @Override
    public Map<String, Optional<Set<String>>> getHeaderValueSetCheckMap() {
        return HEADER_SET;
    }

    public Stream processRecord(ConsumerRecord<String, byte[]> consumerRecord) {
        Map<String, Header> headersMap = this.getHeaderMap(consumerRecord.headers());
        String header = new String(headersMap.get(MO_TYPE).value(), StandardCharsets.UTF_8);

        return switch (header) {
            case NRCELLCU_SCHEMA ->
                    deserializer.avroSpecificDeserializer(consumerRecord, schemaTopic, PmRopNRCellCU.class).stream();
            case NRCELLDU_SCHEMA ->
                    deserializer.avroSpecificDeserializer(consumerRecord, schemaTopic, PmRopNRCellDU.class).stream();
            case GNBDU_FUNCTION_SCHEMA ->
                    deserializer.avroSpecificDeserializer(consumerRecord, schemaTopic, PmRopGNBDUFunction.class).stream();

            default -> null;
        };
    }
}