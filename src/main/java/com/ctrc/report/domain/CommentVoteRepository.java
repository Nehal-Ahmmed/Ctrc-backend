package com.ctrc.report.domain;

import java.util.Optional;

public interface CommentVoteRepository {

    Optional<String> findVoteType(Long userId, Long commentId);

    void insert(Long userId, Long commentId, String voteType);

    void delete(Long userId, Long commentId);
}
