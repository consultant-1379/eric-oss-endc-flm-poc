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
package com.ericsson.oss.apps.repository;

import com.ericsson.oss.apps.model.pmrop.MoRopId;
import com.ericsson.oss.apps.model.pmrop.PmRopNRCellCU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PmNRCellCURepo extends JpaRepository<PmRopNRCellCU, MoRopId> {
    @Query("select p.pmRrcConnLevelSumEnDc from PmRopNRCellCU p where p.moRopId = :moRopId")
    Optional<Double> findPmRrcConnLevelSumEnDcByMoRopId(MoRopId moRopId);

    @Query("select p.pmRrcConnLevelSamp from PmRopNRCellCU p where p.moRopId = :moRopId")
    Optional<Double> findPmRrcConnLevelSampByMoRopId(MoRopId moRopId);

    @Query("select p from PmRopNRCellCU p where p.moRopId.ropTime <= ?1")
    List<PmRopNRCellCU> findByRopTimeLessThanEqual(long ropTime);
}
