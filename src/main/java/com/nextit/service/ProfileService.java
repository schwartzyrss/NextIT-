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
        Validator.maxLength(student.getStudentNumber(), 30, "Student number");
        Validator.maxLength(student.getProgram(), 100, "Program");
        Validator.maxLength(student.getSection(), 30, "Section");
        if (student.getYearLevel() < 1) throw new AppException("Year level must be at least 1.");
        studentRepo.update(student);
    }

    public void updateInstructor(Instructor instructor) {
        Validator.notBlank(instructor.getDepartment(), "Department");
        Validator.maxLength(instructor.getEmployeeNumber(), 30, "Employee number");
        Validator.maxLength(instructor.getDepartment(), 100, "Department");
        Validator.maxLength(instructor.getSpecialization(), 150, "Specialization");
        instructorRepo.update(instructor);
    }

    public void updateEmployer(Employer employer) {
        Validator.notBlank(employer.getCompanyName(), "Company name");
        Validator.maxLength(employer.getCompanyName(), 150, "Company name");
        Validator.maxLength(employer.getIndustry(), 100, "Industry");
        Validator.maxLength(employer.getContactPerson(), 100, "Contact person");
        Validator.maxLength(employer.getContactNumber(), 30, "Contact number");
        Validator.maxLength(employer.getAddress(), 255, "Address");

        // Enforce uniqueness for Contact Number across companies
        if (employer.getContactNumber() != null && !employer.getContactNumber().isBlank()) {
            String normPhone = employer.getContactNumber().replaceAll("[^0-9+]", "");
            boolean duplicatePhone = employerRepo.findAll().stream()
                    .anyMatch(other -> other.getEmployerId() != employer.getEmployerId()
                            && other.getContactNumber() != null
                            && !other.getContactNumber().isBlank()
                            && other.getContactNumber().replaceAll("[^0-9+]", "").equalsIgnoreCase(normPhone));
            if (duplicatePhone) {
                throw new AppException("This contact number is already registered to another company.");
            }
        }

        // Enforce uniqueness for Office Address across companies
        if (employer.getAddress() != null && !employer.getAddress().isBlank()) {
            String normAddr = employer.getAddress().trim().toLowerCase(java.util.Locale.ROOT);
            boolean duplicateAddress = employerRepo.findAll().stream()
                    .anyMatch(other -> other.getEmployerId() != employer.getEmployerId()
                            && other.getAddress() != null
                            && !other.getAddress().isBlank()
                            && other.getAddress().trim().toLowerCase(java.util.Locale.ROOT).equals(normAddr));
            if (duplicateAddress) {
                throw new AppException("This office address is already in use. Please specify a unique branch or suite number.");
            }
        }

        employerRepo.update(employer);
    }

    public boolean isContactNumberTaken(int currentEmployerId, String contactNumber) {
        if (contactNumber == null || contactNumber.isBlank()) return false;
        String normPhone = contactNumber.replaceAll("[^0-9+]", "");
        return employerRepo.findAll().stream()
                .anyMatch(other -> other.getEmployerId() != currentEmployerId
                        && other.getContactNumber() != null
                        && !other.getContactNumber().isBlank()
                        && other.getContactNumber().replaceAll("[^0-9+]", "").equalsIgnoreCase(normPhone));
    }

    public boolean isAddressTaken(int currentEmployerId, String address) {
        if (address == null || address.isBlank()) return false;
        String normAddr = address.trim().toLowerCase(java.util.Locale.ROOT);
        return employerRepo.findAll().stream()
                .anyMatch(other -> other.getEmployerId() != currentEmployerId
                        && other.getAddress() != null
                        && !other.getAddress().isBlank()
                        && other.getAddress().trim().toLowerCase(java.util.Locale.ROOT).equals(normAddr));
    }
}
