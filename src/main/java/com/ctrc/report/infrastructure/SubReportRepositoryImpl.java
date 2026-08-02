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
        String sql = "insert into sub_report (user_id, report_id, location_id, description, "
                   + "evidence_type, category, dist_from_parent, upvote_count, downvote_count) "
                   + "values (?, ?, ?, ?, ?, ?, "
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
            ps.setString(5, subReport.getEvidenceType() != null ? subReport.getEvidenceType() : "heard");
            ps.setString(6, subReport.getCategory());
            ps.setLong(7, subReport.getLocationId());
            ps.setLong(8, subReport.getReportId());
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

    /**
     * Adds the location, the author and the comment tally to the base row, so
     * one query produces everything an update needs on screen.
     */
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

        return subReport;
    };

    @Override
    public List<SubReport> findByReportId(Long reportId) {
        String sql = "select sr.*, l.longitude, l.latitude, l.address as loc_address, l.city, "
                   + "u.name as author_name, u.image_url as author_image_url, "
                   // Comments tie to either a report or a sub-report through the
                   // dual-FK comment table; here only the sub-report side counts.
                   + "(select count(*) from comment c where c.sub_report_id = sr.sub_report_id) as comment_count "
                   + "from sub_report sr "
                   + "join location l on sr.location_id = l.location_id "
                   // `user` is backticked because it is a keyword in several engines.
                   + "left join `user` u on u.user_id = sr.user_id "
                   + "where sr.report_id = ? "
                   // Oldest first: an incident thread reads as it unfolded.
                   + "order by sr.created_at asc";
        return jdbcTemplate.query(sql, ROW_MAPPER_WITH_LOCATION, reportId);
    }
}
