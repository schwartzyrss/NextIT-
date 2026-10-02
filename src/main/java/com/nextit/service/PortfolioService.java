package com.nextit.service;

import com.nextit.model.*;
import com.nextit.repository.*;

import java.util.List;
import java.util.Optional;

public class PortfolioService {
    private final JdbcPortfolioRepository portfolioRepo = new JdbcPortfolioRepository();
    private final JdbcPortfolioAccessRepository accessRepo = new JdbcPortfolioAccessRepository();

    public Optional<Portfolio> portfolioForStudent(int studentId) { return portfolioRepo.findByStudentId(studentId); }
    public List<Portfolio> approvedPortfolios() { return portfolioRepo.findApproved(); }

    public void save(Portfolio p) {
        if (portfolioRepo.findByStudentId(p.getStudentId()).isPresent()) portfolioRepo.update(p);
        else portfolioRepo.save(p);
    }

    public List<PortfolioAccess> accessFor(int portfolioId) { return accessRepo.findByPortfolioId(portfolioId); }
}
