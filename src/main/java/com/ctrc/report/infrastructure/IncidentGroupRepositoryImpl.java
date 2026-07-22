package com.ctrc.report.infrastructure;

import com.ctrc.report.domain.IncidentGroupRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class IncidentGroupRepositoryImpl implements IncidentGroupRepository {

    private final JdbcTemplate jdbcTemplate;

    public IncidentGroupRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long insert(Long reportId, String description) {
        String sql = "insert into incident_group (report_id, description) values (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, reportId);
            ps.setString(2, description);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
