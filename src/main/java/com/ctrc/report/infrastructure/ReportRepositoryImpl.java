package com.ctrc.report.infrastructure;

import com.ctrc.report.domain.Report;
import com.ctrc.report.domain.ReportRepository;
import com.ctrc.location.domain.Location;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ReportRepositoryImpl implements ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Report> ROW_MAPPER = (rs, rowNum) -> {
        Report report = new Report();
        report.setReportId(rs.getLong("report_id"));
        report.setUserId(rs.getLong("user_id"));
        report.setLocationId(rs.getLong("location_id"));
        report.setTitle(rs.getString("title"));
        report.setDescription(rs.getString("description"));
        report.setCategory(rs.getString("category"));
        report.setUpvoteCount(rs.getInt("upvote_count"));
        report.setDownvoteCount(rs.getInt("downvote_count"));
        
        try {
            int commentCount = rs.getInt("comment_count");
            report.setCommentCount(commentCount);
        } catch (java.sql.SQLException e) {
            // comment_count column might not be present in all queries
            report.setCommentCount(0);
        }

        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            report.setExpiresAt(expiresAt.toLocalDateTime());
        }
        report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return report;
    };

    // maps a report joined with its location row in one query
    private static final RowMapper<Report> ROW_MAPPER_WITH_LOCATION = (rs, rowNum) -> {
        Report report = ROW_MAPPER.mapRow(rs, rowNum);

        Location location = new Location();
        location.setLocationId(rs.getLong("location_id"));
        location.setLongitude(rs.getDouble("longitude"));
        location.setLatitude(rs.getDouble("latitude"));
        location.setAddress(rs.getString("loc_address"));
        location.setCity(rs.getString("city"));
        report.setLocation(location);

        return report;
    };

    @Override
    public Long insert(Report report) {
        String sql = "insert into report (user_id, location_id, title, description, category, "
                + "upvote_count, downvote_count, expires_at) values (?, ?, ?, ?, ?, 0, 0, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, report.getUserId());
            ps.setLong(2, report.getLocationId());
            ps.setString(3, report.getTitle());
            ps.setString(4, report.getDescription());
            ps.setString(5, report.getCategory());
            if (report.getExpiresAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(report.getExpiresAt()));
            } else {
                ps.setTimestamp(6, null);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : 0L;
    }

    @Override
    public Optional<Report> findById(Long reportId) {
        String sql = "select r.*, (select count(*) from comment c where c.report_id = r.report_id) as comment_count from report r where r.report_id = ?";
        List<Report> results = jdbcTemplate.query(sql, ROW_MAPPER, reportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<Report> findByIdWithLocation(Long reportId) {
        String sql = "select r.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
                + "(select count(*) from comment c where c.report_id = r.report_id) as comment_count "
                + "from report r join location l on r.location_id = l.location_id "
                + "where r.report_id = ?";
        List<Report> results = jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, reportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Report> findAll() {
        String sql = "select r.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
                + "(select count(*) from comment c where c.report_id = r.report_id) as comment_count "
                + "from report r join location l on r.location_id = l.location_id "
                + "order by r.created_at desc";
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION);
    }

    @Override
    public List<Report> findNearby(Double latitude, Double longitude, Double radiusInMeters, String category) {
        String sql = "select r.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
                + "(select count(*) from comment c where c.report_id = r.report_id) as comment_count "
                + "from report r join location l on r.location_id = l.location_id "
                + "where ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(?, ?)) <= ? ";
        
        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            sql += "and r.category = ? ";
            sql += "order by ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(?, ?)) asc";
            return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, longitude, latitude, radiusInMeters, category, longitude, latitude);
        } else {
            sql += "order by ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(?, ?)) asc";
            return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, longitude, latitude, radiusInMeters, longitude, latitude);
        }
    }

    @Override
    public void updateUpvotes(Long reportId, int delta) {
        String sql = "UPDATE report SET upvote_count = upvote_count + ? WHERE report_id = ?";
        jdbcTemplate.update(sql, delta, reportId);
    }

    @Override
    public void updateDownvotes(Long reportId, int delta) {
        String sql = "UPDATE report SET downvote_count = downvote_count + ? WHERE report_id = ?";
        jdbcTemplate.update(sql, delta, reportId);
    }

    @Override
    public void updateCommentCount(Long reportId, int delta) {
        String sql = "UPDATE report SET comment_count = comment_count + ? WHERE report_id = ?";
        jdbcTemplate.update(sql, delta, reportId);
    }

    @Override
    public List<Report> findByUserId(Long userId) {
        String sql = "select r.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
                + "(select count(*) from comment c where c.report_id = r.report_id) as comment_count "
                + "from report r join location l on r.location_id = l.location_id "
                + "where r.user_id = ? "
                + "order by r.created_at desc";
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, userId);
    }

    @Override
    public List<Report> findSavedByUserId(Long userId) {
        String sql = "select r.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
                + "(select count(*) from comment c where c.report_id = r.report_id) as comment_count "
                + "from report r join location l on r.location_id = l.location_id "
                + "join saved_report sr on sr.report_id = r.report_id "
                + "where sr.user_id = ? "
                + "order by sr.saved_at desc";
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, userId);
    }
}
