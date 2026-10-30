package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.repository.*;
import com.nextit.util.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CareerService {
    private final JdbcJobOpportunityRepository oppRepo = new JdbcJobOpportunityRepository();
    private final JdbcApplicationRepository appRepo = new JdbcApplicationRepository();
    private final JdbcEmployerRepository employerRepo = new JdbcEmployerRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();

    public List<JobOpportunity> openOpportunities() { return oppRepo.findAvailable(); }
    public List<JobOpportunity> allOpportunities() { return oppRepo.findAll(); }
    public List<JobOpportunity> opportunitiesForEmployer(int employerId) { return oppRepo.findByEmployerId(employerId); }
    public JobOpportunity opportunityById(int id) {
        return oppRepo.findById(id)
                .orElseThrow(() -> new AppException("Opportunity not found."));
    }

    public int employerIdForUser(int userId) {
        return employerRepo.findByUserId(userId).map(Employer::getEmployerId)
            .orElseThrow(() -> new AppException("Employer profile not found."));
    }

    public void addOpportunity(JobOpportunity o, int actingEmployerId) {
        if (employerRepo.findById(actingEmployerId).isEmpty()) {
            throw new AppException("Employer profile not found.");
        }
        o.setEmployerId(actingEmployerId);
        validate(o);
        o.setStatus(o.getStatus() == null || o.getStatus().isBlank() ? "Open" : o.getStatus());
        oppRepo.save(o);
    }

    public void updateOpportunity(JobOpportunity o, int actingEmployerId) {
        requireOwned(o.getOpportunityId(), actingEmployerId);
        validate(o);
        o.setStatus(o.getStatus() == null || o.getStatus().isBlank() ? "Open" : o.getStatus());
        oppRepo.update(o);
    }

    public void deleteOpportunity(int id, int actingEmployerId) {
        requireOwned(id, actingEmployerId);
        oppRepo.delete(id);
    }

    private void requireOwned(int opportunityId, int actingEmployerId) {
        JobOpportunity opportunity = opportunityById(opportunityId);
        if (opportunity.getEmployerId() != actingEmployerId) {
            throw new AppException("You can only manage your own opportunities.");
        }
    }

    public List<Application> applicationsByStudent(int studentId) { return appRepo.findByStudentId(studentId); }
    public List<Application> applicationsForOpportunity(int oppId) { return appRepo.findByOpportunityId(oppId); }

    public List<Application> applicationsForEmployer(int employerId) {
        return opportunitiesForEmployer(employerId).stream()
                .flatMap(o -> applicationsForOpportunity(o.getOpportunityId()).stream())
                .toList();
    }

    public boolean hasApplied(int studentId, int opportunityId) {
        return appRepo.findByStudentAndOpportunity(studentId, opportunityId).isPresent();
    }

    public void apply(Application a) {
        if (a.getStudentId() <= 0 || studentRepo.findById(a.getStudentId()).isEmpty()) {
            throw new AppException("A valid student is required to apply.");
        }
        JobOpportunity opportunity = opportunityById(a.getOpportunityId());
        if (!"Open".equalsIgnoreCase(opportunity.getStatus())) {
            throw new AppException("This opportunity is no longer accepting applications.");
        }
        if (opportunity.getDeadline() != null && opportunity.getDeadline().isBefore(LocalDate.now())) {
            throw new AppException("The application deadline has passed.");
        }
        if (hasApplied(a.getStudentId(), a.getOpportunityId())) {
            throw new AppException("You have already applied to this opportunity.");
        }
        a.setStatus("Pending");
        if (a.getApplicationDate() == null) a.setApplicationDate(LocalDateTime.now());
        appRepo.save(a);
    }

    public void updateApplicationStatus(int employerId, Application a, String status, String remarks) {
        if (a.getApplicationId() <= 0) throw new AppException("Select an application to review.");
        Application stored = appRepo.findById(a.getApplicationId())
                .orElseThrow(() -> new AppException("Application not found."));
        JobOpportunity opportunity = opportunityById(stored.getOpportunityId());
        if (opportunity.getEmployerId() != employerId) {
            throw new AppException("You can only manage applications for your own opportunities.");
        }
        if (status == null || !List.of("Pending", "Reviewed", "Shortlisted", "Rejected", "Accepted").contains(status)) {
            throw new AppException("Select a valid application status.");
        }
        stored.setStatus(status);
        stored.setRemarks(remarks);
        appRepo.update(stored);
    }

    private void validate(JobOpportunity o) {
        Validator.notBlank(o.getTitle(), "Title");
        if (!"Internship".equalsIgnoreCase(o.getOpportunityType())
                && !"Employment".equalsIgnoreCase(o.getOpportunityType())) {
            throw new AppException("Opportunity type must be Internship or Employment.");
        }
        Validator.notBlank(o.getDescription(), "Description");
        boolean inactive = "Closed".equalsIgnoreCase(o.getStatus()) || "Archived".equalsIgnoreCase(o.getStatus());
        if (!inactive && o.getDeadline() != null && o.getDeadline().isBefore(LocalDate.now())) {
            throw new AppException("The deadline cannot be in the past while the opportunity is open.");
        }
        if (o.getStatus() != null && !List.of("Open", "Closed", "Archived").contains(o.getStatus())) {
            throw new AppException("Status must be Open, Closed, or Archived.");
        }
    }
}
