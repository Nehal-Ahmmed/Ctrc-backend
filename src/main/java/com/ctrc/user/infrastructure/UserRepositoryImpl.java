package com.ctrc.user.infrastructure;

import com.ctrc.user.domain.User;
import com.ctrc.user.domain.UserRepository;
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
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setAddress(rs.getString("address"));
        user.setImageUrl(rs.getString("image_url"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    };

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User insert(User user) {
        String sql = "INSERT INTO user (name, password, email, address, image_url) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getAddress());
            ps.setString(5, user.getImageUrl());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            user.setId(key.longValue());
        }
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, rowMapper, email);
        return users.stream().findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM user WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, rowMapper, id);
        return users.stream().findFirst();
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE user SET name = ?, address = ?, image_url = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getName(), user.getAddress(), user.getImageUrl(), user.getId());
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        String sql = "UPDATE user SET password = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, newPassword, userId);
    }
}
