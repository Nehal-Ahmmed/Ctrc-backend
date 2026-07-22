package com.ctrc.report.domain;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {

    Long insert(Report report);

    Optional<Report> findById(Long reportId);

    // joined with location, used for feed and detail views
    Optional<Report> findByIdWithLocation(Long reportId);

    List<Report> findAll();

    List<Report> findNearby(Double latitude, Double longitude, Double radiusInMeters, String category);
    void updateUpvotes(Long reportId, int delta);
    void updateDownvotes(Long reportId, int delta);
    void updateCommentCount(Long reportId, int delta);
    List<Report> findByUserId(Long userId);
    List<Report> findSavedByUserId(Long userId);
}
