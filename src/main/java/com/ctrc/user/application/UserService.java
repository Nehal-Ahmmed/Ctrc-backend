package com.ctrc.user.application;

import com.ctrc.core.domain.exceptions.ConflictException;
import com.ctrc.core.domain.exceptions.ResourceNotFoundException;
import com.ctrc.core.domain.exceptions.ValidationException;
import com.ctrc.user.application.dto.ChangePasswordRequest;
import com.ctrc.user.application.dto.LoginRequest;
import com.ctrc.user.application.dto.SignupRequest;
import com.ctrc.user.application.dto.UserUpdateDto;
import com.ctrc.user.domain.User;
import com.ctrc.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final com.ctrc.core.services.CloudinaryService cloudinaryService;

    public UserService(UserRepository userRepository, com.ctrc.core.services.CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
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

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User updateProfile(String email, UserUpdateDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(dto.getName());
        user.setAddress(dto.getAddress());
        user.setImageUrl(dto.getImageUrl());

        userRepository.update(user);
        return user;
    }

    @Transactional
    public User changePassword(String email, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("New passwords do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Plain text comparison, matching signup/login above
        if (!user.getPassword().equals(request.getCurrentPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new ValidationException("New password must be different from the current one");
        }

        userRepository.updatePassword(user.getId(), request.getNewPassword());
        user.setPassword(request.getNewPassword());
        return user;
    }

    @Transactional
    public User updateAvatar(String email, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String imageUrl = cloudinaryService.uploadImage(file);
        user.setImageUrl(imageUrl);
        userRepository.update(user);
        return user;
    }
}
