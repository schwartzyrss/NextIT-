package com.nextit.model;

public class ProjectMember {
    private int projectMemberId;
    private int projectId;
    private int studentId;
    private String role;
    private String contribution;

    public ProjectMember() {}

    public int getProjectMemberId() { return projectMemberId; }
    public void setProjectMemberId(int projectMemberId) { this.projectMemberId = projectMemberId; }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContribution() { return contribution; }
    public void setContribution(String contribution) { this.contribution = contribution; }
}

