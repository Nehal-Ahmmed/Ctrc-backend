package com.ctrc.report.application;

import com.ctrc.report.domain.Report;
import com.ctrc.report.domain.ReportRepository;
import com.ctrc.report.domain.SubReport;
import com.ctrc.report.domain.SubReportRepository;
import com.ctrc.report.domain.IncidentGroupRepository;
import com.ctrc.report.domain.SavedReportRepository;
import com.ctrc.report.application.dto.CreateReportRequest;
import com.ctrc.core.domain.exceptions.ResourceNotFoundException;
import com.ctrc.location.domain.Location;
import com.ctrc.location.domain.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final LocationRepository locationRepository;
    private final SubReportRepository subReportRepository;
    private final IncidentGroupRepository incidentGroupRepository;
    private final com.ctrc.report.domain.VoteRepository voteRepository;
    private final com.ctrc.report.domain.CommentRepository commentRepository;
    private final SavedReportRepository savedReportRepository;
    private final com.ctrc.core.services.CloudinaryService cloudinaryService;

    public ReportService(ReportRepository reportRepository,
                         LocationRepository locationRepository,
                         SubReportRepository subReportRepository,
                         IncidentGroupRepository incidentGroupRepository,
                         com.ctrc.report.domain.VoteRepository voteRepository,
                         com.ctrc.report.domain.CommentRepository commentRepository,
                         SavedReportRepository savedReportRepository,
                         com.ctrc.core.services.CloudinaryService cloudinaryService) {
        this.reportRepository = reportRepository;
        this.locationRepository = locationRepository;
        this.subReportRepository = subReportRepository;
        this.incidentGroupRepository = incidentGroupRepository;
        this.voteRepository = voteRepository;
        this.commentRepository = commentRepository;
        this.savedReportRepository = savedReportRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public String uploadReportImage(org.springframework.web.multipart.MultipartFile file)
            throws java.io.IOException {
        return cloudinaryService.uploadImage(file, "ctrc_reports");
    }

    @Transactional
    public Object createReport(CreateReportRequest request) {
        Location location = new Location();
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAddress(request.getAddress());
        location.setCity(request.getCity());

        Long locationId = locationRepository.insert(location);

        String evidenceType = request.getEvidenceType();

        if (request.getParentReportId() != null) {
            SubReport subReport = new SubReport();
            subReport.setUserId(request.getUserId());
            subReport.setReportId(request.getParentReportId());
            subReport.setLocationId(locationId);
            subReport.setDescription(request.getDescription());
            subReport.setEvidenceType(evidenceType != null ? evidenceType : "heard");
            subReport.setCategory(request.getCategory());
            subReport.setImageUrl(request.getImageUrl());

            subReportRepository.insert(subReport);

            // Hand back the parent with its thread already rebuilt, so the
            // client sees the update it just filed without a second call.
            return getReportById(request.getParentReportId(), request.getUserId());
        } else {
            Report report = new Report();
            report.setUserId(request.getUserId());
            report.setLocationId(locationId);
            report.setTitle(request.getTitle());
            report.setDescription(request.getDescription());
            report.setCategory(request.getCategory());
            report.setEvidenceType(evidenceType != null ? evidenceType : "seen");
            report.setImageUrl(request.getImageUrl());

            Long reportId = reportRepository.insert(report);
            
            incidentGroupRepository.insert(reportId, request.getTitle());

            return reportRepository.findByIdWithLocation(reportId, request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("report not found after creation"));
        }
    }

    public Report getReportById(Long reportId, Long currentUserId) {
        Report report = reportRepository.findByIdWithLocation(reportId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("report not found with id " + reportId));

        // The detail view is the one place the whole incident thread is shown,
        // so the linked updates are loaded here and nowhere else.
        List<SubReport> subReports = subReportRepository.findByReportId(reportId);
        report.setSubReports(subReports);
        report.setSubReportCount(subReports.size());

        return report;
    }

    public List<Report> getAllReports(Long currentUserId) {
        return reportRepository.findAll(currentUserId);
    }

    public List<Report> getNearbyReports(Double latitude, Double longitude, Double radiusInKm, String category, Long currentUserId) {
        Double radiusInMeters = radiusInKm * 1000;
        return reportRepository.findNearby(latitude, longitude, radiusInMeters, category, currentUserId, ReportRepository.DEFAULT_LIMIT);
    }

    public List<Report> getUserReports(Long userId, Long currentUserId) {
        return reportRepository.findByUserId(userId, currentUserId);
    }

    public List<Report> getSavedReports(Long userId, Long currentUserId) {
        return reportRepository.findSavedByUserId(userId, currentUserId);
    }

    @Transactional
    public void saveReport(Long userId, Long reportId) {
        savedReportRepository.save(userId, reportId);
    }

    @Transactional
    public void unsaveReport(Long userId, Long reportId) {
        savedReportRepository.unsave(userId, reportId);
    }

    @Transactional
    public void voteReport(Long reportId, Long userId, String type) {
        // Voting the same way twice removes the vote, voting the other way
        // replaces it. The upvote_count / downvote_count columns and the
        // report status are maintained by the trg_vote_insert and
        // trg_vote_delete triggers, so nothing is counted here.
        java.util.Optional<com.ctrc.report.domain.Vote> existingVote = voteRepository.findByUserAndReport(userId, reportId);

        if (existingVote.isPresent()) {
            com.ctrc.report.domain.Vote vote = existingVote.get();
            voteRepository.delete(vote.getVoteId());
            if (vote.getVoteType().equals(type)) {
                return;
            }
        }

        com.ctrc.report.domain.Vote newVote = new com.ctrc.report.domain.Vote();
        newVote.setReportId(reportId);
        newVote.setUserId(userId);
        newVote.setVoteType(type);
        voteRepository.insert(newVote);
    }

    @Transactional
    public void commentReport(Long reportId, Long userId, String content) {
        com.ctrc.report.domain.Comment comment = new com.ctrc.report.domain.Comment();
        comment.setReportId(reportId);
        comment.setUserId(userId);
        comment.setContent(content);
        commentRepository.insert(comment);
        // comment_count is not a column on `report`; every read derives it with
        // a subquery over `comment`, so there is nothing to increment here.
    }

    public List<com.ctrc.report.domain.Comment> getComments(Long reportId) {
        return commentRepository.findByReportId(reportId);
    }

    public List<com.ctrc.report.domain.Vote> getVotes(Long reportId) {
        return voteRepository.findByReportId(reportId);
    }

    /**
     * Incidents sitting within {@code corridorKm} either side of a route.
     *
     * <p>Runs in two passes: one indexed bounding-box query that pulls the
     * candidates, then an exact point-to-polyline filter in memory. This
     * replaces the client having to probe /nearby dozens of times along a long
     * trip.
     */
    public List<com.ctrc.report.application.dto.RouteHazardDto> getReportsAlongRoute(
            List<com.ctrc.report.application.dto.GeoPointDto> path,
            Double corridorKm,
            String category,
            Long currentUserId) {

        List<double[]> route = new java.util.ArrayList<>();
        for (com.ctrc.report.application.dto.GeoPointDto point : path) {
            if (point != null && point.getLat() != null && point.getLng() != null) {
                route.add(new double[] {point.getLat(), point.getLng()});
            }
        }

        if (route.isEmpty()) {
            throw new com.ctrc.core.domain.exceptions.ValidationException(
                    "path must contain at least one valid point");
        }

        double corridorMeters = (corridorKm == null ? 2.0 : corridorKm) * 1000.0;
        if (corridorMeters <= 0 || corridorMeters > MAX_CORRIDOR_METERS) {
            throw new com.ctrc.core.domain.exceptions.ValidationException(
                    "corridorKm must be between 0 and " + (MAX_CORRIDOR_METERS / 1000));
        }

        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;

        for (double[] point : route) {
            minLat = Math.min(minLat, point[0]);
            maxLat = Math.max(maxLat, point[0]);
            minLng = Math.min(minLng, point[1]);
            maxLng = Math.max(maxLng, point[1]);
        }

        // Grow the box by the corridor so incidents beside the ends are included.
        double latPadding = com.ctrc.report.domain.GeoMath.latitudeDegreesFor(corridorMeters);
        double lngPadding = com.ctrc.report.domain.GeoMath.longitudeDegreesFor(
                corridorMeters, Math.max(Math.abs(minLat), Math.abs(maxLat)));

        List<Report> candidates = reportRepository.findWithinBoundingBox(
                minLat - latPadding, maxLat + latPadding,
                minLng - lngPadding, maxLng + lngPadding,
                category, currentUserId, ReportRepository.DEFAULT_LIMIT);

        List<com.ctrc.report.application.dto.RouteHazardDto> hazards = new java.util.ArrayList<>();
        for (Report report : candidates) {
            Location location = report.getLocation();
            if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
                continue;
            }

            double[] projection = com.ctrc.report.domain.GeoMath.projectOnPolyline(
                    location.getLatitude(), location.getLongitude(), route);

            if (projection[0] <= corridorMeters) {
                hazards.add(new com.ctrc.report.application.dto.RouteHazardDto(
                        report, projection[0], projection[1]));
            }
        }

        hazards.sort(java.util.Comparator.comparingDouble(
                com.ctrc.report.application.dto.RouteHazardDto::getAlongMeters));
        return hazards;
    }

    /** Upper bound on the corridor width a caller may ask for. */
    private static final double MAX_CORRIDOR_METERS = 25_000;
}
