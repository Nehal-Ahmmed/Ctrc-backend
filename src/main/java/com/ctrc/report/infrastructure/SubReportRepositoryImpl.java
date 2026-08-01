package com.ctrc.report.infrastructure;

import com.ctrc.report.domain.SubReport;
import com.ctrc.report.domain.SubReportRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class SubReportRepositoryImpl implements SubReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public SubReportRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<SubReport> ROW_MAPPER = (rs, rowNum) -> {
        SubReport sr = new SubReport();
        sr.setSubReportId(rs.getLong("sub_report_id"));
        sr.setUserId(rs.getLong("user_id"));
        sr.setReportId(rs.getLong("report_id"));
        sr.setLocationId(rs.getLong("location_id"));
        sr.setDescription(rs.getString("description"));
        sr.setDistFromParent(rs.getDouble("dist_from_parent"));
        sr.setUpvoteCount(rs.getInt("upvote_count"));
        sr.setDownvoteCount(rs.getInt("downvote_count"));
        sr.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return sr;
    };

    @Override
    public Long insert(SubReport subReport) {
        String sql = "insert into sub_report (user_id, report_id, location_id, description, dist_from_parent, upvote_count, downvote_count) "
                   + "values (?, ?, ?, ?, "
                   + "(select ST_Distance_Sphere(POINT(l1.longitude, l1.latitude), POINT(l2.longitude, l2.latitude)) "
                   + "from location l1 cross join location l2 "
                   + "where l1.location_id = ? and l2.location_id = (select location_id from report where report_id = ?)), "
                   + "0, 0)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, subReport.getUserId());
            ps.setLong(2, subReport.getReportId());
            ps.setLong(3, subReport.getLocationId());
            ps.setString(4, subReport.getDescription());
            ps.setLong(5, subReport.getLocationId());
            ps.setLong(6, subReport.getReportId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<SubReport> findById(Long subReportId) {
        String sql = "select * from sub_report where sub_report_id = ?";
        List<SubReport> results = jdbcTemplate.query(sql, ROW_MAPPER, subReportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
