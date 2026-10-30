package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.repository.*;
import com.nextit.util.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PortfolioService {
    private final JdbcPortfolioRepository portfolioRepo = new JdbcPortfolioRepository();
    private final JdbcPortfolioAccessRepository accessRepo = new JdbcPortfolioAccessRepository();
    private final JdbcEmployerRepository employerRepo = new JdbcEmployerRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();

    public Optional<Portfolio> portfolioForStudent(int studentId) { return portfolioRepo.findByStudentId(studentId); }
    public Optional<Portfolio> portfolioById(int portfolioId) { return portfolioRepo.findById(portfolioId); }
    public List<Portfolio> approvedPortfolios() { return portfolioRepo.findApproved(); }
    public List<Portfolio> allPortfolios() { return portfolioRepo.findAll(); }

    /** Only portfolios that are approved and explicitly shared with the employer. */
    public List<Portfolio> portfoliosForEmployer(int employerId) {
        return accessRepo.findByEmployerId(employerId).stream()
                .filter(a -> "Approved".equalsIgnoreCase(a.getAccessStatus()))
                .map(a -> portfolioRepo.findById(a.getPortfolioId()))
                .flatMap(Optional::stream)
                .filter(p -> "Approved".equalsIgnoreCase(p.getVisibility()))
                .toList();
    }

    public boolean isVisibleToEmployer(int portfolioId, int employerId) {
        return accessRepo.findByPortfolioAndEmployer(portfolioId, employerId)
                .filter(a -> "Approved".equalsIgnoreCase(a.getAccessStatus()))
                .flatMap(a -> portfolioRepo.findById(portfolioId))
                .filter(p -> "Approved".equalsIgnoreCase(p.getVisibility()))
                .isPresent();
    }

    public void save(Portfolio p) {
        if (p.getStudentId() <= 0 || studentRepo.findById(p.getStudentId()).isEmpty()) {
            throw new AppException("A valid student is required to save a portfolio.");
        }
        Validator.notBlank(p.getVisibility(), "Portfolio visibility");
        if (!List.of("Private", "Pending", "Approved").contains(p.getVisibility())) {
            throw new AppException("Portfolio visibility must be Private, Pending, or Approved.");
        }
        Optional<Portfolio> existing = portfolioRepo.findByStudentId(p.getStudentId());
        if ("Approved".equalsIgnoreCase(p.getVisibility())
                && existing.map(e -> !"Approved".equalsIgnoreCase(e.getVisibility())).orElse(true)) {
            throw new AppException("Only an administrator can approve a portfolio.");
        }
        if (existing.isPresent()) {
            Portfolio stored = existing.get();
            p.setPortfolioId(stored.getPortfolioId());
            if ("Approved".equalsIgnoreCase(stored.getVisibility())
                    && "Approved".equalsIgnoreCase(p.getVisibility())
                    && contentChanged(stored, p)) {
                p.setVisibility("Pending");
            }
            portfolioRepo.update(p);
        } else {
            if (!"Private".equals(p.getVisibility()) && !"Pending".equals(p.getVisibility())) {
                p.setVisibility("Private");
            }
            portfolioRepo.save(p);
        }
    }

    private boolean contentChanged(Portfolio stored, Portfolio updated) {
        return !Objects.equals(stored.getHeadline(), updated.getHeadline())
                || !Objects.equals(stored.getBio(), updated.getBio())
                || !Objects.equals(stored.getCareerGoal(), updated.getCareerGoal());
    }

    public void approve(int portfolioId) {
        Portfolio p = portfolioRepo.findById(portfolioId)
                .orElseThrow(() -> new AppException("Portfolio not found."));
        if (!"Pending".equalsIgnoreCase(p.getVisibility())
                && !"Approved".equalsIgnoreCase(p.getVisibility())) {
            throw new AppException("Only a portfolio submitted for approval can be approved.");
        }
        p.setVisibility("Approved");
        portfolioRepo.update(p);
    }

    public List<PortfolioAccess> accessFor(int portfolioId) { return accessRepo.findByPortfolioId(portfolioId); }

    public void grantAccess(int portfolioId, int employerId) {
        Portfolio portfolio = portfolioRepo.findById(portfolioId)
                .orElseThrow(() -> new AppException("Portfolio not found."));
        if (!"Approved".equalsIgnoreCase(portfolio.getVisibility())) {
            throw new AppException("Approve the portfolio before granting employer access.");
        }
        if (employerRepo.findById(employerId).isEmpty()) {
            throw new AppException("Select a valid employer.");
        }
        Optional<PortfolioAccess> existing = accessRepo.findByPortfolioAndEmployer(portfolioId, employerId);
        if (existing.isPresent()) {
            PortfolioAccess access = existing.get();
            if (!"Revoked".equalsIgnoreCase(access.getAccessStatus())) {
                throw new AppException("This employer already has access to the portfolio.");
            }
            access.setAccessStatus("Approved");
            access.setGrantedAt(LocalDateTime.now());
            accessRepo.update(access);
            return;
        }
        PortfolioAccess access = new PortfolioAccess();
        access.setPortfolioId(portfolioId);
        access.setEmployerId(employerId);
        access.setAccessStatus("Approved");
        access.setGrantedAt(LocalDateTime.now());
        accessRepo.save(access);
    }

    /**
     * FR-19 requires access records to reach a {@code Revoked} state without
     * deleting the audit trail. The record is retained so it can be restored
     * later by granting access again.
     */
    public void revokeAccess(int accessId) {
        PortfolioAccess access = accessRepo.findById(accessId)
                .orElseThrow(() -> new AppException("Portfolio access record not found."));
        if ("Revoked".equalsIgnoreCase(access.getAccessStatus())) {
            throw new AppException("That portfolio access is already revoked.");
        }
        access.setAccessStatus("Revoked");
        accessRepo.update(access);
    }
}
