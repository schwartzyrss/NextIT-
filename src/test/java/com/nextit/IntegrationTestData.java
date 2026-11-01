package com.nextit;

import com.nextit.model.Employer;
import com.nextit.model.Enrollment;
import com.nextit.model.Instructor;
import com.nextit.model.Role;
import com.nextit.model.Student;
import com.nextit.model.Subject;
import com.nextit.model.User;
import com.nextit.repository.JdbcEmployerRepository;
import com.nextit.repository.JdbcInstructorRepository;
import com.nextit.repository.JdbcStudentRepository;
import com.nextit.repository.JdbcSubjectRepository;
import com.nextit.repository.JdbcUserRepository;
import com.nextit.service.AcademicService;

import java.util.concurrent.atomic.AtomicInteger;

/** Creates isolated, uniquely named records for database-backed service tests. */
final class IntegrationTestData {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final JdbcUserRepository users = new JdbcUserRepository();
    private final JdbcStudentRepository students = new JdbcStudentRepository();
    private final JdbcInstructorRepository instructors = new JdbcInstructorRepository();
    private final JdbcEmployerRepository employers = new JdbcEmployerRepository();
    private final JdbcSubjectRepository subjects = new JdbcSubjectRepository();
    private final AcademicService academic = new AcademicService();

    private static String suffix() {
        return System.nanoTime() + "x" + COUNTER.incrementAndGet();
    }

    User user(Role role) {
        String unique = suffix();
        User user = new User();
        user.setUsername("test_" + unique);
        user.setPasswordHash("$2a$10$01234567890123456789012345678901234567890123456789012");
        user.setEmail("test_" + unique + "@example.test");
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName(role.name().toLowerCase());
        users.save(user);
        return user;
    }

    Student student() {
        User user = user(Role.STUDENT);
        Student student = new Student();
        student.setUserId(user.getUserId());
        student.setStudentNumber("S" + suffix());
        student.setProgram("BS Information Technology");
        student.setYearLevel(3);
        student.setSection("IT3A");
        student.setEnrollmentStatus("Enrolled");
        students.save(student);
        return student;
    }

    Instructor instructor() {
        User user = user(Role.INSTRUCTOR);
        Instructor instructor = new Instructor();
        instructor.setUserId(user.getUserId());
        instructor.setEmployeeNumber("E" + suffix());
        instructor.setDepartment("Information Technology");
        instructor.setSpecialization("Software Engineering");
        instructors.save(instructor);
        return instructor;
    }

    Employer employer() {
        User user = user(Role.EMPLOYER);
        Employer employer = new Employer();
        employer.setUserId(user.getUserId());
        employer.setCompanyName("Company " + suffix());
        employer.setIndustry("Technology");
        employer.setContactPerson("Hiring Manager");
        employer.setContactNumber("09000000000");
        employer.setAddress("Test Address");
        employers.save(employer);
        return employer;
    }

    Subject subject() {
        Subject subject = new Subject();
        subject.setSubjectCode("SUB" + suffix());
        subject.setSubjectName("Test Subject " + suffix());
        subject.setUnits(3.0);
        subject.setDescription("Database-backed test subject");
        subjects.save(subject);
        return subject;
    }

    Enrollment enrollment(Student student, Subject subject) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getStudentId());
        enrollment.setSubjectId(subject.getSubjectId());
        enrollment.setAcademicYear("2025-2026");
        enrollment.setSemester("1st Semester");
        enrollment.setStatus("Enrolled");
        academic.saveEnrollment(enrollment);
        return enrollment;
    }
}
