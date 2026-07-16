package com.ctrc.user.application;

import com.ctrc.core.domain.exceptions.ConflictException;
import com.ctrc.core.domain.exceptions.ResourceNotFoundException;
import com.ctrc.core.domain.exceptions.ValidationException;
import com.ctrc.user.application.dto.LoginRequest;
import com.ctrc.user.application.dto.SignupRequest;
import com.ctrc.user.domain.User;
import com.ctrc.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User signup(SignupRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already in use");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Plain text per instructions
        user.setAddress(request.getAddress());
        user.setImageUrl(request.getImageUrl());

        return userRepository.insert(user);
    }

    public User login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        // Plain text comparison per instructions
        if (!user.getPassword().equals(request.getPassword())) {
            throw new ResourceNotFoundException("Invalid email or password"); // Vague error for security
        }

        return user;
    }
}
