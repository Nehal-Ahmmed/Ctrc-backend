package com.ctrc.report.domain;

import com.ctrc.location.domain.Location;

import java.time.LocalDateTime;

public class SubReport {

    private Long subReportId;
    private Long userId;
    private Long reportId; 
    private Long locationId;
    private String description;

    private String evidenceType;

    private String category;

    private String imageUrl;

    private Double distFromParent;
    private Integer upvoteCount;
    private Integer downvoteCount;
    private LocalDateTime createdAt;

    private Integer commentCount;

    private Location location;

    private String authorName;
    private String authorImageUrl;

    private String userVoteType;
    private String parentTitle;

    public SubReport() {
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public String getUserVoteType() {
        return userVoteType;
    }

    public void setUserVoteType(String userVoteType) {
        this.userVoteType = userVoteType;
    }

    public String getParentTitle() {
        return parentTitle;
    }

    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
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
