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

import com.ericsson.oss.apps.loader.CmDataLoader;
import com.ericsson.oss.apps.model.mom.*;
import com.ericsson.oss.apps.ncmp.NcmpClient;
import com.ericsson.oss.apps.ncmp.model.ManagedElement;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.util.ManagedObjectAggregate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class CmDataLoaderConf {
    @Bean
    public CmDataLoader cmDataLoader(NcmpClient ncmpClient) {
        return new CmDataLoader(ncmpClient);
    }

    @Bean
    public ManagedObjectAggregate endcDistrProfileAggregate() {
        return new ManagedObjectAggregate() {
            @Override
            public Class<? extends ManagedObject> getKey() {
                return ManagedElement.class;
            }

            @Override
            public Set<Class<? extends ManagedObject>> getTypes() {
                return Set.of(
                        ENodeBFunction.class,
                        EndcDistrProfile.class,
                        EUtranCellTDD.class,
                        ExternalGNodeBFunction.class,
                        ExternalGUtranCell.class,
                        ExternalGNBCUCPFunction.class,
                        ExternalNRCellCU.class,
                        FeatureState.class,
                        GUtranCellRelation.class,
                        GUtranFreqRelation.class,
                        GUtranSyncSignalFrequency.class,
                        GNBCUCPFunction.class,
                        SectorCarrier.class,
                        TermPointToGNB.class,
                        TrStPsCellProfileUeCfg.class,
                        TrStPSCellProfile.class,
                        NRCellCU.class,
                        NRCellDU.class,
                        NRCellRelation.class,
                        NRFrequency.class,
                        NRSectorCarrier.class);
            }
        };
    }
}
