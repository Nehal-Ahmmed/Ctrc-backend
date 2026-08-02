package com.ctrc.report.domain;

import java.util.List;
import java.util.Optional;

public interface SubReportRepository {

    Long insert(SubReport subReport);

    Optional<SubReport> findById(Long subReportId);

    /**
     * Every update filed against a parent report, oldest first, with its
     * location and author joined in so the client can render the thread
     * without a second round trip.
     */
    List<SubReport> findByReportId(Long reportId);
}
