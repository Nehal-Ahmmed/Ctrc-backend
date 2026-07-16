package com.ctrc.user.application.dto;

import jakarta.validation.constraints.NotBlank;

public class UserUpdateDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String address;
    private String imageUrl;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
