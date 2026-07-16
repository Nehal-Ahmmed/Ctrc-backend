package com.ctrc.user.presentation;

import com.ctrc.core.presentation.ApiResponse;
import com.ctrc.user.application.UserService;
import com.ctrc.user.application.dto.UserUpdateDto;
import com.ctrc.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private String extractEmailFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return token.replace("dummy-token-for-", "");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String email = extractEmailFromHeader(authHeader);
            User user = userService.getUserByEmail(email);
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserUpdateDto dto) {
        try {
            String email = extractEmailFromHeader(authHeader);
            User user = userService.updateProfile(email, dto);
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
