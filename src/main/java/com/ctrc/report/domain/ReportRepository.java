package com.ctrc.report.domain;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {

    int DEFAULT_LIMIT = 300;

    Long insert(Report report);

    Optional<Report> findById(Long reportId, Long viewerUserId);

    Optional<Report> findByIdWithLocation(Long reportId, Long viewerUserId);

    List<Report> findAll(Long viewerUserId);

    List<Report> findNearby(Double latitude, Double longitude, Double radiusInMeters,
                            String category, ReportFeedFilter filter, Long viewerUserId, int limit);

    List<Report> findWithinBoundingBox(double minLatitude, double maxLatitude,
                                       double minLongitude, double maxLongitude,
                                       String category, Long viewerUserId, int limit);

    int update(Report report);

    int softDelete(Long reportId, Long userId);

    void updateUpvotes(Long reportId, int delta);

    void updateDownvotes(Long reportId, int delta);

    List<Report> findByUserId(Long userId, Long viewerUserId);

    List<Report> findSavedByUserId(Long userId, Long viewerUserId);
}
