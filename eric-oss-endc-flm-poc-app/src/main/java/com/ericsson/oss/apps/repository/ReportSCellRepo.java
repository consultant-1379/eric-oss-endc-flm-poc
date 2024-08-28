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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ericsson.oss.apps.model.report.ReportDataId;
import com.ericsson.oss.apps.model.report.SCellReportTuple;

import java.util.List;

@Repository
public interface ReportSCellRepo extends JpaRepository<SCellReportTuple, ReportDataId> {

    @Query("SELECT r FROM SCellReportTuple r WHERE r.reportDataId.ropTimeStamp <= ?1")
    List<SCellReportTuple> findByRopTimeLessThanEqual(long ropTimeStamp);
}
