package com.ctrc.report.application.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class RouteScanRequest {

    @NotEmpty(message = "path must contain at least one point")
    private List<GeoPointDto> path;

    private Double corridorKm = 2.0;

    private String category;

    public List<GeoPointDto> getPath() {
        return path;
    }

    public void setPath(List<GeoPointDto> path) {
        this.path = path;
    }

    public Double getCorridorKm() {
        return corridorKm;
    }

    public void setCorridorKm(Double corridorKm) {
        this.corridorKm = corridorKm;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
