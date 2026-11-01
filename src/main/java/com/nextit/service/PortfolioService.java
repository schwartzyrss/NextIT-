package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.repository.*;
import com.nextit.util.Validator;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PortfolioService {
    private static final List<String> VISIBILITIES =
            List.of("Private", "Pending", "Approved", "Public");

    private final JdbcPortfolioRepository portfolioRepo = new JdbcPortfolioRepository();
    private final JdbcPortfolioAccessRepository accessRepo = new JdbcPortfolioAccessRepository();
    private final JdbcEmployerRepository employerRepo = new JdbcEmployerRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();

    public Optional<Portfolio> portfolioForStudent(int studentId) { return portfolioRepo.findByStudentId(studentId); }
    public Optional<Portfolio> portfolioById(int portfolioId) { return portfolioRepo.findById(portfolioId); }
    public List<Portfolio> approvedPortfolios() { return portfolioRepo.findApproved(); }
    public List<Portfolio> allPortfolios() { return portfolioRepo.findAll(); }

    /** Public portfolios plus approved portfolios that were explicitly shared with the employer. */
    public List<Portfolio> portfoliosForEmployer(int employerId) {
        Map<Integer, Portfolio> visible = new LinkedHashMap<>();
        // Public portfolios are visible to every employer without an access record.
        portfolioRepo.findApproved().stream()
                .filter(PortfolioService::isPublic)
                .forEach(p -> visible.put(p.getPortfolioId(), p));
        // Approved portfolios still require an explicit, non-revoked access grant.
        accessRepo.findByEmployerId(employerId).stream()
                .filter(a -> "Approved".equalsIgnoreCase(a.getAccessStatus()))
                .map(a -> portfolioRepo.findById(a.getPortfolioId()))
                .flatMap(Optional::stream)
                .filter(PortfolioService::isApproved)
                .forEach(p -> visible.putIfAbsent(p.getPortfolioId(), p));
        return List.copyOf(visible.values());
    }

    public boolean isVisibleToEmployer(int portfolioId, int employerId) {
        Optional<Portfolio> portfolio = portfolioRepo.findById(portfolioId);
        if (portfolio.isEmpty()) return false;
        if (isPublic(portfolio.get())) return true;
        if (!isApproved(portfolio.get())) return false;
        return accessRepo.findByPortfolioAndEmployer(portfolioId, employerId)
                .filter(a -> "Approved".equalsIgnoreCase(a.getAccessStatus()))
                .isPresent();
    }

    static boolean isValidVisibility(String visibility) {
        return visibility != null && VISIBILITIES.contains(visibility);
    }

    static boolean isPublic(Portfolio portfolio) {
        return portfolio != null && "Public".equalsIgnoreCase(portfolio.getVisibility());
    }

    static boolean isApproved(Portfolio portfolio) {
        return portfolio != null && "Approved".equalsIgnoreCase(portfolio.getVisibility());
    }

    public void save(Portfolio p) {
        if (p.getStudentId() <= 0 || studentRepo.findById(p.getStudentId()).isEmpty()) {
            throw new AppException("A valid student is required to save a portfolio.");
        }
        Validator.notBlank(p.getVisibility(), "Portfolio visibility");
        Validator.maxLength(p.getHeadline(), 200, "Headline");
        Validator.maxLength(p.getBio(), 16000, "Bio");
        Validator.maxLength(p.getCareerGoal(), 16000, "Career goal");
        if (!isValidVisibility(p.getVisibility())) {
            throw new AppException("Portfolio visibility must be Private, Pending, Approved, or Public.");
        }
        Optional<Portfolio> existing = portfolioRepo.findByStudentId(p.getStudentId());
        if ("Approved".equalsIgnoreCase(p.getVisibility())
                && existing.map(e -> !"Approved".equalsIgnoreCase(e.getVisibility())).orElse(true)) {
            throw new AppException("Only an administrator can approve a portfolio.");
        }
        if (existing.isPresent()) {
            Portfolio stored = existing.get();
            p.setPortfolioId(stored.getPortfolioId());
            if (isApproved(stored) && isApproved(p) && contentChanged(stored, p)) {
                p.setVisibility("Pending");
            }
            portfolioRepo.update(p);
        } else {
            if (!"Private".equals(p.getVisibility()) && !"Pending".equals(p.getVisibility())
                    && !"Public".equals(p.getVisibility())) {
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
        if (!isApproved(portfolio) && !isPublic(portfolio)) {
            throw new AppException("Submit and approve the portfolio before granting employer access.");
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
