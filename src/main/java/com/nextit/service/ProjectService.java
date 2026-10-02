package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.repository.*;

import java.util.List;

public class ProjectService {
    private final JdbcProjectRepository projectRepo = new JdbcProjectRepository();
    private final JdbcProjectEvaluationRepository evalRepo = new JdbcProjectEvaluationRepository();

    public List<Project> projectsForStudent(int studentId) { return projectRepo.findByStudentId(studentId); }
    public List<Project> allProjects() { return projectRepo.findAll(); }

    public void addProject(Project p) {
        if (p.getProjectTitle() == null || p.getProjectTitle().isBlank()) throw new AppException("Project title is required.");
        projectRepo.save(p);
    }

    public void updateProject(Project p) { projectRepo.update(p); }
    public void deleteProject(int id) { projectRepo.delete(id); }

    public List<ProjectEvaluation> evaluationsForStudent(int studentId) { return evalRepo.findByStudentId(studentId); }
    public List<ProjectEvaluation> allEvaluations() { return evalRepo.findAll(); }

    public void evaluate(ProjectEvaluation e) { evalRepo.save(e); }
}
