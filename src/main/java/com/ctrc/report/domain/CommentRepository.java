package com.ctrc.report.domain;

import java.util.List;

public interface CommentRepository {
    Long insert(Comment comment);
    List<Comment> findByReportId(Long reportId);
}
