package com.ctrc.report.domain;

import java.util.List;

public interface SavedReportRepository {
    void save(Long userId, Long reportId);
    void unsave(Long userId, Long reportId);
    List<SavedReport> findByUserId(Long userId);
    boolean isSaved(Long userId, Long reportId);
}
