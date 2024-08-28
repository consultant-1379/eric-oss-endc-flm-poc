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

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ericsson.oss.apps.model.report.Report;

@Repository
public interface ReportDataRepo extends JpaRepository<Report, Long> {
    @Query("SELECT r FROM Report r ORDER BY r.ropTimeStamp DESC LIMIT ?1")
    Optional<List<Report>> getLatestReports(Integer nRops);

    @Query("SELECT r FROM Report r WHERE r.ropTimeStamp >= ?2 ORDER BY r.ropTimeStamp LIMIT ?1")
    Optional<List<Report>> getReportsAfter(Integer nRpos, Long startTimeStamp);

    @Query("SELECT r FROM Report r WHERE r.ropTimeStamp <= ?2 ORDER BY r.ropTimeStamp DESC LIMIT ?1")
    Optional<List<Report>> getReportsBefore(Integer nRpos, Long endTimeStamp);

    @Query("SELECT r FROM Report r WHERE r.ropTimeStamp >=?1 AND r.ropTimeStamp <= ?2 ORDER BY r.ropTimeStamp")
    Optional<List<Report>> getReportsBetween(Long startTimeStamp, Long endTimeStamp);

    @Query("SELECT r FROM Report r WHERE r.ropTimeStamp <= ?1")
    List<Report> findByRopTimeLessThanEqual(long ropTimeStamp);
}
