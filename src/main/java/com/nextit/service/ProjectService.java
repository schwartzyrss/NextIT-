package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.*;
import com.nextit.repository.*;
import com.nextit.util.Validator;

import java.util.List;

public class ProjectService {
    private final JdbcProjectRepository projectRepo = new JdbcProjectRepository();
    private final JdbcProjectEvaluationRepository evalRepo = new JdbcProjectEvaluationRepository();
    private final JdbcProjectMemberRepository memberRepo = new JdbcProjectMemberRepository();
    private final JdbcSubjectRepository subjectRepo = new JdbcSubjectRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();
    private final JdbcInstructorRepository instructorRepo = new JdbcInstructorRepository();

    public List<Project> projectsForStudent(int studentId) { return projectRepo.findByStudentId(studentId); }
    public List<Project> allProjects() { return projectRepo.findAll(); }

    /** True when the student owns the project record (not just membership). */
    public boolean isOwner(Project project, int studentId) {
        return project != null && project.getStudentId() != null && project.getStudentId() == studentId;
    }

    public void addProject(Project p) {
        if (p.getStudentId() == null || p.getStudentId() <= 0) {
            throw new AppException("A student owner is required to create a project.");
        }
        if (studentRepo.findById(p.getStudentId()).isEmpty()) {
            throw new AppException("The project owner could not be found.");
        }
        validate(p);
        projectRepo.save(p);
    }

    public void updateProject(Project p, int actingStudentId) {
        requireOwner(p.getProjectId(), actingStudentId, "edit");
        validate(p);
        projectRepo.update(p);
    }

    public void deleteProject(int id, int actingStudentId) {
        requireOwner(id, actingStudentId, "delete");
        projectRepo.delete(id);
    }

    public List<ProjectMember> membersForProject(int projectId) { return memberRepo.findByProjectId(projectId); }

    public void addMember(ProjectMember m, int actingStudentId) {
        if (m.getProjectId() <= 0) throw new AppException("A project is required.");
        if (m.getStudentId() <= 0) throw new AppException("A student is required.");
        if (studentRepo.findById(m.getStudentId()).isEmpty()) {
            throw new AppException("Select a valid student.");
        }
        Project project = requireOwner(m.getProjectId(), actingStudentId, "manage group members for");
        if (isOwner(project, m.getStudentId())) {
            throw new AppException("The project owner cannot also be added as a group member.");
        }
        if (memberRepo.findByProjectAndStudent(m.getProjectId(), m.getStudentId()).isPresent()) {
            throw new AppException("That student is already a member of this project.");
        }
        memberRepo.save(m);
    }

    public void removeMember(int projectMemberId, int actingStudentId) {
        ProjectMember member = memberRepo.findById(projectMemberId)
                .orElseThrow(() -> new AppException("Project member not found."));
        requireOwner(member.getProjectId(), actingStudentId, "manage group members for");
        memberRepo.delete(projectMemberId);
    }

    private Project requireOwner(int projectId, int actingStudentId, String action) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new AppException("Project not found."));
        if (!isOwner(project, actingStudentId)) {
            throw new AppException("Only the project owner can " + action + " this project.");
        }
        return project;
    }

    public List<ProjectEvaluation> evaluationsForStudent(int studentId) { return evalRepo.findByStudentId(studentId); }
    public List<ProjectEvaluation> allEvaluations() { return evalRepo.findAll(); }

    /** The single evaluation for a student on a project, when one has been recorded. */
    public java.util.Optional<ProjectEvaluation> evaluationFor(int projectId, int studentId) {
        return evalRepo.findByProjectAndStudent(projectId, studentId);
    }

    public void evaluate(ProjectEvaluation e) {
        if (e.getProjectId() <= 0 || e.getStudentId() <= 0) {
            throw new AppException("Select a project and a student.");
        }
        if (e.getInstructorId() <= 0) throw new AppException("An instructor is required.");
        if (instructorRepo.findById(e.getInstructorId()).isEmpty()) {
            throw new AppException("The evaluating instructor could not be found.");
        }
        Project project = projectRepo.findById(e.getProjectId())
                .orElseThrow(() -> new AppException("Select a valid project."));
        boolean owner = project.getStudentId() != null && project.getStudentId() == e.getStudentId();
        boolean member = memberRepo.findByProjectAndStudent(e.getProjectId(), e.getStudentId()).isPresent();
        if (!owner && !member) {
            throw new AppException("That student is not a member of the selected project.");
        }
        validateScore(e.getContributionScore(), "Contribution score");
        validateScore(e.getTechnicalScore(), "Technical score");
        validateScore(e.getTeamworkScore(), "Teamwork score");
        if (e.getOverallScore() == null) {
            e.setOverallScore(average(e.getContributionScore(), e.getTechnicalScore(), e.getTeamworkScore()));
        }
        validateScore(e.getOverallScore(), "Overall score");
        // One evaluation per student per project: re-evaluating updates the
        // existing record instead of creating a duplicate row.
        evalRepo.findByProjectAndStudent(e.getProjectId(), e.getStudentId())
                .ifPresent(existing -> e.setEvaluationId(existing.getEvaluationId()));
        if (e.getEvaluationId() > 0) evalRepo.update(e);
        else evalRepo.save(e);
    }

    private void validate(Project p) {
        Validator.notBlank(p.getProjectTitle(), "Project title");
        Validator.notBlank(p.getProjectType(), "Project type");
        Validator.notBlank(p.getStatus(), "Project status");
        Validator.optionalWebUrl(p.getRepositoryUrl(), "Repository URL");
        Validator.optionalWebUrl(p.getDemoUrl(), "Demo URL");
        if (p.getSubjectId() != null && subjectRepo.findById(p.getSubjectId()).isEmpty()) {
            throw new AppException("Select a valid subject.");
        }
    }

    private void validateScore(Double score, String name) {
        if (score != null && (!Double.isFinite(score) || score < 0 || score > 100)) {
            throw new AppException(name + " must be between 0 and 100.");
        }
    }

    public static Double average(Double a, Double b, Double c) {
        double sum = 0;
        int n = 0;
        if (a != null) { sum += a; n++; }
        if (b != null) { sum += b; n++; }
        if (c != null) { sum += c; n++; }
        return n == 0 ? null : Math.round(sum / n * 100.0) / 100.0;
    }
}
