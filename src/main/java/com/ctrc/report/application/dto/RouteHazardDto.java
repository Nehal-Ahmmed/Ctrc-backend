package com.ctrc.report.application.dto;

import com.ctrc.report.domain.Report;

/** A report that sits inside the route corridor, with its position relative to the road. */
public class RouteHazardDto {

    private Report report;

    /** Perpendicular distance from the centre line of the road, in meters. */
    private double offsetMeters;

    /** How far along the route the incident sits, in meters. Used for ordering. */
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
