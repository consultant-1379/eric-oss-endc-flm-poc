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

import com.ericsson.oss.apps.streaming.counters.PmCounterProcessor;
import io.micrometer.core.instrument.Counter;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mock;

abstract class PmCounterDataRetrievalTestBase {
    @Mock
    PmCounterProcessor pmCounterProcessor;
    @Mock
    Counter counter;

    @NotNull
    static ConsumerRecord<String, byte[]> getStringConsumerRecord(String topic) {
        return getStringConsumerRecord(topic, new byte[]{1, 2});
    }

    @NotNull
    static ConsumerRecord<String, byte[]> getStringConsumerRecord(String topic, byte[] messageValue) {
        return new ConsumerRecord<>(topic, 1, 0, "k1", messageValue);
    }
}
