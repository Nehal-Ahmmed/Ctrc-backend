package com.ctrc.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// request body for changing the signed-in user's password
public class ChangePasswordRequest {

    @NotBlank(message = "is required")
    private String currentPassword;

    @NotBlank(message = "is required")
    @Size(min = 6, message = "must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "is required")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
