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

    /**
     * Hides reports whose lifetime has already elapsed. An incident that has
     * expired is no longer a live road condition, so it must not reach the feed
     * or the map.
     */
    /**
     * No user can own this id, so for anonymous callers the viewer-specific
     * subqueries simply match nothing instead of binding a NULL parameter.
     */
    private static final long ANONYMOUS_VIEWER = -1L;

    private static long viewer(Long viewerUserId) {
        return viewerUserId != null ? viewerUserId : ANONYMOUS_VIEWER;
    }

    private static final String NOT_EXPIRED =
            "(r.expires_at is null or r.expires_at > now()) ";

    /**
     * Shared projection for every "rich" read: report + location + author +
     * the two viewer-specific flags.
     *
     * <p>The two {@code ?} placeholders in the correlated subqueries are always
     * the first two bind parameters, so callers pass the viewer id twice before
     * their own arguments.
     */
    private static final String SELECT_WITH_LOCATION =
            "select r.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
            + "u.name as author_name, u.image_url as author_image_url, "
            + "(select count(*) from comment c where c.report_id = r.report_id) as comment_count, "
            + "v.vote_type as user_vote_type, "
            // CASE rather than `(... is not null) as is_saved`: a bare boolean
            // expression with an alias is the kind of thing older MySQL /
            // MariaDB builds are picky about.
            + "case when sv.saved_report_id is null then 0 else 1 end as is_saved "
            + "from report r "
            + "join location l on r.location_id = l.location_id "
            // `user` is backticked because it is a keyword in several engines.
            + "left join `user` u on u.user_id = r.user_id "
            // Joined instead of correlated-subqueried: same result, one pass,
            // and the viewer id stays as the first two bind parameters.
            + "left join vote v on v.report_id = r.report_id and v.user_id = ? "
            + "left join saved_report sv on sv.report_id = r.report_id and sv.user_id = ? ";

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
            report.setCommentCount(0);
        }

        try {
            report.setUserVoteType(rs.getString("user_vote_type"));
        } catch (java.sql.SQLException e) {
            report.setUserVoteType(null);
        }

        try {
            report.setIsSaved(rs.getBoolean("is_saved"));
        } catch (java.sql.SQLException e) {
            report.setIsSaved(false);
        }

        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            report.setExpiresAt(expiresAt.toLocalDateTime());
        }
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            report.setCreatedAt(createdAt.toLocalDateTime());
        }
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

        report.setAuthorName(rs.getString("author_name"));
        report.setAuthorImageUrl(rs.getString("author_image_url"));

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
    public Optional<Report> findById(Long reportId, Long viewerUserId) {
        String sql = "select r.*, (select count(*) from comment c where c.report_id = r.report_id) as comment_count, "
                + "v.vote_type as user_vote_type, "
                + "case when sv.saved_report_id is null then 0 else 1 end as is_saved "
                + "from report r "
                + "left join vote v on v.report_id = r.report_id and v.user_id = ? "
                + "left join saved_report sv on sv.report_id = r.report_id and sv.user_id = ? "
                + "where r.report_id = ?";
        List<Report> results = jdbcTemplate.query(sql, ROW_MAPPER, viewer(viewerUserId), viewer(viewerUserId), reportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<Report> findByIdWithLocation(Long reportId, Long viewerUserId) {
        String sql = SELECT_WITH_LOCATION
                + "where r.report_id = ?";
        List<Report> results = jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),reportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Report> findAll(Long viewerUserId) {
        String sql = SELECT_WITH_LOCATION
                + "order by r.created_at desc limit " + DEFAULT_LIMIT;
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId));
    }

    @Override
    public List<Report> findNearby(Double latitude, Double longitude, Double radiusInMeters, String category, Long viewerUserId, int limit) {
        String sql = SELECT_WITH_LOCATION
                + "where " + NOT_EXPIRED
                + "and ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(?, ?)) <= ? ";

        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            sql += "and r.category = ? ";
            sql += "order by ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(?, ?)) asc limit " + limit;
            return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),longitude, latitude, radiusInMeters, category, longitude, latitude);
        } else {
            sql += "order by ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(?, ?)) asc limit " + limit;
            return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),longitude, latitude, radiusInMeters, longitude, latitude);
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
    public List<Report> findByUserId(Long userId, Long viewerUserId) {
        String sql = SELECT_WITH_LOCATION
                + "where r.user_id = ? "
                + "order by r.created_at desc limit " + DEFAULT_LIMIT;
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),userId);
    }

    @Override
    public List<Report> findSavedByUserId(Long userId, Long viewerUserId) {
        String sql = SELECT_WITH_LOCATION
                + "join saved_report sr on sr.report_id = r.report_id "
                + "where sr.user_id = ? "
                + "order by sr.saved_at desc limit " + DEFAULT_LIMIT;
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),userId);
    }

    @Override
    public List<Report> findWithinBoundingBox(double minLatitude, double maxLatitude,
                                               double minLongitude, double maxLongitude,
                                               String category, Long viewerUserId, int limit) {
        String sql = SELECT_WITH_LOCATION
                + "where " + NOT_EXPIRED
                + "and l.latitude >= ? and l.latitude <= ? and l.longitude >= ? and l.longitude <= ? ";
        
        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            sql += "and r.category = ? ";
            sql += "limit " + limit;
            return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),minLatitude, maxLatitude, minLongitude, maxLongitude, category);
        } else {
            sql += "limit " + limit;
            return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),minLatitude, maxLatitude, minLongitude, maxLongitude);
        }
    }
}
