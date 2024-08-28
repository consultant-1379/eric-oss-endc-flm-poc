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
package com.ericsson.oss.apps.model.pmrop;

import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

@Slf4j
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PmRopNRCellDU implements PmRop, Serializable {
    @Serial
    private static final long serialVersionUID = 2701081017609114686L;

    @EmbeddedId
    private MoRopId moRopId;

    private double pmCellDowntimeAuto;
    private double pmCellDowntimeMan;
    private double pmMacVolDl;
    private double pmMacVolUl;
    private double pmPdschAvailTime;
    private double pmPuschAvailTime;
    private double pmActiveUeDlSum;
    private double pmActiveUeDlMax;
    private double pmActiveUeDlSamp;
    private double pmActiveUeUlSum;
    private double pmActiveUeUlMax;
    private double pmActiveUeUlSamp;
    private double pmMacRBSymUsedPdcchTypeA;
    private double pmMacRBSymUsedPdcchTypeB;
    private double pmMacRBSymUsedPdschTypeA;
    private double pmMacRBSymUsedPdschTypeB;
    private double pmMacRBSymUsedPdschTypeABroadcasting;
    private double pmMacRBSymCsiRs;
    private double pmMacRBSymAvailDl;
    private double pmMacRBSymUsedPuschTypeA;
    private double pmMacRBSymUsedPuschTypeB;
    private double pmMacRBSymAvailUl;
    private double pmPdschSchedActivity;
    private double pmPuschSchedActivity;
    private double pmRlcDelayTimeDl;

    @Override
    public boolean fromAvroObject(GenericRecord avroObject) {
        if (avroObject.getSchema().getName().equals("NRCellDU_GNBDU_1")) {
            GenericRecord pmCounters = (GenericRecord) avroObject.get("pmCounters");
            moRopId = new MoRopId(ManagedObjectId.of(avroObject.get("moFdn").toString()), (Long)avroObject.get("ropEndTimeInEpoch"));
            try {
                setPmData(pmCounters);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error("Failed to set PmCounters for PmRopNRCellDU: {}", pmCounters.toString(), e);
                return false;
            }

            return true;
        }

        log.error("Failed to parse PmRopNRCellDU: incorrect object type: {}", avroObject.toString());

        return false;
    }
}
