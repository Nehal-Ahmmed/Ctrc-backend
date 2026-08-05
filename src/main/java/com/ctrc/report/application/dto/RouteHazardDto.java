package com.ctrc.report.application.dto;

import com.ctrc.report.domain.Report;

public class RouteHazardDto {

    private Report report;

    private double offsetMeters;

    private double alongMeters;

    public RouteHazardDto() {
    }

    public RouteHazardDto(Report report, double offsetMeters, double alongMeters) {
        this.report = report;
        this.offsetMeters = offsetMeters;
        this.alongMeters = alongMeters;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public double getOffsetMeters() {
        return offsetMeters;
    }

    public void setOffsetMeters(double offsetMeters) {
        this.offsetMeters = offsetMeters;
    }

    public double getAlongMeters() {
        return alongMeters;
    }

    public void setAlongMeters(double alongMeters) {
        this.alongMeters = alongMeters;
    }
}
