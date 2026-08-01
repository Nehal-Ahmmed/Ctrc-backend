package com.ctrc.report.domain;

import java.util.List;
import java.util.Optional;

public interface ReportRepository {

    /**
     * Default cap applied to feed-style queries so a division-wide radius
     * cannot pull the whole table into memory.
     */
    int DEFAULT_LIMIT = 300;

    Long insert(Report report);

    Optional<Report> findById(Long reportId, Long viewerUserId);

    // joined with location, used for feed and detail views
    Optional<Report> findByIdWithLocation(Long reportId, Long viewerUserId);

    List<Report> findAll(Long viewerUserId);

    List<Report> findNearby(Double latitude, Double longitude, Double radiusInMeters,
                            String category, Long viewerUserId, int limit);

    /**
     * Reports whose location falls inside a lat/lng box. Used as the cheap
     * first pass of the route-corridor scan before the exact geometry filter.
     */
    List<Report> findWithinBoundingBox(double minLatitude, double maxLatitude,
                                       double minLongitude, double maxLongitude,
                                       String category, Long viewerUserId, int limit);

    void updateUpvotes(Long reportId, int delta);

    void updateDownvotes(Long reportId, int delta);

    List<Report> findByUserId(Long userId, Long viewerUserId);

    List<Report> findSavedByUserId(Long userId, Long viewerUserId);
}
