package com.ctrc.report.domain;

import java.util.Optional;

public interface VoteRepository {
    Long insert(Vote vote);
    Optional<Vote> findByUserAndReport(Long userId, Long reportId);
    void delete(Long voteId);
    java.util.List<Vote> findByReportId(Long reportId);
}
