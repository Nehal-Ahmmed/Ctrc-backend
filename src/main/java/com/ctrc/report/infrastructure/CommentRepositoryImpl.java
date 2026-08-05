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
import java.util.Optional;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommentRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final long ANONYMOUS_VIEWER = -1L;

    private static long viewer(Long viewerUserId) {
        return viewerUserId != null ? viewerUserId : ANONYMOUS_VIEWER;
    }

    private static final String SELECT_WITH_AUTHOR =
            "select c.*, u.name as user_name, u.image_url as user_image_url, "
            + "cv.vote_type as user_vote_type "
            + "from comment c "
            + "join `user` u on c.user_id = u.user_id "
            + "left join comment_vote cv on cv.comment_id = c.comment_id and cv.user_id = ? ";

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

        }
        
        try {
            comment.setUpvoteCount(rs.getInt("upvote_count"));
        } catch (java.sql.SQLException e) {
            comment.setUpvoteCount(0);
        }
        try {
            comment.setDownvoteCount(rs.getInt("downvote_count"));
        } catch (java.sql.SQLException e) {
            comment.setDownvoteCount(0);
        }
        try {
            comment.setUserVoteType(rs.getString("user_vote_type"));
        } catch (java.sql.SQLException e) {
            comment.setUserVoteType(null);
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
        return findByReportId(reportId, null);
    }

    @Override
    public List<Comment> findBySubReportId(Long subReportId) {
        return findBySubReportId(subReportId, null);
    }

    @Override
    public List<Comment> findByReportId(Long reportId, Long viewerUserId) {
        String sql = SELECT_WITH_AUTHOR
                + "where c.report_id = ? "
                + "order by c.created_at asc";
        return jdbcTemplate.query(sql, ROW_MAPPER, viewer(viewerUserId), reportId);
    }

    @Override
    public List<Comment> findBySubReportId(Long subReportId, Long viewerUserId) {
        String sql = SELECT_WITH_AUTHOR
                + "where c.sub_report_id = ? "
                + "order by c.created_at asc";
        return jdbcTemplate.query(sql, ROW_MAPPER, viewer(viewerUserId), subReportId);
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        List<Comment> results = jdbcTemplate.query(
                "select c.* from comment c where c.comment_id = ?", ROW_MAPPER, commentId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
