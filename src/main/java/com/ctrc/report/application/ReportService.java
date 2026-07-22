package com.ctrc.report.application;

import com.ctrc.report.domain.Report;
import com.ctrc.report.domain.ReportRepository;
import com.ctrc.report.domain.SubReport;
import com.ctrc.report.domain.SubReportRepository;
import com.ctrc.report.domain.IncidentGroupRepository;
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

    public ReportService(ReportRepository reportRepository, 
                         LocationRepository locationRepository,
                         SubReportRepository subReportRepository,
                         IncidentGroupRepository incidentGroupRepository,
                         com.ctrc.report.domain.VoteRepository voteRepository,
                         com.ctrc.report.domain.CommentRepository commentRepository) {
        this.reportRepository = reportRepository;
        this.locationRepository = locationRepository;
        this.subReportRepository = subReportRepository;
        this.incidentGroupRepository = incidentGroupRepository;
        this.voteRepository = voteRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Object createReport(CreateReportRequest request) {
        Location location = new Location();
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAddress(request.getAddress());
        location.setCity(request.getCity());

        Long locationId = locationRepository.insert(location);

        if (request.getParentReportId() != null) {
            SubReport subReport = new SubReport();
            subReport.setUserId(request.getUserId());
            subReport.setReportId(request.getParentReportId());
            subReport.setLocationId(locationId);
            subReport.setDescription(request.getDescription());
            
            subReportRepository.insert(subReport);
            
            return reportRepository.findByIdWithLocation(request.getParentReportId())
                    .orElseThrow(() -> new ResourceNotFoundException("parent report not found"));
        } else {
            Report report = new Report();
            report.setUserId(request.getUserId());
            report.setLocationId(locationId);
            report.setTitle(request.getTitle());
            report.setDescription(request.getDescription());
            report.setCategory(request.getCategory());

            Long reportId = reportRepository.insert(report);
            
            incidentGroupRepository.insert(reportId, request.getTitle());

            return reportRepository.findByIdWithLocation(reportId)
                    .orElseThrow(() -> new ResourceNotFoundException("report not found after creation"));
        }
    }

    public Report getReportById(Long reportId) {
        return reportRepository.findByIdWithLocation(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("report not found with id " + reportId));
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getNearbyReports(Double latitude, Double longitude, Double radiusInKm, String category) {
        Double radiusInMeters = radiusInKm * 1000;
        return reportRepository.findNearby(latitude, longitude, radiusInMeters, category);
    }

    @Transactional
    public void voteReport(Long reportId, Long userId, String type) {
        // Simple logic for now: delete existing vote if any, adjust count, add new vote
        java.util.Optional<com.ctrc.report.domain.Vote> existingVote = voteRepository.findByUserAndReport(userId, reportId);
        
        if (existingVote.isPresent()) {
            com.ctrc.report.domain.Vote vote = existingVote.get();
            if (vote.getVoteType().equals(type)) {
                return; // already voted same type
            } else {
                // remove old vote effect
                if ("up".equals(vote.getVoteType())) {
                    reportRepository.updateUpvotes(reportId, -1);
                } else {
                    reportRepository.updateDownvotes(reportId, -1);
                }
                voteRepository.delete(vote.getVoteId());
            }
        }

        // Add new vote
        com.ctrc.report.domain.Vote newVote = new com.ctrc.report.domain.Vote();
        newVote.setReportId(reportId);
        newVote.setUserId(userId);
        newVote.setVoteType(type);
        voteRepository.insert(newVote);

        if ("up".equals(type)) {
            reportRepository.updateUpvotes(reportId, 1);
        } else {
            reportRepository.updateDownvotes(reportId, 1);
        }
    }

    @Transactional
    public void commentReport(Long reportId, Long userId, String content) {
        com.ctrc.report.domain.Comment comment = new com.ctrc.report.domain.Comment();
        comment.setReportId(reportId);
        comment.setUserId(userId);
        comment.setContent(content);
        commentRepository.insert(comment);
        
        reportRepository.updateCommentCount(reportId, 1);
    }

    public List<com.ctrc.report.domain.Comment> getComments(Long reportId) {
        return commentRepository.findByReportId(reportId);
    }
}
