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

import org.apache.avro.generic.GenericRecord;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public interface PmRop {

    default void setPmData(GenericRecord pmCounters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            if(fieldName.startsWith("pm")) {
                GenericRecord pmCounterKey = (GenericRecord) pmCounters.get(fieldName);

                Object fieldValue = getDefaultValue(fieldName);
                if((boolean) pmCounterKey.get("isValuePresent")) {
                    fieldValue = pmCounterKey.get("counterValue");
                }

                String setMethodName = "set" + fieldName.substring(0,1).toUpperCase(Locale.ENGLISH) + fieldName.substring(1);
                Method setterMethod = this.getClass().getMethod(setMethodName, field.getType());
                setterMethod.invoke(this, fieldValue);
            }
        }
    }

    MoRopId getMoRopId();
    boolean fromAvroObject(GenericRecord avorObj);
    default Object getDefaultValue(String fieldName){
        return Double.NaN;
    }
}
