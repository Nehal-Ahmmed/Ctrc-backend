package com.ctrc.report.application.dto;

import jakarta.validation.constraints.NotBlank;

// request body for editing a report you already filed
// only the text side is editable, the location a report was filed at stays put
public class UpdateReportRequest {

    @NotBlank(message = "is required")
    private String title;

    private String description;

    @NotBlank(message = "is required")
    private String category;

    // 'seen', 'heard' or 'guessed'
    private String evidenceType;

    // null clears the photo
    private String imageUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
