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

package com.ericsson.oss.apps.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    KafkaAvroDeserializer getKafkaAvroDeserializer(@Value("${spring.kafka.schema-registry.url}") String schemaRegistryUrl) {
        Map<String, Object> config = new HashMap<>();
        try (KafkaAvroDeserializer kafkaAvroDeserializer = new KafkaAvroDeserializer()) {
            config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);
            config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
            config.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
            kafkaAvroDeserializer.configure(config, false);
            return kafkaAvroDeserializer;
        }
    }
}
