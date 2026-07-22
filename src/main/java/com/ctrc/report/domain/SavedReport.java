package com.ctrc.report.domain;

import java.time.LocalDateTime;

public class SavedReport {
    private Long savedReportId;
    private Long userId;
    private Long reportId;
    private LocalDateTime savedAt;

    public SavedReport() {}

    public SavedReport(Long userId, Long reportId) {
        this.userId = userId;
        this.reportId = reportId;
        this.savedAt = LocalDateTime.now();
    }

    public Long getSavedReportId() {
        return savedReportId;
    }

    public void setSavedReportId(Long savedReportId) {
        this.savedReportId = savedReportId;
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

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
}
