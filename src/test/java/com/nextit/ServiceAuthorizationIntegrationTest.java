package com.nextit;

import com.nextit.exception.AppException;
import com.nextit.model.Achievement;
import com.nextit.model.Application;
import com.nextit.model.Attendance;
import com.nextit.model.Certification;
import com.nextit.model.Employer;
import com.nextit.model.Enrollment;
import com.nextit.model.Grade;
import com.nextit.model.Instructor;
import com.nextit.model.JobOpportunity;
import com.nextit.model.Portfolio;
import com.nextit.model.PortfolioAccess;
import com.nextit.model.Project;
import com.nextit.model.ProjectEvaluation;
import com.nextit.model.ProjectMember;
import com.nextit.model.Student;
import com.nextit.model.StudentSkill;
import com.nextit.model.Subject;
import com.nextit.model.TechnicalSkill;
import com.nextit.repository.JdbcTechnicalSkillRepository;
import com.nextit.service.AcademicService;
import com.nextit.service.CareerService;
import com.nextit.service.PortfolioService;
import com.nextit.service.ProjectService;
import com.nextit.service.StudentService;
import com.nextit.service.StudentSkillService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the real MySQL schema and repositories to prove that ownership,
 * approval and validation rules hold at the service boundary rather than only
 * in the JavaFX controllers.
 */
class ServiceAuthorizationIntegrationTest {

    private static final AtomicInteger SUBJECT_SEQUENCE = new AtomicInteger();

    private static IntegrationTestData data;

    @BeforeAll
    static void startDatabase() throws SQLException {
        DatabaseTestServer.getInstance().useForApplication();
        data = new IntegrationTestData();
    }

    @Test
    void achievementsAndCertificationsCanOnlyBeChangedByTheirStudent() {
        Student owner = data.student();
        Student other = data.student();
        StudentService service = new StudentService();

        Achievement achievement = new Achievement();
        achievement.setStudentId(owner.getStudentId());
        achievement.setTitle("Best Capstone Proposal");
        achievement.setOrganization("NextIT");
        achievement.setAchievementDate(LocalDate.now().minusDays(3));
        service.saveAchievement(achievement);
        assertTrue(achievement.getAchievementId() > 0);

        achievement.setStudentId(other.getStudentId());
        achievement.setTitle("Hijacked");
        assertThrows(AppException.class, () -> service.saveAchievement(achievement));
        assertThrows(AppException.class,
                () -> service.deleteAchievement(achievement.getAchievementId(), other.getStudentId()));

        achievement.setStudentId(owner.getStudentId());
        achievement.setTitle("Best Capstone Proposal (updated)");
        service.saveAchievement(achievement);
        assertEquals("Best Capstone Proposal (updated)",
                service.achievementsFor(owner.getStudentId()).stream()
                        .filter(a -> a.getAchievementId() == achievement.getAchievementId())
                        .findFirst().orElseThrow().getTitle());

        service.deleteAchievement(achievement.getAchievementId(), owner.getStudentId());
        assertTrue(service.achievementsFor(owner.getStudentId()).stream()
                .noneMatch(a -> a.getAchievementId() == achievement.getAchievementId()));

        Certification certification = new Certification();
        certification.setStudentId(owner.getStudentId());
        certification.setCertificationName("Java Foundations");
        certification.setIssuingOrganization("Oracle");
        service.saveCertification(certification);
        assertTrue(certification.getCertificationId() > 0);

        certification.setStudentId(other.getStudentId());
        assertThrows(AppException.class, () -> service.saveCertification(certification));
        assertThrows(AppException.class,
                () -> service.deleteCertification(certification.getCertificationId(), other.getStudentId()));

        Certification badDates = new Certification();
        badDates.setStudentId(owner.getStudentId());
        badDates.setCertificationName("Expired");
        badDates.setIssuingOrganization("Issuer");
        badDates.setIssueDate(LocalDate.now());
        badDates.setExpirationDate(LocalDate.now().minusDays(1));
        assertThrows(AppException.class, () -> service.saveCertification(badDates));
    }

    @Test
    void projectOwnershipControlsEditingMembersAndDeletion() {
        Student owner = data.student();
        Student member = data.student();
        Student outsider = data.student();
        Instructor instructor = data.instructor();
        Subject subject = data.subject();
        ProjectService service = new ProjectService();

        Project project = new Project();
        project.setStudentId(owner.getStudentId());
        project.setSubjectId(subject.getSubjectId());
        project.setProjectTitle("Ownership Integration Project");
        project.setProjectType("Capstone");
        project.setProjectDate(LocalDate.now());
        project.setStatus("Draft");
        service.addProject(project);
        assertTrue(project.getProjectId() > 0);

        project.setProjectTitle("Unauthorised rename");
        assertThrows(AppException.class, () -> service.updateProject(project, member.getStudentId()));
        project.setProjectTitle("Ownership Integration Project");
        service.updateProject(project, owner.getStudentId());

        ProjectMember membership = new ProjectMember();
        membership.setProjectId(project.getProjectId());
        membership.setStudentId(member.getStudentId());
        membership.setRole("Developer");
        membership.setContribution("Built the repository layer");
        assertThrows(AppException.class, () -> service.addMember(membership, outsider.getStudentId()));
        service.addMember(membership, owner.getStudentId());
        assertTrue(membership.getProjectMemberId() > 0);

        ProjectMember duplicate = new ProjectMember();
        duplicate.setProjectId(project.getProjectId());
        duplicate.setStudentId(member.getStudentId());
        assertThrows(AppException.class, () -> service.addMember(duplicate, owner.getStudentId()));

        ProjectMember ownerAsMember = new ProjectMember();
        ownerAsMember.setProjectId(project.getProjectId());
        ownerAsMember.setStudentId(owner.getStudentId());
        assertThrows(AppException.class, () -> service.addMember(ownerAsMember, owner.getStudentId()));

        ProjectEvaluation evaluation = new ProjectEvaluation();
        evaluation.setProjectId(project.getProjectId());
        evaluation.setStudentId(member.getStudentId());
        evaluation.setInstructorId(instructor.getInstructorId());
        evaluation.setContributionScore(90.0);
        evaluation.setTechnicalScore(80.0);
        evaluation.setTeamworkScore(70.0);
        service.evaluate(evaluation);
        assertEquals(80.0, evaluation.getOverallScore());

        ProjectEvaluation notAMember = new ProjectEvaluation();
        notAMember.setProjectId(project.getProjectId());
        notAMember.setStudentId(outsider.getStudentId());
        notAMember.setInstructorId(instructor.getInstructorId());
        assertThrows(AppException.class, () -> service.evaluate(notAMember));

        assertThrows(AppException.class,
                () -> service.removeMember(membership.getProjectMemberId(), outsider.getStudentId()));
        service.removeMember(membership.getProjectMemberId(), owner.getStudentId());
        assertTrue(service.membersForProject(project.getProjectId()).isEmpty());

        assertThrows(AppException.class,
                () -> service.deleteProject(project.getProjectId(), member.getStudentId()));
        service.deleteProject(project.getProjectId(), owner.getStudentId());
        assertTrue(service.allProjects().stream()
                .noneMatch(p -> p.getProjectId() == project.getProjectId()));
    }

    @Test
    void employersCanOnlyManageTheirOwnOpportunitiesAndApplications() {
        Employer owner = data.employer();
        Employer other = data.employer();
        Student applicant = data.student();
        Student secondApplicant = data.student();
        CareerService service = new CareerService();

        JobOpportunity opportunity = new JobOpportunity();
        opportunity.setTitle("Junior Java Developer");
        opportunity.setOpportunityType("Employment");
        opportunity.setDescription("Build JavaFX desktop applications with the NextIT team.");
        opportunity.setRequiredSkills("Java, JavaFX, SQL");
        opportunity.setLocation("Manila");
        opportunity.setDeadline(LocalDate.now().plusDays(30));
        opportunity.setStatus("Open");
        service.addOpportunity(opportunity, owner.getEmployerId());
        assertTrue(opportunity.getOpportunityId() > 0);
        assertEquals(owner.getEmployerId(), opportunity.getEmployerId());

        opportunity.setTitle("Unauthorised edit");
        assertThrows(AppException.class,
                () -> service.updateOpportunity(opportunity, other.getEmployerId()));
        opportunity.setTitle("Junior Java Developer");
        service.updateOpportunity(opportunity, owner.getEmployerId());

        Application application = new Application();
        application.setStudentId(applicant.getStudentId());
        application.setOpportunityId(opportunity.getOpportunityId());
        application.setRemarks("Please consider my portfolio.");
        service.apply(application);
        assertTrue(application.getApplicationId() > 0);

        Application duplicate = new Application();
        duplicate.setStudentId(applicant.getStudentId());
        duplicate.setOpportunityId(opportunity.getOpportunityId());
        assertThrows(AppException.class, () -> service.apply(duplicate));

        assertThrows(AppException.class, () -> service.updateApplicationStatus(
                other.getEmployerId(), application, "Shortlisted", "Wrong employer"));
        service.updateApplicationStatus(
                owner.getEmployerId(), application, "Shortlisted", "Strong portfolio");
        assertEquals("Shortlisted", service.applicationById(application.getApplicationId()).getStatus());
        assertEquals("Strong portfolio", service.applicationById(application.getApplicationId()).getRemarks());

        assertThrows(AppException.class, () -> service.updateApplicationStatus(
                owner.getEmployerId(), application, "Hired", "Not a documented status"));

        opportunity.setStatus("Closed");
        service.updateOpportunity(opportunity, owner.getEmployerId());
        Application late = new Application();
        late.setStudentId(secondApplicant.getStudentId());
        late.setOpportunityId(opportunity.getOpportunityId());
        assertThrows(AppException.class, () -> service.apply(late));

        assertThrows(AppException.class,
                () -> service.deleteOpportunity(opportunity.getOpportunityId(), other.getEmployerId()));
        service.deleteOpportunity(opportunity.getOpportunityId(), owner.getEmployerId());
        assertTrue(service.allOpportunities().stream()
                .noneMatch(o -> o.getOpportunityId() == opportunity.getOpportunityId()));
    }

    @Test
    void portfolioApprovalPublicVisibilityAndAccessGrantsAreEnforced() {
        Student publicStudent = data.student();
        Student approvedStudent = data.student();
        Student unapprovedStudent = data.student();
        Employer firstEmployer = data.employer();
        Employer secondEmployer = data.employer();
        PortfolioService service = new PortfolioService();

        Portfolio publicPortfolio = new Portfolio();
        publicPortfolio.setStudentId(publicStudent.getStudentId());
        publicPortfolio.setHeadline("Public portfolio");
        publicPortfolio.setVisibility("Public");
        service.save(publicPortfolio);
        assertTrue(service.isVisibleToEmployer(
                publicPortfolio.getPortfolioId(), firstEmployer.getEmployerId()));
        assertTrue(service.isVisibleToEmployer(
                publicPortfolio.getPortfolioId(), secondEmployer.getEmployerId()));

        Portfolio approvedPortfolio = new Portfolio();
        approvedPortfolio.setStudentId(approvedStudent.getStudentId());
        approvedPortfolio.setHeadline("Approved portfolio");
        approvedPortfolio.setVisibility("Pending");
        service.save(approvedPortfolio);
        assertThrows(AppException.class, () -> service.grantAccess(
                approvedPortfolio.getPortfolioId(), firstEmployer.getEmployerId()));

        service.approve(approvedPortfolio.getPortfolioId());
        Portfolio stored = service.portfolioById(approvedPortfolio.getPortfolioId()).orElseThrow();
        assertEquals("Approved", stored.getVisibility());
        service.grantAccess(stored.getPortfolioId(), firstEmployer.getEmployerId());

        assertTrue(service.isVisibleToEmployer(stored.getPortfolioId(), firstEmployer.getEmployerId()));
        assertFalse(service.isVisibleToEmployer(stored.getPortfolioId(), secondEmployer.getEmployerId()));
        assertTrue(service.portfoliosForEmployer(firstEmployer.getEmployerId()).stream()
                .anyMatch(p -> p.getPortfolioId() == publicPortfolio.getPortfolioId()));
        assertTrue(service.portfoliosForEmployer(firstEmployer.getEmployerId()).stream()
                .anyMatch(p -> p.getPortfolioId() == stored.getPortfolioId()));

        PortfolioAccess access = service.accessFor(stored.getPortfolioId()).stream()
                .filter(a -> a.getEmployerId() == firstEmployer.getEmployerId())
                .findFirst().orElseThrow();
        service.revokeAccess(access.getAccessId());
        assertFalse(service.isVisibleToEmployer(stored.getPortfolioId(), firstEmployer.getEmployerId()));
        assertThrows(AppException.class, () -> service.revokeAccess(access.getAccessId()));

        Portfolio selfApproved = new Portfolio();
        selfApproved.setStudentId(unapprovedStudent.getStudentId());
        selfApproved.setHeadline("Self approved");
        selfApproved.setVisibility("Approved");
        assertThrows(AppException.class, () -> service.save(selfApproved));

        stored.setHeadline("Approved portfolio updated");
        stored.setVisibility("Approved");
        service.save(stored);
        assertEquals("Pending",
                service.portfolioById(stored.getPortfolioId()).orElseThrow().getVisibility());
        assertFalse(service.isVisibleToEmployer(stored.getPortfolioId(), firstEmployer.getEmployerId()));
    }

    @Test
    void academicRecordsValidateDuplicatesScoresAndAttendance() {
        Student student = data.student();
        Subject subject = data.subject();
        Instructor instructor = data.instructor();
        AcademicService service = new AcademicService();

        Enrollment enrollment = data.enrollment(student, subject);
        assertTrue(enrollment.getEnrollmentId() > 0);

        Enrollment duplicate = new Enrollment();
        duplicate.setStudentId(student.getStudentId());
        duplicate.setSubjectId(subject.getSubjectId());
        duplicate.setAcademicYear("2025-2026");
        duplicate.setSemester("1st Semester");
        duplicate.setStatus("Enrolled");
        assertThrows(AppException.class, () -> service.saveEnrollment(duplicate));

        Enrollment nextTerm = new Enrollment();
        nextTerm.setStudentId(student.getStudentId());
        nextTerm.setSubjectId(subject.getSubjectId());
        nextTerm.setAcademicYear("2025-2026");
        nextTerm.setSemester("2nd Semester");
        nextTerm.setStatus("Enrolled");
        service.saveEnrollment(nextTerm);
        assertTrue(nextTerm.getEnrollmentId() > 0);

        Attendance attendance = new Attendance();
        attendance.setEnrollmentId(enrollment.getEnrollmentId());
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setStatus("Present");
        attendance.setRecordedBy(instructor.getInstructorId());
        service.recordAttendance(attendance);
        assertTrue(attendance.getAttendanceId() > 0);

        Attendance duplicateAttendance = new Attendance();
        duplicateAttendance.setEnrollmentId(enrollment.getEnrollmentId());
        duplicateAttendance.setAttendanceDate(LocalDate.now());
        duplicateAttendance.setStatus("Late");
        duplicateAttendance.setRecordedBy(instructor.getInstructorId());
        assertThrows(AppException.class, () -> service.recordAttendance(duplicateAttendance));

        Attendance invalidStatus = new Attendance();
        invalidStatus.setEnrollmentId(enrollment.getEnrollmentId());
        invalidStatus.setAttendanceDate(LocalDate.now().plusDays(1));
        invalidStatus.setStatus("Unknown");
        invalidStatus.setRecordedBy(instructor.getInstructorId());
        assertThrows(AppException.class, () -> service.recordAttendance(invalidStatus));

        Grade grade = new Grade();
        grade.setEnrollmentId(enrollment.getEnrollmentId());
        grade.setInstructorId(instructor.getInstructorId());
        grade.setActivityName("Integration Quiz");
        grade.setActivityType("Quiz");
        grade.setScore(45);
        grade.setMaxScore(50);
        service.recordGrade(grade);
        assertNotNull(grade.getGrade());
        assertEquals(90.0, grade.getGrade(), 0.01);

        Grade invalidScore = new Grade();
        invalidScore.setEnrollmentId(enrollment.getEnrollmentId());
        invalidScore.setInstructorId(instructor.getInstructorId());
        invalidScore.setActivityName("Invalid Quiz");
        invalidScore.setActivityType("Quiz");
        invalidScore.setScore(11);
        invalidScore.setMaxScore(10);
        assertThrows(AppException.class, () -> service.recordGrade(invalidScore));

        Grade invalidInstructor = new Grade();
        invalidInstructor.setEnrollmentId(enrollment.getEnrollmentId());
        invalidInstructor.setInstructorId(999_999);
        invalidInstructor.setActivityName("Invalid Instructor Quiz");
        invalidInstructor.setActivityType("Quiz");
        invalidInstructor.setScore(1);
        invalidInstructor.setMaxScore(1);
        assertThrows(AppException.class, () -> service.recordGrade(invalidInstructor));
    }

    @Test
    void technicalSkillAssessmentsAreValidatedAndUpdatedInPlace() {
        Student student = data.student();
        Instructor instructor = data.instructor();
        TechnicalSkill skill = new TechnicalSkill();
        skill.setSkillName("Integration Skill " + SUBJECT_SEQUENCE.incrementAndGet() + "_" + System.nanoTime());
        skill.setCategory("Testing");
        skill.setDescription("Created by the integration test");
        new JdbcTechnicalSkillRepository().save(skill);

        StudentSkillService service = new StudentSkillService();
        StudentSkill assessment = new StudentSkill();
        assessment.setStudentId(student.getStudentId());
        assessment.setSkillId(skill.getSkillId());
        assessment.setProficiencyLevel("Intermediate");
        assessment.setAssessmentScore(75.0);
        service.recordEvaluation(assessment, instructor.getInstructorId());
        assertTrue(assessment.getStudentSkillId() > 0);
        assertEquals(instructor.getInstructorId(), assessment.getAssessedBy());
        assertNotNull(assessment.getAssessedAt());

        StudentSkill reassessment = new StudentSkill();
        reassessment.setStudentId(student.getStudentId());
        reassessment.setSkillId(skill.getSkillId());
        reassessment.setProficiencyLevel("Advanced");
        reassessment.setAssessmentScore(92.0);
        service.recordEvaluation(reassessment, instructor.getInstructorId());
        assertEquals(assessment.getStudentSkillId(), reassessment.getStudentSkillId());

        StudentSkill stored = service.find(student.getStudentId(), skill.getSkillId()).orElseThrow();
        assertEquals("Advanced", stored.getProficiencyLevel());
        assertEquals(92.0, stored.getAssessmentScore());

        StudentSkill invalidInstructor = new StudentSkill();
        invalidInstructor.setStudentId(student.getStudentId());
        invalidInstructor.setSkillId(skill.getSkillId());
        invalidInstructor.setProficiencyLevel("Advanced");
        assertThrows(AppException.class,
                () -> service.recordEvaluation(invalidInstructor, 999_999));

        StudentSkill invalidLevel = new StudentSkill();
        invalidLevel.setStudentId(student.getStudentId());
        invalidLevel.setSkillId(skill.getSkillId());
        invalidLevel.setProficiencyLevel("Wizard");
        assertThrows(AppException.class,
                () -> service.recordEvaluation(invalidLevel, instructor.getInstructorId()));
    }
}
