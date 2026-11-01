package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.TechnicalSkill;
import com.nextit.repository.JdbcTechnicalSkillRepository;
import com.nextit.util.Validator;

import java.util.List;

/**
 * Technical skill catalogue operations. Both instructors and administrators
 * are allowed to maintain the catalogue, so the validation lives here rather
 * than in the administrator-only service.
 */
public class SkillService {

    private final JdbcTechnicalSkillRepository skillRepo = new JdbcTechnicalSkillRepository();

    public List<TechnicalSkill> skills() { return skillRepo.findAll(); }

    public void save(TechnicalSkill skill) {
        Validator.notBlank(skill.getSkillName(), "Skill name");
        Validator.notBlank(skill.getCategory(), "Category");
        Validator.maxLength(skill.getSkillName(), 100, "Skill name");
        Validator.maxLength(skill.getCategory(), 100, "Category");
        Validator.maxLength(skill.getDescription(), 16000, "Description");
        String name = skill.getSkillName().trim();
        skillRepo.findAll().stream()
                .filter(other -> other.getSkillId() != skill.getSkillId())
                .filter(other -> name.equalsIgnoreCase(String.valueOf(other.getSkillName()).trim()))
                .findAny()
                .ifPresent(other -> { throw new AppException("That technical skill already exists."); });
        skill.setSkillName(name);
        skill.setCategory(skill.getCategory().trim());
        if (skill.getSkillId() > 0) {
            if (skillRepo.findById(skill.getSkillId()).isEmpty()) {
                throw new AppException("Technical skill not found.");
            }
            skillRepo.update(skill);
        } else skillRepo.save(skill);
    }

    public void delete(int skillId) {
        if (skillRepo.findById(skillId).isEmpty()) {
            throw new AppException("Technical skill not found.");
        }
        skillRepo.delete(skillId);
    }
}
