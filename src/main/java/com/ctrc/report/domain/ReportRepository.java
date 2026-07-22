package com.ctrc.report.domain;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {

    Long insert(Report report);

    Optional<Report> findById(Long reportId);

    // joined with location, used for feed and detail views
    Optional<Report> findByIdWithLocation(Long reportId);

    List<Report> findAll();

    List<Report> findNearby(Double latitude, Double longitude, Double radiusInMeters);
}
