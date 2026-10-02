package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.repository.*;

import java.util.List;

public class CareerService {
    private final JdbcJobOpportunityRepository oppRepo = new JdbcJobOpportunityRepository();
    private final JdbcApplicationRepository appRepo = new JdbcApplicationRepository();
    private final JdbcEmployerRepository employerRepo = new JdbcEmployerRepository();

    public List<JobOpportunity> openOpportunities() { return oppRepo.findAvailable(); }
    public List<JobOpportunity> opportunitiesForEmployer(int employerId) { return oppRepo.findByEmployerId(employerId); }
    public int employerIdForUser(int userId) {
        return employerRepo.findByUserId(userId).map(Employer::getEmployerId)
            .orElseThrow(() -> new AppException("Employer profile not found."));
    }

    public void addOpportunity(JobOpportunity o) {
        if (o.getTitle() == null || o.getTitle().isBlank()) throw new AppException("Title is required.");
        if (o.getOpportunityType() == null || o.getOpportunityType().isBlank()) throw new AppException("Type is required.");
        oppRepo.save(o);
    }

    public void updateOpportunity(JobOpportunity o) { oppRepo.update(o); }
    public void deleteOpportunity(int id) { oppRepo.delete(id); }

    public List<Application> applicationsByStudent(int studentId) { return appRepo.findByStudentId(studentId); }
    public List<Application> applicationsForOpportunity(int oppId) { return appRepo.findByOpportunityId(oppId); }

    public void apply(Application a) {
        oppRepo.findAll().stream().filter(o -> o.getOpportunityId() == a.getOpportunityId()).findFirst()
            .ifPresent(o -> {
                if (!"Open".equalsIgnoreCase(o.getStatus())) throw new AppException("This opportunity is no longer accepting applications.");
            });
        a.setStatus("Pending");
        appRepo.save(a);
    }

    public void updateApplicationStatus(Application a) { appRepo.update(a); }
}
