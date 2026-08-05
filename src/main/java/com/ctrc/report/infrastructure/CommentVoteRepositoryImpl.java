package com.ctrc.report.infrastructure;

import com.ctrc.report.domain.CommentVoteRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommentVoteRepositoryImpl implements CommentVoteRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommentVoteRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<String> findVoteType(Long userId, Long commentId) {
        List<String> types = jdbcTemplate.queryForList(
                "select vote_type from comment_vote where user_id = ? and comment_id = ?",
                String.class, userId, commentId);
        return types.isEmpty() ? Optional.empty() : Optional.of(types.get(0));
    }

    @Override
    public void insert(Long userId, Long commentId, String voteType) {
        
        jdbcTemplate.update(
                "insert into comment_vote (user_id, comment_id, vote_type) values (?, ?, ?)",
                userId, commentId, voteType);
    }

    @Override
    public void delete(Long userId, Long commentId) {
        jdbcTemplate.update(
                "delete from comment_vote where user_id = ? and comment_id = ?",
                userId, commentId);
    }
}
