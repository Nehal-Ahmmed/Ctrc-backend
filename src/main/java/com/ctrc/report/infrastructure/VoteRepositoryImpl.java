package com.ctrc.report.infrastructure;

import com.ctrc.report.domain.Vote;
import com.ctrc.report.domain.VoteRepository;
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
public class VoteRepositoryImpl implements VoteRepository {

    private final JdbcTemplate jdbcTemplate;

    public VoteRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Vote> ROW_MAPPER = (rs, rowNum) -> {
        Vote vote = new Vote();
        vote.setVoteId(rs.getLong("vote_id"));
        vote.setUserId(rs.getLong("user_id"));
        vote.setReportId(rs.getObject("report_id") != null ? rs.getLong("report_id") : null);
        vote.setSubReportId(rs.getObject("sub_report_id") != null ? rs.getLong("sub_report_id") : null);
        vote.setVoteType(rs.getString("vote_type"));
        vote.setVotedAt(rs.getTimestamp("voted_at"));
        try {
            vote.setUserName(rs.getString("user_name"));
            vote.setUserImageUrl(rs.getString("user_image_url"));
        } catch (java.sql.SQLException e) {

        }
        return vote;
    };

    @Override
    public Long insert(Vote vote) {
        String sql = "INSERT INTO vote (user_id, report_id, sub_report_id, vote_type) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, vote.getUserId());
            if (vote.getReportId() != null) {
                ps.setLong(2, vote.getReportId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            if (vote.getSubReportId() != null) {
                ps.setLong(3, vote.getSubReportId());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setString(4, vote.getVoteType());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : 0L;
    }

    @Override
    public Optional<Vote> findByUserAndReport(Long userId, Long reportId) {
        String sql = "SELECT * FROM vote WHERE user_id = ? AND report_id = ?";
        List<Vote> results = jdbcTemplate.query(sql, ROW_MAPPER, userId, reportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<Vote> findByUserAndSubReport(Long userId, Long subReportId) {
        String sql = "SELECT * FROM vote WHERE user_id = ? AND sub_report_id = ?";
        List<Vote> results = jdbcTemplate.query(sql, ROW_MAPPER, userId, subReportId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void delete(Long voteId) {
        String sql = "DELETE FROM vote WHERE vote_id = ?";
        jdbcTemplate.update(sql, voteId);
    }

    @Override
    public List<Vote> findByReportId(Long reportId) {
        String sql = "SELECT v.*, u.name AS user_name, u.image_url AS user_image_url "
                + "FROM vote v "
                + "JOIN user u ON v.user_id = u.user_id "
                + "WHERE v.report_id = ? "
                + "ORDER BY v.voted_at DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, reportId);
    }

    @Override
    public List<Vote> findBySubReportId(Long subReportId) {
        String sql = "SELECT v.*, u.name AS user_name, u.image_url AS user_image_url "
                + "FROM vote v "
                + "JOIN user u ON v.user_id = u.user_id "
                + "WHERE v.sub_report_id = ? "
                + "ORDER BY v.voted_at DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, subReportId);
    }
}
