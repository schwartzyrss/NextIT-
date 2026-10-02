package com.nextit.model;

public class PortfolioAccess {
    private int accessId;
    private int portfolioId;
    private int employerId;
    private String accessStatus;
    private java.time.LocalDateTime grantedAt;

    public PortfolioAccess() {}

    public int getAccessId() { return accessId; }
    public void setAccessId(int accessId) { this.accessId = accessId; }

    public int getPortfolioId() { return portfolioId; }
    public void setPortfolioId(int portfolioId) { this.portfolioId = portfolioId; }

    public int getEmployerId() { return employerId; }
    public void setEmployerId(int employerId) { this.employerId = employerId; }

    public String getAccessStatus() { return accessStatus; }
    public void setAccessStatus(String accessStatus) { this.accessStatus = accessStatus; }

    public java.time.LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(java.time.LocalDateTime grantedAt) { this.grantedAt = grantedAt; }
}

