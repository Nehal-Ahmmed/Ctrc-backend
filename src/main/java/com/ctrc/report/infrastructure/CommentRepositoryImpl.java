package com.ctrc.report.infrastructure;

import com.ctrc.report.domain.Comment;
import com.ctrc.report.domain.CommentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Comment> ROW_MAPPER = (rs, rowNum) -> {
        Comment comment = new Comment();
        comment.setCommentId(rs.getLong("comment_id"));
        comment.setUserId(rs.getLong("user_id"));
        comment.setReportId(rs.getObject("report_id") != null ? rs.getLong("report_id") : null);
        comment.setSubReportId(rs.getObject("sub_report_id") != null ? rs.getLong("sub_report_id") : null);
        comment.setContent(rs.getString("content"));
        comment.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            comment.setUserName(rs.getString("user_name"));
            comment.setUserImageUrl(rs.getString("user_image_url"));
        } catch (java.sql.SQLException e) {
            // ignore if not present in query result
        }
        return comment;
    };

    @Override
    public Long insert(Comment comment) {
        String sql = "INSERT INTO comment (user_id, report_id, sub_report_id, content) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, comment.getUserId());
            if (comment.getReportId() != null) {
                ps.setLong(2, comment.getReportId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            if (comment.getSubReportId() != null) {
                ps.setLong(3, comment.getSubReportId());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setString(4, comment.getContent());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : 0L;
    }

    @Override
    public List<Comment> findByReportId(Long reportId) {
        String sql = "SELECT c.*, u.name AS user_name, u.image_url AS user_image_url "
                + "FROM comment c "
                + "JOIN user u ON c.user_id = u.user_id "
                + "WHERE c.report_id = ? "
                + "ORDER BY c.created_at ASC";
        return jdbcTemplate.query(sql, ROW_MAPPER, reportId);
    }
}
