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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomMetricsConfiguration {
    public static final String APP = "app";
    public static final String ERIC_OSS_ENDC_FLM_POC_APP = "eric.oss.endc.flm.poc.app";

    @Autowired
    private MeterRegistry meterRegistry;

    @Bean
    Counter cellSuitabilityChecked() {
        return meterRegistry.counter("cell.suitability.check", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter cellBlocked() {
        return meterRegistry.counter("cell.blocked", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaNrPmCounterBatchesReceived() {
        return meterRegistry.counter("kafka.nr.pmcounter.batches.received", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaNrPmCounterValidHeaderReceived() {
        return meterRegistry.counter("kafka.nr.pmcounter.valid.header.received", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaNrPmCounterValidCounterReceived() {
        return meterRegistry.counter("kafka.nr.pmcounter.valid.counter.received", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaNrPmCounterValidFormatReceived() {
        return meterRegistry.counter("kafka.nr.pmcounter.valid.format.received", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaNrPmCounterRecordsReceived() {
        return meterRegistry.counter("kafka.nr.pmcounter.failed.parsing", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter nrPmCounterInternalStored() {
        return meterRegistry.counter("nr.pmcounter.internal.stored", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaLtePmCounterBatchesReceived() {
        return meterRegistry.counter("kafka.lte.pmcounter.batches.received", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaLtePmCounterValidHeaderReceived() {
        return meterRegistry.counter("kafka.lte.pmcounter.valid.header.received", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaLtePmCounterValidCounterReceived() {
        return meterRegistry.counter("kafka.lte.pmcounter.valid.counter.received", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaLtePmCounterValidFormatReceived() {
        return meterRegistry.counter("kafka.lte.pmcounter.valid.format.received", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter kafkaLtePmCounterRecordsReceived() {
        return meterRegistry.counter("kafka.lte.pmcounter.failed.parsing", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }

    @Bean
    Counter ltePmCounterInternalStored() {
        return meterRegistry.counter("lte.pmcounter.internal.stored", APP, ERIC_OSS_ENDC_FLM_POC_APP);
    }
}
