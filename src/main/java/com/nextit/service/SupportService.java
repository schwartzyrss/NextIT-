package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.AcademicSupport;
import com.nextit.repository.JdbcAcademicSupportRepository;

import java.util.List;

public class SupportService {
    private final JdbcAcademicSupportRepository repo = new JdbcAcademicSupportRepository();

    public List<AcademicSupport> forStudent(int studentId) { return repo.findByStudentId(studentId); }
    public List<AcademicSupport> all() { return repo.findAll(); }

    public void record(AcademicSupport a) {
        if (a.getSupportType() == null || a.getSupportType().isBlank()) throw new AppException("Support type is required.");
        if (a.getSupportDate() == null) throw new AppException("Support date is required.");
        repo.save(a);
    }
}
