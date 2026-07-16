package com.ctrc.report.application;

import com.ctrc.report.domain.Report;
import com.ctrc.report.domain.ReportRepository;
import com.ctrc.report.application.dto.CreateReportRequest;
import com.ctrc.core.domain.exceptions.ResourceNotFoundException;
import com.ctrc.location.domain.Location;
import com.ctrc.location.domain.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportService {

    private final ReportRepository ReportRepository;
    private final LocationRepository LocationRepository;

    public ReportService(ReportRepository ReportRepository, LocationRepository LocationRepository) {
        this.ReportRepository = ReportRepository;
        this.LocationRepository = LocationRepository;
    }

    // creates the location row then the report row in one transaction
    @Transactional
    public Report createReport(CreateReportRequest request) {
        Location location = new Location();
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAddress(request.getAddress());
        location.setCity(request.getCity());

        Long locationId = LocationRepository.insert(location);

        Report report = new Report();
        report.setUserId(request.getUserId());
        report.setLocationId(locationId);
        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setCategory(request.getCategory());

        Long reportId = ReportRepository.insert(report);

        return ReportRepository.findByIdWithLocation(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("report not found after creation"));
    }

    public Report getReportById(Long reportId) {
        return ReportRepository.findByIdWithLocation(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("report not found with id " + reportId));
    }

    public List<Report> getAllReports() {
        return ReportRepository.findAll();
    }
}
