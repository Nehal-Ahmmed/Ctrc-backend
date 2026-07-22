package com.ctrc.report.domain;

import java.util.Optional;

public interface SubReportRepository {

    Long insert(SubReport subReport);

    Optional<SubReport> findById(Long subReportId);
}
