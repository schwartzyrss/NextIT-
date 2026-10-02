package com.nextit.model;

public class Application {
    private int applicationId;
    private int opportunityId;
    private int studentId;
    private java.time.LocalDateTime applicationDate;
    private String status;
    private String remarks;

    public Application() {}

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getOpportunityId() { return opportunityId; }
    public void setOpportunityId(int opportunityId) { this.opportunityId = opportunityId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public java.time.LocalDateTime getApplicationDate() { return applicationDate; }
    public void setApplicationDate(java.time.LocalDateTime applicationDate) { this.applicationDate = applicationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

