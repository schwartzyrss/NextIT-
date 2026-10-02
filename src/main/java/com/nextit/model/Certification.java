package com.nextit.model;

public class Certification {
    private int certificationId;
    private int studentId;
    private String certificationName;
    private String issuingOrganization;
    private java.time.LocalDate issueDate;
    private java.time.LocalDate expirationDate;
    private String credentialUrl;

    public Certification() {}

    public int getCertificationId() { return certificationId; }
    public void setCertificationId(int certificationId) { this.certificationId = certificationId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getCertificationName() { return certificationName; }
    public void setCertificationName(String certificationName) { this.certificationName = certificationName; }

    public String getIssuingOrganization() { return issuingOrganization; }
    public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }

    public java.time.LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(java.time.LocalDate issueDate) { this.issueDate = issueDate; }

    public java.time.LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(java.time.LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public String getCredentialUrl() { return credentialUrl; }
    public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }
}

