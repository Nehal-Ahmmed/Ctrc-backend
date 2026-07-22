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

    public ReportService(ReportRepository reportRepository, 
                         LocationRepository locationRepository,
                         SubReportRepository subReportRepository,
                         IncidentGroupRepository incidentGroupRepository) {
        this.reportRepository = reportRepository;
        this.locationRepository = locationRepository;
        this.subReportRepository = subReportRepository;
        this.incidentGroupRepository = incidentGroupRepository;
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

    public List<Report> getNearbyReports(Double latitude, Double longitude, Double radiusInKm) {
        Double radiusInMeters = radiusInKm * 1000;
        return reportRepository.findNearby(latitude, longitude, radiusInMeters);
    }
}
