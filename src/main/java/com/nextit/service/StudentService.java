package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.Achievement;
import com.nextit.model.Certification;
import com.nextit.repository.JdbcAchievementRepository;
import com.nextit.repository.JdbcCertificationRepository;
import com.nextit.repository.JdbcStudentRepository;
import com.nextit.util.Validator;

import java.util.List;

public class StudentService {

    private final JdbcAchievementRepository achievementRepo = new JdbcAchievementRepository();
    private final JdbcCertificationRepository certificationRepo = new JdbcCertificationRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();

    public List<Achievement> achievementsFor(int studentId) { return achievementRepo.findByStudentId(studentId); }
    public List<Certification> certificationsFor(int studentId) { return certificationRepo.findByStudentId(studentId); }

    public void saveAchievement(Achievement a) {
        Validator.notBlank(a.getTitle(), "Achievement title");
        Validator.optionalWebUrl(a.getVerificationUrl(), "Verification URL");
        if (a.getStudentId() <= 0 || studentRepo.findById(a.getStudentId()).isEmpty()) {
            throw new AppException("A valid student is required.");
        }
        if (a.getAchievementId() > 0) {
            requireAchievementOwner(a.getAchievementId(), a.getStudentId());
            achievementRepo.update(a);
        } else {
            achievementRepo.save(a);
        }
    }

    public void deleteAchievement(int id, int studentId) {
        requireAchievementOwner(id, studentId);
        achievementRepo.delete(id);
    }

    private void requireAchievementOwner(int achievementId, int studentId) {
        Achievement stored = achievementRepo.findById(achievementId)
                .orElseThrow(() -> new AppException("Achievement not found."));
        if (stored.getStudentId() != studentId) {
            throw new AppException("You can only manage your own achievements.");
        }
    }

    public void saveCertification(Certification c) {
        Validator.notBlank(c.getCertificationName(), "Certification name");
        Validator.notBlank(c.getIssuingOrganization(), "Issuing organization");
        Validator.optionalWebUrl(c.getCredentialUrl(), "Credential URL");
        if (c.getStudentId() <= 0 || studentRepo.findById(c.getStudentId()).isEmpty()) {
            throw new AppException("A valid student is required.");
        }
        if (c.getIssueDate() != null && c.getExpirationDate() != null
                && c.getExpirationDate().isBefore(c.getIssueDate())) {
            throw new AppException("Expiration date cannot be before the issue date.");
        }
        if (c.getCertificationId() > 0) {
            requireCertificationOwner(c.getCertificationId(), c.getStudentId());
            certificationRepo.update(c);
        } else {
            certificationRepo.save(c);
        }
    }

    public void deleteCertification(int id, int studentId) {
        requireCertificationOwner(id, studentId);
        certificationRepo.delete(id);
    }

    private void requireCertificationOwner(int certificationId, int studentId) {
        Certification stored = certificationRepo.findById(certificationId)
                .orElseThrow(() -> new AppException("Certification not found."));
        if (stored.getStudentId() != studentId) {
            throw new AppException("You can only manage your own certifications.");
        }
    }
}
