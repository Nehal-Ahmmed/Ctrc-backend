package com.ctrc.report.application.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Body of {@code POST /api/reports/along-route}.
 *
 * <p>The client sends the road geometry it drew on the map and asks which
 * reported incidents sit inside the corridor beside it.
 */
public class RouteScanRequest {

    /** Ordered points of the route, as returned by the routing engine. */
    @NotEmpty(message = "path must contain at least one point")
    private List<GeoPointDto> path;

    /** Half-width of the corridor either side of the road, in kilometres. */
    private Double corridorKm = 2.0;

    /** Optional category filter, same semantics as the nearby endpoint. */
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
