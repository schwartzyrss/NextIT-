package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.StudentSkill;
import com.nextit.repository.JdbcInstructorRepository;
import com.nextit.repository.JdbcStudentRepository;
import com.nextit.repository.JdbcStudentSkillRepository;
import com.nextit.repository.JdbcTechnicalSkillRepository;
import com.nextit.util.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class StudentSkillService {
    private static final Set<String> LEVELS =
            Set.of("Beginner", "Intermediate", "Advanced", "Expert");

    private final JdbcStudentSkillRepository repo = new JdbcStudentSkillRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();
    private final JdbcTechnicalSkillRepository skillRepo = new JdbcTechnicalSkillRepository();
    private final JdbcInstructorRepository instructorRepo = new JdbcInstructorRepository();

    public List<StudentSkill> forStudent(int studentId) { return repo.findByStudentId(studentId); }
    public List<StudentSkill> all() { return repo.findAll(); }

    public java.util.Optional<StudentSkill> find(int studentId, int skillId) {
        if (studentId <= 0 || skillId <= 0) return java.util.Optional.empty();
        return repo.findByStudentAndSkill(studentId, skillId);
    }

    public void recordEvaluation(StudentSkill s, int instructorId) {
        if (s.getStudentId() <= 0 || studentRepo.findById(s.getStudentId()).isEmpty()) {
            throw new AppException("Select a valid student.");
        }
        if (s.getSkillId() <= 0 || skillRepo.findById(s.getSkillId()).isEmpty()) {
            throw new AppException("Select a valid technical skill.");
        }
        if (instructorId <= 0 || instructorRepo.findById(instructorId).isEmpty()) {
            throw new AppException("A valid instructor is required to record an assessment.");
        }
        if (s.getProficiencyLevel() == null || !LEVELS.contains(s.getProficiencyLevel())) {
            throw new AppException("Proficiency must be Beginner, Intermediate, Advanced, or Expert.");
        }
        if (s.getAssessmentScore() != null) {
            Validator.nonNegative(s.getAssessmentScore(), "Assessment score");
            if (s.getAssessmentScore() > 100) throw new AppException("Assessment score cannot exceed 100.");
        }
        s.setAssessedBy(instructorId);
        s.setAssessedAt(LocalDateTime.now());
        // Keep one assessment per student/skill pair; re-assessing updates it.
        if (s.getStudentSkillId() <= 0) {
            repo.findByStudentAndSkill(s.getStudentId(), s.getSkillId())
                    .ifPresent(existing -> s.setStudentSkillId(existing.getStudentSkillId()));
        }
        if (s.getStudentSkillId() > 0) repo.update(s);
        else repo.save(s);
    }

    public void delete(int id) {
        if (repo.findById(id).isEmpty()) {
            throw new AppException("Skill assessment not found.");
        }
        repo.delete(id);
    }
}
