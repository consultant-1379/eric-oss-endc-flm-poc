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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class LtePmCounterDataRetrievalTest extends PmCounterDataRetrievalTestBase {
    @InjectMocks
    LtePmCounterDataRetrieval ltePmCounterDataRetrieval;

    private static final String LTE_SCHEMA_TOPIC = "eric-oss-3gpp-pm-xml-ran-parser-lte";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ltePmCounterDataRetrieval, "schemaTopic", LTE_SCHEMA_TOPIC);
    }

    @Test
    void retrieveBatchDataNull() {
        ltePmCounterDataRetrieval.retrieveBatchData(null);
        Mockito.verifyNoInteractions(pmCounterProcessor);
    }

    @Test
    void retrieveBatchDataBadHeader() {
        ConsumerRecord<String, byte[]> record = getStringConsumerRecord(LTE_SCHEMA_TOPIC);
        record.headers().add((new RecordHeader("badHeader", "dummyValue".getBytes())));
        ltePmCounterDataRetrieval.retrieveBatchData(List.of(record));
        Mockito.verifyNoInteractions(pmCounterProcessor);
    }

    @Test
    void retrieveBatchDataNoHeader() {
        ConsumerRecord<String, byte[]> record = getStringConsumerRecord(LTE_SCHEMA_TOPIC);
        ltePmCounterDataRetrieval.retrieveBatchData(List.of(record));
        Mockito.verifyNoInteractions(pmCounterProcessor);
    }

    @Test
    void testRetrieveBatchDataWithNoAvroSchemaLoading() {
        List<ConsumerRecord<String, byte[]>> records = new ArrayList<>();
        ltePmCounterDataRetrieval.retrieveBatchData(records);
        Mockito.verifyNoInteractions(pmCounterProcessor);
    }

    @Test
    void retrieveBatchDataValidHeader() {
        ConsumerRecord<String, byte[]> record = getStringConsumerRecord(LTE_SCHEMA_TOPIC);
        record.headers().add((new RecordHeader("nodeFDN", "dummyValue".getBytes())));
        record.headers().add((new RecordHeader("moType", "EUtranCell".getBytes())));

        ltePmCounterDataRetrieval.retrieveBatchData(List.of(record));
        Mockito.verify(pmCounterProcessor, Mockito.times(1)).isValidPmCounter("dummyValue", false);
    }
}
