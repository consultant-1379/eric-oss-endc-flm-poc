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
package com.ericsson.oss.apps.streaming.counters;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;

import com.ericsson.oss.apps.kafka.consumer.CustomBatchMessageListener;
import org.springframework.kafka.core.KafkaTemplate;

public abstract class PmCounterDeserializerIntegrationTestBase<L extends CustomBatchMessageListener> extends PmCounterDeserializerTestBase {

    @SpyBean
    PmCounterProcessor pmCounterProcessor;

    /**
     * This looks overkill however since the data is sent through
     * a kafka avro serializer the magic byte(s) is set and unless we want to
     * set that manually the easiest option is to and send the message over the wire.
     */
    @Test
    void decodePmCounterOkPayload() {
        sendKafkaRecord();
        await()
                .pollInterval(Duration.ofMillis(300))
                .atMost(60, TimeUnit.SECONDS)
                .untilAsserted(this::validateResult);
    }

    void sendRecord(Map<String, Optional<String>> headerMap, String topic, KafkaTemplate<String, GenericRecord> template) {

        List<Header> headers = new ArrayList<>();
        headerMap.forEach((key, value) -> {
            headers.add(new RecordHeader(key, value.map(String::getBytes).orElse("".getBytes())));
        });

        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topic, null, "k1",
                buildRecordData(), headers);
        template.send(record);
    }
    void setPMData(String pmCounterName, Schema schema, GenericRecord counterRecord, Double value)
    {
        Schema schemaPmCounters = schema.getField(pmCounterName).schema();

        GenericRecord pmCounterRecord = new GenericData.Record(schemaPmCounters);

        pmCounterRecord.put("counterType", "single");
        pmCounterRecord.put("counterValue", value);
        pmCounterRecord.put("isValuePresent", true);

        counterRecord.put(pmCounterName, pmCounterRecord);
    }


    abstract GenericRecord buildRecordData();
    abstract void sendKafkaRecord();
    abstract void validateResult();
}
