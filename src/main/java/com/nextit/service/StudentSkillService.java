package com.nextit.service;

import com.nextit.model.StudentSkill;
import com.nextit.repository.JdbcStudentSkillRepository;

import java.util.List;

public class StudentSkillService {
    private final JdbcStudentSkillRepository repo = new JdbcStudentSkillRepository();

    public List<StudentSkill> forStudent(int studentId) { return repo.findByStudentId(studentId); }
    public List<StudentSkill> all() { return repo.findAll(); }
    public void save(StudentSkill s) { repo.save(s); }
}
