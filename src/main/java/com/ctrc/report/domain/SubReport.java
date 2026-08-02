package com.ctrc.report.domain;

import com.ctrc.location.domain.Location;

import java.time.LocalDateTime;

public class SubReport {

    private Long subReportId;
    private Long userId;
    private Long reportId; // The parent report ID
    private Long locationId;
    private String description;

    // how the reporter knows: 'seen', 'heard' or 'guessed'
    private String evidenceType;

    // what the reporter thinks the incident is; promotes an 'Unknown' parent
    private String category;

    // photo of the incident, hosted on cloudinary
    private String imageUrl;

    private Double distFromParent;
    private Integer upvoteCount;
    private Integer downvoteCount;
    private LocalDateTime createdAt;

    // comments hang off sub_report_id via the dual-FK comment table
    private Integer commentCount;

    // populated when fetched with location
    private Location location;

    // joined server side so an update renders without a second lookup
    private String authorName;
    private String authorImageUrl;

    public SubReport() {
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorImageUrl() {
        return authorImageUrl;
    }

    public void setAuthorImageUrl(String authorImageUrl) {
        this.authorImageUrl = authorImageUrl;
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

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
