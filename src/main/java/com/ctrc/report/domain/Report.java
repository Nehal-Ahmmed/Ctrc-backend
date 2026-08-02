package com.ctrc.report.domain;

import com.ctrc.location.domain.Location;

import java.time.LocalDateTime;
import java.util.List;

// plain model matching the report table, includes location for joined reads
public class Report {

    private Long reportId;
    private Long userId;
    private Long locationId;
    private String title;
    private String description;
    private String category;

    // how the reporter knows: 'seen', 'heard' or 'guessed'
    private String evidenceType;

    // 'unverified', 'verified' or 'disputed', maintained by a db trigger
    private String status;

    // photo of the incident, hosted on cloudinary
    private String imageUrl;

    private Integer upvoteCount;
    private Integer downvoteCount;
    private Integer commentCount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    // populated when report is fetched with its location joined, not a db column
    private Location location;

    // joined from the user table so clients don't have to resolve the author
    private String authorName;
    private String authorImageUrl;

    // true when the requesting user (X-User-Id) has bookmarked this report
    private Boolean isSaved;

    // 'up', 'down', or null representing the current user's vote
    private String userVoteType;

    // how many updates (sub-reports) hang off this incident; counted on every
    // list read so a card can say "3 updates" without loading them
    private Integer subReportCount;

    // the updates themselves, filled in only for the single-report read
    private List<SubReport> subReports;

    public Report() {
    }

    public Integer getSubReportCount() {
        return subReportCount;
    }

    public void setSubReportCount(Integer subReportCount) {
        this.subReportCount = subReportCount;
    }

    public List<SubReport> getSubReports() {
        return subReports;
    }

    public void setSubReports(List<SubReport> subReports) {
        this.subReports = subReports;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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

    public Boolean getIsSaved() {
        return isSaved;
    }

    public void setIsSaved(Boolean isSaved) {
        this.isSaved = isSaved;
    }

    public String getUserVoteType() {
        return userVoteType;
    }

    public void setUserVoteType(String userVoteType) {
        this.userVoteType = userVoteType;
    }
}
