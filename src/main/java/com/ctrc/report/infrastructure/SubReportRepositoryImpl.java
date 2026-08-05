package com.ctrc.report.infrastructure;

import com.ctrc.location.domain.Location;
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

        try {
            sr.setEvidenceType(rs.getString("evidence_type"));
            sr.setCategory(rs.getString("category"));
            sr.setImageUrl(rs.getString("image_url"));
        } catch (java.sql.SQLException e) {
            sr.setEvidenceType("heard");
        }

        sr.setDistFromParent(rs.getDouble("dist_from_parent"));
        sr.setUpvoteCount(rs.getInt("upvote_count"));
        sr.setDownvoteCount(rs.getInt("downvote_count"));
        sr.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return sr;
    };

    @Override
    public Long insert(SubReport subReport) {
        // Where the parent was pinned, looked up in its own statement rather
        // than as a sub-select inside the insert below.
        //
        // trg_subreport_insert fires after this insert and updates `report`,
        // and MySQL refuses to let a trigger write to a table the invoking
        // statement is already reading (error 1442). Reading `report` here
        // instead keeps the two apart. Filing an update with "I saw it myself"
        // failed outright until this was split.
        List<Long> parentLocation = jdbcTemplate.queryForList(
                "select location_id from report where report_id = ?",
                Long.class, subReport.getReportId());

        if (parentLocation.isEmpty()) {
            throw new com.ctrc.core.domain.exceptions.ResourceNotFoundException(
                    "report not found with id " + subReport.getReportId());
        }
        final Long parentLocationId = parentLocation.get(0);

        String sql = "insert into sub_report (user_id, report_id, location_id, description, "
                   + "evidence_type, category, image_url, dist_from_parent, upvote_count, downvote_count) "
                   + "values (?, ?, ?, ?, ?, ?, ?, "
                   + "(select ST_Distance_Sphere(POINT(l1.longitude, l1.latitude), POINT(l2.longitude, l2.latitude)) "
                   + "from location l1 cross join location l2 "
                   + "where l1.location_id = ? and l2.location_id = ?), "
                   + "0, 0)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, subReport.getUserId());
            ps.setLong(2, subReport.getReportId());
            ps.setLong(3, subReport.getLocationId());
            ps.setString(4, subReport.getDescription());
            ps.setString(5, subReport.getEvidenceType() != null ? subReport.getEvidenceType() : "heard");
            ps.setString(6, subReport.getCategory());
            ps.setString(7, subReport.getImageUrl());
            ps.setLong(8, subReport.getLocationId());
            ps.setLong(9, parentLocationId);
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

    private static final RowMapper<SubReport> ROW_MAPPER_WITH_LOCATION = (rs, rowNum) -> {
        SubReport subReport = ROW_MAPPER.mapRow(rs, rowNum);

        Location location = new Location();
        location.setLocationId(rs.getLong("location_id"));
        location.setLongitude(rs.getDouble("longitude"));
        location.setLatitude(rs.getDouble("latitude"));
        location.setAddress(rs.getString("loc_address"));
        location.setCity(rs.getString("city"));
        subReport.setLocation(location);

        subReport.setAuthorName(rs.getString("author_name"));
        subReport.setAuthorImageUrl(rs.getString("author_image_url"));
        subReport.setCommentCount(rs.getInt("comment_count"));

        try {
            subReport.setUserVoteType(rs.getString("user_vote_type"));
        } catch (java.sql.SQLException e) {
            subReport.setUserVoteType(null);
        }

        try {
            subReport.setParentTitle(rs.getString("parent_title"));
        } catch (java.sql.SQLException e) {
            subReport.setParentTitle(null);
        }

        return subReport;
    };

    @Override
    public Optional<SubReport> findByIdWithLocation(Long subReportId, Long currentUserId) {
        String sql = "select sr.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
                   + "u.name as author_name, u.image_url as author_image_url, "
                   + "r.title as parent_title, "
                   + "(select count(*) from comment c where c.sub_report_id = sr.sub_report_id) as comment_count, "
                   + "v.vote_type as user_vote_type "
                   + "from sub_report sr "
                   + "join location l on sr.location_id = l.location_id "
                   + "join report r on sr.report_id = r.report_id "
                   + "left join `user` u on u.user_id = sr.user_id "
                   + "left join vote v on v.sub_report_id = sr.sub_report_id and v.user_id = ? "
                   + "where sr.sub_report_id = ?";
        List<SubReport> results = jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, currentUserId, subReportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<SubReport> findByReportId(Long reportId) {
        String sql = "select sr.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
                   + "u.name as author_name, u.image_url as author_image_url, "
                   + "r.title as parent_title, "
                   + "(select count(*) from comment c where c.sub_report_id = sr.sub_report_id) as comment_count, "
                   + "null as user_vote_type "
                   + "from sub_report sr "
                   + "join location l on sr.location_id = l.location_id "
                   + "join report r on sr.report_id = r.report_id "
                   + "left join `user` u on u.user_id = sr.user_id "
                   + "where sr.report_id = ? "
                   + "order by sr.created_at asc";
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, reportId);
    }
}
