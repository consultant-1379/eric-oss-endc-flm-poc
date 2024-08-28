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

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import com.ericsson.oss.apps.model.pmrop.PmRop;

@Slf4j
@Component
@RequiredArgsConstructor
public class PmCounterDeserializer {
    private final KafkaAvroDeserializer avroDeserializer;

    public GenericRecord avroDeserializer(ConsumerRecord<String, byte[]> consumerRecord, String topic) {
        return (GenericRecord) avroDeserializer.deserialize(topic, consumerRecord.value());
    }

    public <T extends PmRop> Optional<T> avroSpecificDeserializer(
            ConsumerRecord<String, byte[]> consumerRecord, String topic, Class<T> targetType) {
        // Deserialize the ConsumedRecord into PmRop via AVRO.
        GenericRecord record = (GenericRecord) avroDeserializer.deserialize(topic, consumerRecord.value());

        try {
            T target = targetType.getDeclaredConstructor().newInstance();
            if (target.fromAvroObject(record)) {
                return Optional.of(target);
            }
        }
        catch (Exception e) {
            log.error("Error during AVRO to Entity mapping, avroObject={}, targetType={}", record.toString(), targetType, e);
        }

        return Optional.empty();
    }
}