package com.ctrc.report.domain;

import java.util.List;

public interface CommentRepository {
    Long insert(Comment comment);

    List<Comment> findByReportId(Long reportId);

    List<Comment> findBySubReportId(Long subReportId);

    List<Comment> findByReportId(Long reportId, Long viewerUserId);

    List<Comment> findBySubReportId(Long subReportId, Long viewerUserId);

    java.util.Optional<Comment> findById(Long commentId);
}
