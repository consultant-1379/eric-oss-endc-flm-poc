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
package com.ericsson.oss.apps.model.entities;

import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class EutranCellLoadChange {

    @EmbeddedId
    private ManagedObjectId objectId;
    private static final Integer NUM_ROP_TO_STORE = 3;

    private List<Float> loadChangeFR1Rop1 = new ArrayList<>();
    private List<Float> loadChangeFr1Rop2 = new ArrayList<>();
    private List<Float> loadChangeFR1Rop3 = new ArrayList<>();

    private List<Float> loadChangeFR2Rop1 = new ArrayList<>();
    private List<Float> loadChangeFR2Rop2 = new ArrayList<>();
    private List<Float> loadChangeFR2Rop3 = new ArrayList<>();

    public EutranCellLoadChange(ManagedObjectId objectId) {
        this.objectId = objectId;
    }

    public List<List<Float>> getLoadChangeFR1() {
        List<List<Float>> loadChangeFR1 = new ArrayList<>();
        if (!loadChangeFR1Rop1.isEmpty()) {
            loadChangeFR1.add(loadChangeFR1Rop1);
        }
        if (!loadChangeFr1Rop2.isEmpty()) {
            loadChangeFR1.add(loadChangeFr1Rop2);
        }
        if (!loadChangeFR1Rop3.isEmpty()) {
            loadChangeFR1.add(loadChangeFR1Rop3);
        }
        return loadChangeFR1;
    }

    public List<List<Float>> getLoadChangeFR2() {
        List<List<Float>> loadChangeFR2 = new ArrayList<>();
        if (!loadChangeFR2Rop1.isEmpty()) {
            loadChangeFR2.add(loadChangeFR2Rop1);
        }
        if (!loadChangeFR2Rop2.isEmpty()) {
            loadChangeFR2.add(loadChangeFR2Rop2);
        }
        if (!loadChangeFR2Rop3.isEmpty()) {
            loadChangeFR2.add(loadChangeFR2Rop3);
        }
        return loadChangeFR2;
    }

    public void setLoadChangeFR1(List<List<Float>> loadChangeList) {
        int validIndex = loadChangeList.size() - 1;
        if (validIndex >= 0) {
            loadChangeFR1Rop1 = loadChangeList.get(0);
        }
        if (validIndex >= 1) {
            loadChangeFr1Rop2 = loadChangeList.get(1);
        }
        if (validIndex >= 2) {
            loadChangeFR1Rop3 = loadChangeList.get(2);
        }
    }

    public void setLoadChangeFR2(List<List<Float>> loadChangeList) {
        int validIndex = loadChangeList.size() - 1;
        if (validIndex >= 0) {
            loadChangeFR2Rop1 = loadChangeList.get(0);
        }
        if (validIndex >= 1) {
            loadChangeFR2Rop2 = loadChangeList.get(1);
        }
        if (validIndex >= 2) {
            loadChangeFR2Rop3 = loadChangeList.get(2);
        }
    }

    public int getNumRopTimeToStore() {
        return NUM_ROP_TO_STORE;
    }
}