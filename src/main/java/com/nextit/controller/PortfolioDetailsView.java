package com.nextit.controller;

import com.nextit.model.Portfolio;
import com.nextit.service.AcademicService;
import com.nextit.service.DirectoryService;
import com.nextit.service.ProjectService;
import com.nextit.service.StudentService;
import com.nextit.service.StudentSkillService;
import com.nextit.util.Ui;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

/**
 * Shared read-only portfolio view used by administrators when reviewing a
 * submission and by employers when opening an authorised portfolio.
 */
public final class PortfolioDetailsView {

    private static final DirectoryService directory = new DirectoryService();
    private static final StudentSkillService skills = new StudentSkillService();
    private static final ProjectService projects = new ProjectService();
    private static final StudentService students = new StudentService();
    private static final AcademicService academic = new AcademicService();

    private PortfolioDetailsView() {}

    public static void show(Portfolio p) {
        int studentId = p.getStudentId();
        String studentName = directory.studentName(studentId);

        Node content = Ui.form(
                new String[]{"Student", "Headline", "Visibility", "Biography", "Career goal", "Technical skills",
                        "Projects", "Achievements", "Certifications", "Academic highlights"},
                new Node[]{new Label(studentName), new Label(value(p.getHeadline())),
                        new Label(value(p.getVisibility())), wrapped(p.getBio()), wrapped(p.getCareerGoal()),
                        wrapped(technicalSkills(studentId)), wrapped(projectList(studentId)),
                        wrapped(achievementList(studentId)), wrapped(certificationList(studentId)),
                        new Label(String.format("Average grade: %.2f%%   |   Attendance: %.2f%%",
                                academic.averageForStudent(studentId), academic.attendanceRateForStudent(studentId)))});

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportWidth(700);
        scroll.setPrefViewportHeight(560);
        Ui.viewDialog("Portfolio - " + studentName, scroll);
    }

    private static String technicalSkills(int studentId) {
        var skillNames = directory.skillNames();
        StringBuilder text = new StringBuilder();
        skills.forStudent(studentId).forEach(s -> text.append("• ")
                .append(skillNames.getOrDefault(s.getSkillId(), "Skill #" + s.getSkillId()))
                .append(" — ").append(value(s.getProficiencyLevel()))
                .append(s.getAssessmentScore() == null ? "" : " (" + s.getAssessmentScore() + "%)")
                .append('\n'));
        return text.toString();
    }

    private static String projectList(int studentId) {
        StringBuilder text = new StringBuilder();
        projects.projectsForStudent(studentId).forEach(p -> text.append("• ")
                .append(p.getProjectTitle()).append(" — ").append(p.getProjectType())
                .append(p.getTechnologiesUsed() == null ? "" : " [" + p.getTechnologiesUsed() + "]")
                .append(p.getStatus() == null ? "" : " · " + p.getStatus())
                .append('\n'));
        return text.toString();
    }

    private static String achievementList(int studentId) {
        StringBuilder text = new StringBuilder();
        students.achievementsFor(studentId).forEach(a -> text.append("• ")
                .append(a.getTitle())
                .append(a.getOrganization() == null ? "" : " — " + a.getOrganization())
                .append('\n'));
        return text.toString();
    }

    private static String certificationList(int studentId) {
        StringBuilder text = new StringBuilder();
        students.certificationsFor(studentId).forEach(c -> text.append("• ")
                .append(c.getCertificationName())
                .append(c.getIssuingOrganization() == null ? "" : " — " + c.getIssuingOrganization())
                .append('\n'));
        return text.toString();
    }

    private static Label wrapped(String text) {
        Label label = new Label(text == null || text.isBlank() ? "Not provided" : text);
        label.setWrapText(true);
        return label;
    }

    private static String value(String text) { return text == null ? "" : text; }
}
