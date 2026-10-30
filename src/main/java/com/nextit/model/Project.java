package com.nextit.model;

public class Project {
    private int projectId;
    private Integer studentId;
    private Integer subjectId;
    private String projectTitle;
    private String projectType;
    private String description;
    private String technologiesUsed;
    private String repositoryUrl;
    private String demoUrl;
    private java.time.LocalDate projectDate;
    private String status;

    public Project() {}

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechnologiesUsed() { return technologiesUsed; }
    public void setTechnologiesUsed(String technologiesUsed) { this.technologiesUsed = technologiesUsed; }

    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }

    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }

    public java.time.LocalDate getProjectDate() { return projectDate; }
    public void setProjectDate(java.time.LocalDate projectDate) { this.projectDate = projectDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

