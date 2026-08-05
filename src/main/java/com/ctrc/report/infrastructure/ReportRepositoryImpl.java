package com.ctrc.report.infrastructure;

import com.ctrc.report.domain.Report;
import com.ctrc.report.domain.ReportFeedFilter;
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

    private static final long ANONYMOUS_VIEWER = -1L;

    private static long viewer(Long viewerUserId) {
        return viewerUserId != null ? viewerUserId : ANONYMOUS_VIEWER;
    }

    private static final String NOT_EXPIRED =
            "(r.expires_at is null or r.expires_at > now()) ";

    private static final String SELECT_WITH_LOCATION =
            "select r.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
            + "u.name as author_name, u.image_url as author_image_url, "
            + "(select count(*) from comment c where c.report_id = r.report_id) as comment_count, "

            + "(select count(*) from sub_report sr2 where sr2.report_id = r.report_id) as sub_report_count, "
            + "v.vote_type as user_vote_type, "

            + "case when sv.saved_report_id is null then 0 else 1 end as is_saved "
            + "from report r "
            + "join location l on r.location_id = l.location_id "

            + "left join `user` u on u.user_id = r.user_id "

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
            report.setEvidenceType(rs.getString("evidence_type"));
        } catch (java.sql.SQLException e) {
            report.setEvidenceType("seen");
        }

        try {
            report.setStatus(rs.getString("status"));
        } catch (java.sql.SQLException e) {
            report.setStatus("unverified");
        }

        try {
            report.setImageUrl(rs.getString("image_url"));
        } catch (java.sql.SQLException e) {
            report.setImageUrl(null);
        }

        try {
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                report.setUpdatedAt(updatedAt.toLocalDateTime());
            }
        } catch (java.sql.SQLException e) {
            report.setUpdatedAt(null);
        }

        try {
            int commentCount = rs.getInt("comment_count");
            report.setCommentCount(commentCount);
        } catch (java.sql.SQLException e) {
            report.setCommentCount(0);
        }

        try {
            report.setSubReportCount(rs.getInt("sub_report_count"));
        } catch (java.sql.SQLException e) {
            report.setSubReportCount(0);
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
                + "evidence_type, image_url, upvote_count, downvote_count, expires_at) "
                + "values (?, ?, ?, ?, ?, ?, ?, 0, 0, coalesce(?, now() + interval 3 hour))";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, report.getUserId());
            ps.setLong(2, report.getLocationId());
            ps.setString(3, report.getTitle());
            ps.setString(4, report.getDescription());
            ps.setString(5, report.getCategory());
            ps.setString(6, report.getEvidenceType() != null ? report.getEvidenceType() : "seen");
            ps.setString(7, report.getImageUrl());
            if (report.getExpiresAt() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(report.getExpiresAt()));
            } else {
                ps.setTimestamp(8, null);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : 0L;
    }

    @Override
    public int update(Report report) {
        String sql = "update report "
                + "set title = ?, description = ?, category = ?, evidence_type = ?, "
                + "image_url = ?, updated_at = now() "
                + "where report_id = ? and user_id = ?";
        return jdbcTemplate.update(sql,
                report.getTitle(),
                report.getDescription(),
                report.getCategory(),
                report.getEvidenceType() != null ? report.getEvidenceType() : "seen",
                report.getImageUrl(),
                report.getReportId(),
                report.getUserId());
    }

    @Override
    public int softDelete(Long reportId, Long userId) {
        String sql = "UPDATE report SET deleted_at = now() WHERE report_id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, reportId, userId);
    }

    @Override
    public Optional<Report> findById(Long reportId, Long viewerUserId) {
        String sql = "select r.*, (select count(*) from comment c where c.report_id = r.report_id) as comment_count, "
                + "v.vote_type as user_vote_type, "
                + "case when sv.saved_report_id is null then 0 else 1 end as is_saved "
                + "from report r "
                + "left join vote v on v.report_id = r.report_id and v.user_id = ? "
                + "left join saved_report sv on sv.report_id = r.report_id and sv.user_id = ? "
                + "where r.report_id = ? and r.deleted_at is null";
        List<Report> results = jdbcTemplate.query(sql, ROW_MAPPER, viewer(viewerUserId), viewer(viewerUserId), reportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<Report> findByIdWithLocation(Long reportId, Long viewerUserId) {
        String sql = SELECT_WITH_LOCATION
                + "where r.report_id = ? and r.deleted_at is null";
        List<Report> results = jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),reportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Report> findAll(Long viewerUserId) {
        String sql = SELECT_WITH_LOCATION
                + "where r.deleted_at is null "
                + "order by r.created_at desc limit " + DEFAULT_LIMIT;
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId));
    }

    @Override
    public List<Report> findNearby(Double latitude, Double longitude, Double radiusInMeters,
                                   String category, ReportFeedFilter filter, Long viewerUserId, int limit) {
        ReportFeedFilter feedFilter = filter != null ? filter : ReportFeedFilter.DEFAULT;

        List<Object> params = new java.util.ArrayList<>();
        params.add(viewer(viewerUserId));
        params.add(viewer(viewerUserId));

        StringBuilder sql = new StringBuilder(SELECT_WITH_LOCATION)
                .append("where ").append(NOT_EXPIRED)
                .append("and r.deleted_at is null ")
                .append("and ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(?, ?)) <= ? ");
        params.add(longitude);
        params.add(latitude);
        params.add(radiusInMeters);

        appendCategory(sql, params, category);
        appendFeedFilters(sql, params, feedFilter);
        appendFeedOrder(sql, params, feedFilter, longitude, latitude);
        sql.append("limit ").append(limit);

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER_WITH_LOCATION, params.toArray());
    }

    private static void appendCategory(StringBuilder sql, List<Object> params, String category) {
        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            sql.append("and r.category = ? ");
            params.add(category);
        }
    }

    private static void appendFeedFilters(StringBuilder sql, List<Object> params, ReportFeedFilter filter) {
        if (filter.getStatus() != null) {
            sql.append("and r.status = ? ");
            params.add(filter.getStatus());
        }
        if (filter.getEvidenceType() != null) {
            sql.append("and r.evidence_type = ? ");
            params.add(filter.getEvidenceType());
        }
        if (filter.getWithinHours() != null) {
            sql.append("and r.created_at >= now() - interval ? hour ");
            params.add(filter.getWithinHours());
        }
        if (filter.isWithPhotoOnly()) {
            sql.append("and r.image_url is not null and r.image_url <> '' ");
        }
    }

    private static void appendFeedOrder(StringBuilder sql, List<Object> params,
                                        ReportFeedFilter filter, Double longitude, Double latitude) {
        switch (filter.getSort()) {
            case ReportFeedFilter.SORT_NEWEST ->
                    sql.append("order by r.created_at desc ");
            case ReportFeedFilter.SORT_OLDEST ->
                    sql.append("order by r.created_at asc ");
            case ReportFeedFilter.SORT_TOP ->
                    sql.append("order by (r.upvote_count - r.downvote_count) desc, r.created_at desc ");
            case ReportFeedFilter.SORT_DISCUSSED ->
                    sql.append("order by comment_count desc, r.created_at desc ");
            case ReportFeedFilter.SORT_CONFIRMED ->
                    sql.append("order by sub_report_count desc, r.created_at desc ");
            default -> {
                sql.append("order by ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(?, ?)) asc ");
                params.add(longitude);
                params.add(latitude);
            }
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
                + "where r.user_id = ? and r.deleted_at is null "
                + "order by r.created_at desc limit " + DEFAULT_LIMIT;
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),userId);
    }

    @Override
    public List<Report> findSavedByUserId(Long userId, Long viewerUserId) {
        String sql = SELECT_WITH_LOCATION
                + "join saved_report sr on sr.report_id = r.report_id "
                + "where sr.user_id = ? and r.deleted_at is null "
                + "order by sr.saved_at desc limit " + DEFAULT_LIMIT;
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, viewer(viewerUserId), viewer(viewerUserId),userId);
    }

    @Override
    public List<Report> findWithinBoundingBox(double minLatitude, double maxLatitude,
                                               double minLongitude, double maxLongitude,
                                               String category, Long viewerUserId, int limit) {
        String sql = SELECT_WITH_LOCATION
                + "where " + NOT_EXPIRED
                + "and r.deleted_at is null "
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
