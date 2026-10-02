package com.nextit.model;

public class Achievement {
    private int achievementId;
    private int studentId;
    private String title;
    private String description;
    private String organization;
    private java.time.LocalDate achievementDate;
    private String verificationUrl;

    public Achievement() {}

    public int getAchievementId() { return achievementId; }
    public void setAchievementId(int achievementId) { this.achievementId = achievementId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public java.time.LocalDate getAchievementDate() { return achievementDate; }
    public void setAchievementDate(java.time.LocalDate achievementDate) { this.achievementDate = achievementDate; }

    public String getVerificationUrl() { return verificationUrl; }
    public void setVerificationUrl(String verificationUrl) { this.verificationUrl = verificationUrl; }
}

