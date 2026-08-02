package com.ctrc.report.presentation;

import com.ctrc.report.application.ReportService;
import com.ctrc.report.domain.Report;
import com.ctrc.report.domain.ReportFeedFilter;
import com.ctrc.report.application.dto.CommentRequest;
import com.ctrc.report.application.dto.CreateReportRequest;
import com.ctrc.core.presentation.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Object> createReport(@Valid @RequestBody CreateReportRequest request) {
        Object report = reportService.createReport(request);
        return ApiResponse.success(report);
    }

    /**
     * Uploads one photo and hands back its link. The app calls this first,
     * then sends the returned url as {@code imageUrl} when it creates the
     * report, so the create endpoint stays plain JSON.
     */
    @PostMapping("/upload-image")
    public ApiResponse<java.util.Map<String, String>> uploadImage(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            String url = reportService.uploadReportImage(file);
            return ApiResponse.success(java.util.Collections.singletonMap("url", url));
        } catch (java.io.IOException e) {
            throw new com.ctrc.core.domain.exceptions.ValidationException(
                    "could not upload the image: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Report> getReport(@PathVariable("id") Long id,
                                         @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(reportService.getReportById(id, userId));
    }

    @GetMapping
    public ApiResponse<List<Report>> getAllReports(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(reportService.getAllReports(userId));
    }

    /**
     * The home feed. Beyond the radius and the category chip, the app can ask
     * for a narrower slice and a different ordering; all of it is resolved in
     * the query rather than by re-sorting the list on the device.
     */
    @GetMapping("/nearby")
    public ApiResponse<List<Report>> getNearbyReports(
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng,
            @RequestParam(value = "radius", defaultValue = "5.0") Double radius,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "evidence", required = false) String evidence,
            @RequestParam(value = "withinHours", required = false) Integer withinHours,
            @RequestParam(value = "withPhoto", required = false) Boolean withPhoto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        ReportFeedFilter filter = ReportFeedFilter.of(sort, status, evidence, withinHours, withPhoto);
        return ApiResponse.success(reportService.getNearbyReports(lat, lng, radius, category, filter, userId));
    }

    @PutMapping("/{id}")
    public ApiResponse<Report> updateReport(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody com.ctrc.report.application.dto.UpdateReportRequest request) {
        return ApiResponse.success(reportService.updateReport(id, userId, request));
    }

    @PostMapping("/{id}/vote")
    public ApiResponse<Object> vote(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody com.ctrc.report.application.dto.VoteRequest request) {
        reportService.voteReport(id, userId, request.getType());
        return ApiResponse.success("Vote recorded successfully");
    }

    @PostMapping("/{id}/comments")
    public ApiResponse<Void> commentReport(@PathVariable("id") Long reportId,
                                           @RequestHeader("X-User-Id") Long userId,
                                           @RequestBody CommentRequest request) {
        reportService.commentReport(reportId, userId, request.getContent());
        return ApiResponse.success(null);
    }

    @GetMapping("/my-reports")
    public ApiResponse<List<Report>> getMyReports(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(reportService.getUserReports(userId, userId));
    }

    @GetMapping("/saved")
    public ApiResponse<List<Report>> getSavedReports(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(reportService.getSavedReports(userId, userId));
    }

    @PostMapping("/{id}/save")
    public ApiResponse<Void> saveReport(@PathVariable("id") Long reportId,
                                        @RequestHeader("X-User-Id") Long userId) {
        reportService.saveReport(userId, reportId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/save")
    public ApiResponse<Void> unsaveReport(@PathVariable("id") Long reportId,
                                          @RequestHeader("X-User-Id") Long userId) {
        reportService.unsaveReport(userId, reportId);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/comments")
    public ApiResponse<List<com.ctrc.report.domain.Comment>> getComments(@PathVariable("id") Long id) {
        return ApiResponse.success(reportService.getComments(id));
    }

    /**
     * Incidents within {@code corridorKm} of a route. One call replaces the
     * client probing /nearby repeatedly along a long trip.
     */
    @PostMapping("/along-route")
    public ApiResponse<List<com.ctrc.report.application.dto.RouteHazardDto>> getReportsAlongRoute(
            @Valid @RequestBody com.ctrc.report.application.dto.RouteScanRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(reportService.getReportsAlongRoute(
                request.getPath(), request.getCorridorKm(), request.getCategory(), userId));
    }

    @GetMapping("/{id}/votes")
    public ApiResponse<List<com.ctrc.report.domain.Vote>> getVotes(@PathVariable("id") Long id) {
        return ApiResponse.success(reportService.getVotes(id));
    }
}
