package com.ctrc.report.presentation;

import com.ctrc.report.application.ReportService;
import com.ctrc.report.domain.SubReport;
import com.ctrc.report.application.dto.CommentRequest;
import com.ctrc.core.presentation.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sub-reports")
public class SubReportController {

    private final ReportService reportService;
    private final com.ctrc.report.application.CommentService commentService;

    public SubReportController(ReportService reportService,
                               com.ctrc.report.application.CommentService commentService) {
        this.reportService = reportService;
        this.commentService = commentService;
    }

    @GetMapping("/{id}")
    public ApiResponse<SubReport> getSubReport(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(reportService.getSubReportById(id, userId));
    }

    @GetMapping("/{id}/comments")
    public ApiResponse<List<com.ctrc.report.domain.Comment>> getComments(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(commentService.getSubReportComments(id, userId));
    }

    @PostMapping("/{id}/comments")
    public ApiResponse<Void> commentSubReport(
            @PathVariable("id") Long subReportId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CommentRequest request) {
        reportService.commentSubReport(subReportId, userId, request.getContent());
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/vote")
    public ApiResponse<Object> vote(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody com.ctrc.report.application.dto.VoteRequest request) {
        reportService.voteSubReport(id, userId, request.getType());
        return ApiResponse.success("Vote recorded successfully");
    }

    @GetMapping("/{id}/votes")
    public ApiResponse<List<com.ctrc.report.domain.Vote>> getVotes(@PathVariable("id") Long id) {
        return ApiResponse.success(reportService.getSubReportVotes(id));
    }
}
