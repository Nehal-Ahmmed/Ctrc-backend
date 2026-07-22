package com.ctrc.report.domain;

import com.ctrc.location.domain.Location;

import java.time.LocalDateTime;

public class SubReport {

    private Long subReportId;
    private Long userId;
    private Long reportId; // The parent report ID
    private Long locationId;
    private String description;
    private Double distFromParent;
    private Integer upvoteCount;
    private Integer downvoteCount;
    private LocalDateTime createdAt;
    
    // populated when fetched with location
    private Location location;

    public SubReport() {
    }

    public Long getSubReportId() {
        return subReportId;
    }

    public void setSubReportId(Long subReportId) {
        this.subReportId = subReportId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDistFromParent() {
        return distFromParent;
    }

    public void setDistFromParent(Double distFromParent) {
        this.distFromParent = distFromParent;
    }

    public Integer getUpvoteCount() {
        return upvoteCount;
    }

    public void setUpvoteCount(Integer upvoteCount) {
        this.upvoteCount = upvoteCount;
    }

    public Integer getDownvoteCount() {
        return downvoteCount;
    }

    public void setDownvoteCount(Integer downvoteCount) {
        this.downvoteCount = downvoteCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
