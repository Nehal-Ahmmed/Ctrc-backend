package com.ctrc.report.presentation;

import com.ctrc.report.application.ReportService;
import com.ctrc.report.domain.Report;
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
    public ApiResponse<Report> createReport(@Valid @RequestBody CreateReportRequest request) {
        Report report = reportService.createReport(request);
        return ApiResponse.success(report);
    }

    @GetMapping("/{id}")
    public ApiResponse<Report> getReport(@PathVariable("id") Long id) {
        return ApiResponse.success(reportService.getReportById(id));
    }

    @GetMapping
    public ApiResponse<List<Report>> getAllReports() {
        return ApiResponse.success(reportService.getAllReports());
    }

    @GetMapping("/nearby")
    public ApiResponse<List<Report>> getNearbyReports(
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng,
            @RequestParam(value = "radius", defaultValue = "5.0") Double radius) {
        return ApiResponse.success(reportService.getNearbyReports(lat, lng, radius));
    }
}
