package com.ctrc.report.domain;

import java.sql.Timestamp;

public class Vote {
    private Long voteId;
    private Long userId;
    private Long reportId;
    private Long subReportId;
    private String voteType; 
    private Timestamp votedAt;
    private String userName;
    private String userImageUrl;

    public Vote() {}

    public Long getVoteId() {
        return voteId;
    }

    public void setVoteId(Long voteId) {
        this.voteId = voteId;
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

    public Long getSubReportId() {
        return subReportId;
    }

    public void setSubReportId(Long subReportId) {
        this.subReportId = subReportId;
    }

    public String getVoteType() {
        return voteType;
    }

    public void setVoteType(String voteType) {
        this.voteType = voteType;
    }

    public Timestamp getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(Timestamp votedAt) {
        this.votedAt = votedAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }
}
