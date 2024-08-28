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
package com.ericsson.oss.apps.streaming.counters.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "rapp-sdk.kafka", name = "enabled", havingValue = "true", matchIfMissing = false)
public class KafkaProducerConfigPmCounter {
    @Bean
    KafkaTemplate<String, GenericRecord> getDefault_Template(@Value("${spring.kafka.schema-registry.url}") String schemaRegistryUrl,
                                                              @Value("${rapp-sdk.kafka.bootstrap-servers}") String bootStrapServers,
                                                              ProducerFactory<String, GenericRecord> pf) {
        return new KafkaTemplate<>(pf, Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class,
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl,
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers));
    }
}
