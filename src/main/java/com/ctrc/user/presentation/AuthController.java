package com.ctrc.user.presentation;

import com.ctrc.core.presentation.ApiResponse;
import com.ctrc.user.application.UserService;
import com.ctrc.user.application.dto.AuthResponse;
import com.ctrc.user.application.dto.LoginRequest;
import com.ctrc.user.application.dto.SignupRequest;
import com.ctrc.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        System.out.println("🟢 Received Signup request for email: " + request.getEmail());
        User user = userService.signup(request);
        // We set password to null in response to avoid exposing it to the client
        user.setPassword(null);
        String dummyToken = "dummy-token-for-" + user.getEmail();
        AuthResponse response = new AuthResponse(dummyToken, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("🟢 Received Login request for email: " + request.getEmail());
        User user = userService.login(request);
        // We set password to null in response to avoid exposing it to the client
        user.setPassword(null);
        String dummyToken = "dummy-token-for-" + user.getEmail();
        AuthResponse response = new AuthResponse(dummyToken, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
