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

import com.ericsson.oss.apps.kafka.KafkaContainersTestUtils;
import com.ericsson.oss.apps.kafka.consumer.LtePmCounterDataRetrieval;
import com.ericsson.oss.apps.model.pmrop.PmRopGUtranCellRelation;
import com.ericsson.oss.apps.repository.PmGUtranCellRelationRepo;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@Testcontainers
@SpringBootTest(properties = {"rapp-sdk.kafka.enabled=true",
        "spring.security.oauth2.client.registration.eic.client-id=kafka"})
public class CellRelationPmCounterDeserializerIntegrationTest extends PmCounterDeserializerIntegrationTestBase<LtePmCounterDataRetrieval> {

    @Autowired
    private LtePmCounterDataRetrieval ltePmCounterDataRetrieval;
    @Autowired
    private PmGUtranCellRelationRepo pmGUtranCellRelationRepo;
    @Captor
    private ArgumentCaptor<List<PmRopGUtranCellRelation>> listArgumentCaptor;
    @Autowired
    private KafkaTemplate<String, GenericRecord> getDefault_Template;

    public static final List<String> PM_COUNTER_LIST = List.of(
            "pmEndcSetupScgUeAtt",
            "pmEndcSetupScgUeSucc",
            "pmEndcSetupUeAtt",
            "pmEndcSetupUeSucc"
            );

    private final Double COUNTER_VALUE = 60.0;
    private static final String MO_TYPE = "moType";
    private static final String NODE_FDN = "nodeFDN";
    private static final String MO_NAME = "GUtranCellRelation";
    public static final Map<String, Optional<String>> HEADER_MAP = Map.of(MO_TYPE, Optional.of(MO_NAME), NODE_FDN, Optional.of(FDN));

    @Container
    static final KafkaContainer kafka = KafkaContainersTestUtils.getKafkaContainer();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        KafkaContainersTestUtils.overrideProperties(registry, kafka.getBootstrapServers());
        registry.add("spring.kafka.schema-registry.url", () -> "mock://testurl");
    }

    GenericRecord buildRecordData() {

        Schema schemaCellRelation;
        try {
            schemaCellRelation = new Schema.Parser().parse(new File("./src/test/resources/__files/schema/GUtranCellRelation.avsc"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Schema schemaPmCounters = schemaCellRelation.getField("pmCounters").schema();
        GenericRecord avroPmCounters = new GenericData.Record(schemaPmCounters);
        GenericRecord record = new GenericData.Record(schemaCellRelation);

        PM_COUNTER_LIST.forEach(value -> setPMData(value, schemaPmCounters, avroPmCounters, COUNTER_VALUE));

        GenericRecord updateCounter = (GenericRecord) avroPmCounters.get("pmEndcSetupUeSucc");
        updateCounter.put("isValuePresent", false);

        record.put("nodeFDN", FDN);
        record.put("moFdn", FDN);
        record.put("elementType", "whatever");
        record.put("ropBeginTime", ROP_BEGIN_TIME);
        record.put("ropEndTime", ROP_END_TIME);
        record.put("ropBeginTimeInEpoch", ROP_BEGIN_TIME_IN_EPOC);
        record.put("ropEndTimeInEpoch", ROP_END_TIME_IN_EPOC);
        record.put("dnPrefix", "");
        record.put("suspect", false);
        record.put("pmCounters", avroPmCounters);

        return record;
    }

    @Override
    void sendKafkaRecord() {
        when(pmCounterProcessor.isValidPmCounter(FDN, false)).thenReturn(true);
        sendRecord(HEADER_MAP, "eric-oss-3gpp-pm-xml-ran-parser-lte", getDefault_Template);
    }

    @Override
    void validateResult() {

        verify(pmCounterProcessor, times(1)).processCounters(listArgumentCaptor.capture());
        var captorValue = listArgumentCaptor.getValue();
        PmRopGUtranCellRelation gUtranCellRelation = (PmRopGUtranCellRelation) captorValue.get(0);
        assertEquals(FDN, gUtranCellRelation.getMoRopId().getObjectId().toString());
        assertEquals(ROP_END_TIME_IN_EPOC, gUtranCellRelation.getMoRopId().getRopTime());
        assertEquals(COUNTER_VALUE, gUtranCellRelation.getPmEndcSetupScgUeAtt());
        assertEquals(Double.NaN, gUtranCellRelation.getPmEndcSetupUeSucc());
        assertEquals(1, pmGUtranCellRelationRepo.findAll().size());
    }
}
