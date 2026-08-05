package com.ctrc.report.domain;

import java.util.List;
import java.util.Optional;

public interface VoteRepository {
    Long insert(Vote vote);
    Optional<Vote> findByUserAndReport(Long userId, Long reportId);

    Optional<Vote> findByUserAndSubReport(Long userId, Long subReportId);

    void delete(Long voteId);

    List<Vote> findByReportId(Long reportId);

    List<Vote> findBySubReportId(Long subReportId);
}
