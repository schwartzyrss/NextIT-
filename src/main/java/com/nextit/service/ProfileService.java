package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.Employer;
import com.nextit.model.Instructor;
import com.nextit.model.Student;
import com.nextit.model.User;
import com.nextit.repository.JdbcEmployerRepository;
import com.nextit.repository.JdbcInstructorRepository;
import com.nextit.repository.JdbcStudentRepository;
import com.nextit.repository.JdbcUserRepository;
import com.nextit.util.Validator;

import java.util.Optional;

/**
 * Loads and updates the profile that belongs to the signed-in account.
 */
public class ProfileService {

    private final JdbcUserRepository userRepo = new JdbcUserRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();
    private final JdbcInstructorRepository instructorRepo = new JdbcInstructorRepository();
    private final JdbcEmployerRepository employerRepo = new JdbcEmployerRepository();

    public User account(int userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new AppException("User account not found."));
    }

    public Optional<Student> studentForUser(int userId) { return studentRepo.findByUserId(userId); }
    public Optional<Instructor> instructorForUser(int userId) { return instructorRepo.findByUserId(userId); }
    public Optional<Employer> employerForUser(int userId) { return employerRepo.findByUserId(userId); }

    public void updateStudent(Student student) {
        Validator.notBlank(student.getStudentNumber(), "Student number");
        Validator.notBlank(student.getProgram(), "Program");
        if (student.getYearLevel() < 1) throw new AppException("Year level must be at least 1.");
        studentRepo.update(student);
    }

    public void updateInstructor(Instructor instructor) {
        Validator.notBlank(instructor.getDepartment(), "Department");
        instructorRepo.update(instructor);
    }

    public void updateEmployer(Employer employer) {
        Validator.notBlank(employer.getCompanyName(), "Company name");
        employerRepo.update(employer);
    }
}
