package com.ctrc.report.application.dto;

import jakarta.validation.constraints.NotBlank;

public class VoteRequest {
    @NotBlank(message = "Vote type must be 'up' or 'down'")
    private String type; // 'up' or 'down'

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
