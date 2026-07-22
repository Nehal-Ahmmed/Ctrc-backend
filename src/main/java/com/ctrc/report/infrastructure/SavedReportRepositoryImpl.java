package com.ctrc.report.infrastructure;

import com.ctrc.report.domain.SavedReport;
import com.ctrc.report.domain.SavedReportRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SavedReportRepositoryImpl implements SavedReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public SavedReportRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<SavedReport> ROW_MAPPER = (rs, rowNum) -> {
        SavedReport savedReport = new SavedReport();
        savedReport.setSavedReportId(rs.getLong("saved_report_id"));
        savedReport.setUserId(rs.getLong("user_id"));
        savedReport.setReportId(rs.getLong("report_id"));
        if (rs.getTimestamp("saved_at") != null) {
            savedReport.setSavedAt(rs.getTimestamp("saved_at").toLocalDateTime());
        }
        return savedReport;
    };

    @Override
    public void save(Long userId, Long reportId) {
        // Use insert ignore to prevent duplicate entries if user saves multiple times rapidly
        String sql = "INSERT IGNORE INTO saved_report (user_id, report_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, reportId);
    }

    @Override
    public void unsave(Long userId, Long reportId) {
        String sql = "DELETE FROM saved_report WHERE user_id = ? AND report_id = ?";
        jdbcTemplate.update(sql, userId, reportId);
    }

    @Override
    public List<SavedReport> findByUserId(Long userId) {
        String sql = "SELECT * FROM saved_report WHERE user_id = ? ORDER BY saved_at DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }

    @Override
    public boolean isSaved(Long userId, Long reportId) {
        String sql = "SELECT COUNT(*) FROM saved_report WHERE user_id = ? AND report_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, reportId);
        return count != null && count > 0;
    }
}
