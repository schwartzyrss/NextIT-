package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.AcademicSupport;
import com.nextit.repository.JdbcAcademicSupportRepository;
import com.nextit.repository.JdbcInstructorRepository;
import com.nextit.repository.JdbcStudentRepository;
import com.nextit.repository.JdbcSubjectRepository;
import com.nextit.util.Validator;

import java.util.List;

public class SupportService {
    private final JdbcAcademicSupportRepository repo = new JdbcAcademicSupportRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();
    private final JdbcInstructorRepository instructorRepo = new JdbcInstructorRepository();
    private final JdbcSubjectRepository subjectRepo = new JdbcSubjectRepository();

    public List<AcademicSupport> forStudent(int studentId) { return repo.findByStudentId(studentId); }
    public List<AcademicSupport> all() { return repo.findAll(); }

    public void record(AcademicSupport a) {
        if (a.getStudentId() <= 0 || studentRepo.findById(a.getStudentId()).isEmpty()) {
            throw new AppException("Select a valid student.");
        }
        if (a.getInstructorId() <= 0 || instructorRepo.findById(a.getInstructorId()).isEmpty()) {
            throw new AppException("Select a valid instructor.");
        }
        if (a.getSubjectId() != null && subjectRepo.findById(a.getSubjectId()).isEmpty()) {
            throw new AppException("Select a valid subject.");
        }
        Validator.notBlank(a.getSupportType(), "Support type");
        Validator.maxLength(a.getSupportType(), 50, "Support type");
        Validator.maxLength(a.getReason(), 16000, "Reason");
        Validator.maxLength(a.getActionTaken(), 16000, "Action taken");
        Validator.maxLength(a.getOutcome(), 16000, "Outcome");
        Validator.maxLength(a.getRemarks(), 16000, "Remarks");
        if (a.getSupportDate() == null) throw new AppException("Support date is required.");
        Validator.notBlank(a.getReason(), "Reason");
        repo.save(a);
    }

    public void delete(int supportId) { repo.delete(supportId); }
}
