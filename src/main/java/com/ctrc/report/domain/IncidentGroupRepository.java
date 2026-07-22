package com.ctrc.report.domain;

public interface IncidentGroupRepository {
    Long insert(Long reportId, String description);
}
