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
package com.ericsson.oss.apps.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.ericsson.oss.apps.catalog.CatalogClient;
import com.ericsson.oss.apps.catalog.model.DataTypeDtoRes;
import com.ericsson.oss.apps.catalog.model.MessageBusDto;
import com.ericsson.oss.apps.catalog.model.MessageDataTopicDtoResV2;
import com.ericsson.oss.apps.catalog.model.MessageSchemaDtoV2;
import com.ericsson.oss.apps.catalog.model.V1DataTypeFiltersBuilder;

import java.util.Collections;
import java.util.List;

public class KafkaContainersTestUtils {
    public static KafkaContainer getKafkaContainer() {
        return new KafkaContainer(
                DockerImageName.parse("confluentinc/cp-kafka:7.5.1")
        );
    }

    public static void overrideProperties(DynamicPropertyRegistry registry, String bootstrapServers) {
        TestConfiguration.messageBus.setAccessEndpoints(List.of(bootstrapServers));
        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServers);
        registry.add("rapp-sdk.kafka.bootstrap-servers", () -> bootstrapServers);
        registry.add("rapp-sdk.kafka.health-check-timeout-ms", () -> 500);
    }

    static class TestConfiguration {
        static MessageBusDto messageBus = new MessageBusDto();
        static MessageDataTopicDtoResV2 dataTopic = new MessageDataTopicDtoResV2();
        static MessageSchemaDtoV2 messageSchema = new MessageSchemaDtoV2();
        static DataTypeDtoRes dataType = new DataTypeDtoRes();

        static {
            dataTopic.setName("topic1");
            dataTopic.setMessageBus(messageBus);
            messageSchema.setMessageDataTopic(dataTopic);
            dataType.setMessageSchema(messageSchema);
        }

        @Bean
        @Primary
        CatalogClient catalogClient() {
            return new CatalogClient() {
                @Override
                public List<DataTypeDtoRes> queryAllDataTypeByParams(V1DataTypeFiltersBuilder filters) {
                    return Collections.singletonList(dataType);
                }
            };
        }
    }
}
