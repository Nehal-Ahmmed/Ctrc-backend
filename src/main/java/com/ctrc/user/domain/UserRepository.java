package com.ctrc.user.domain;

import java.util.Optional;

public interface UserRepository {
    User insert(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    void update(User user);
    void updatePassword(Long userId, String newPassword);
}
