package com.ctrc.report.domain;

import java.util.List;
import java.util.Optional;

public interface SubReportRepository {

    Long insert(SubReport subReport);

    Optional<SubReport> findById(Long subReportId);

    Optional<SubReport> findByIdWithLocation(Long subReportId, Long currentUserId);

    List<SubReport> findByReportId(Long reportId);
}
