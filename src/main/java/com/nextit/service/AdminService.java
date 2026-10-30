package com.nextit.service;

import com.nextit.exception.AppException;
import com.nextit.model.Employer;
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
import com.nextit.util.Validator;

import java.util.List;

public class AdminService {

    private final JdbcUserRepository userRepo = new JdbcUserRepository();
    private final JdbcSubjectRepository subjectRepo = new JdbcSubjectRepository();
    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();
    private final JdbcInstructorRepository instructorRepo = new JdbcInstructorRepository();
    private final JdbcEmployerRepository employerRepo = new JdbcEmployerRepository();

    public List<User> users() { return userRepo.findAll(); }
    public List<Subject> subjects() { return subjectRepo.findAll(); }
    public List<Student> students() { return studentRepo.findAll(); }
    public List<Instructor> instructors() { return instructorRepo.findAll(); }
    public List<Employer> employers() { return employerRepo.findAll(); }

    public void createUser(User user, String plainPassword) {
        validateUser(user);
        Validator.notBlank(plainPassword, "Password");
        if (plainPassword.length() < 6) throw new AppException("Password must be at least 6 characters.");
        if (userRepo.findByUsername(user.getUsername().trim()).isPresent()) {
            throw new AppException("That username is already taken.");
        }
        if (userRepo.findByEmail(user.getEmail().trim()).isPresent()) {
            throw new AppException("That email address is already registered.");
        }
        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim());
        user.setPasswordHash(AuthenticationService.hashPassword(plainPassword));
        userRepo.save(user);
        try {
            createProfile(user);
        } catch (RuntimeException e) {
            // The account is unusable without its role profile, so undo the
            // insert rather than leaving a half-created user behind.
            try {
                userRepo.delete(user.getUserId());
            } catch (RuntimeException ignored) {
                // Keep the original failure; the orphaned row can be removed
                // from the admin screen if the rollback itself fails.
            }
            throw e;
        }
    }

    public void updateUser(User user) {
        validateUser(user);
        User existing = userRepo.findById(user.getUserId())
                .orElseThrow(() -> new AppException("User account not found."));
        if (existing.getRole() != user.getRole()) {
            throw new AppException("A user's role cannot be changed after the account is created.");
        }
        userRepo.findByUsername(user.getUsername().trim())
                .filter(other -> other.getUserId() != user.getUserId())
                .ifPresent(other -> { throw new AppException("That username is already taken."); });
        userRepo.findByEmail(user.getEmail().trim())
                .filter(other -> other.getUserId() != user.getUserId())
                .ifPresent(other -> { throw new AppException("That email address is already registered."); });
        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim());
        userRepo.update(user);
    }

    public void deleteUser(int userId) { userRepo.delete(userId); }

    public void resetPassword(int userId, String plainPassword) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException("User account not found."));
        Validator.notBlank(plainPassword, "Password");
        if (plainPassword.length() < 6) throw new AppException("Password must be at least 6 characters.");
        user.setPasswordHash(AuthenticationService.hashPassword(plainPassword));
        userRepo.updatePassword(userId, user.getPasswordHash());
    }

    public void saveSubject(Subject subject) {
        Validator.notBlank(subject.getSubjectCode(), "Subject code");
        Validator.notBlank(subject.getSubjectName(), "Subject name");
        if (!Double.isFinite(subject.getUnits()) || subject.getUnits() <= 0) {
            throw new AppException("Units must be a finite number greater than zero.");
        }
        subjectRepo.findAll().stream()
                .filter(s -> s.getSubjectId() != subject.getSubjectId())
                .filter(s -> s.getSubjectCode().equalsIgnoreCase(subject.getSubjectCode().trim()))
                .findAny()
                .ifPresent(s -> { throw new AppException("That subject code already exists."); });
        subject.setSubjectCode(subject.getSubjectCode().trim().toUpperCase());
        if (subject.getSubjectId() > 0) subjectRepo.update(subject);
        else subjectRepo.save(subject);
    }

    public void deleteSubject(int subjectId) { subjectRepo.delete(subjectId); }

    public void updateStudent(Student student) {
        Validator.notBlank(student.getStudentNumber(), "Student number");
        Validator.notBlank(student.getProgram(), "Program");
        if (student.getYearLevel() < 1) throw new AppException("Year level must be at least 1.");
        studentRepo.findAll().stream()
                .filter(other -> other.getStudentId() != student.getStudentId())
                .filter(other -> String.valueOf(other.getStudentNumber())
                        .equalsIgnoreCase(student.getStudentNumber().trim()))
                .findAny()
                .ifPresent(other -> { throw new AppException("That student number is already in use."); });
        student.setStudentNumber(student.getStudentNumber().trim());
        studentRepo.update(student);
    }

    public void updateInstructor(Instructor instructor) {
        Validator.notBlank(instructor.getDepartment(), "Department");
        String number = instructor.getEmployeeNumber() == null ? "" : instructor.getEmployeeNumber().trim();
        if (!number.isBlank()) {
            instructorRepo.findAll().stream()
                    .filter(other -> other.getInstructorId() != instructor.getInstructorId())
                    .filter(other -> number.equalsIgnoreCase(other.getEmployeeNumber()))
                    .findAny()
                    .ifPresent(other -> { throw new AppException("That employee number is already in use."); });
        }
        instructor.setEmployeeNumber(number.isBlank() ? null : number);
        instructorRepo.update(instructor);
    }

    public void updateEmployer(Employer employer) {
        Validator.notBlank(employer.getCompanyName(), "Company name");
        employerRepo.update(employer);
    }

    private void validateUser(User user) {
        Validator.notBlank(user.getUsername(), "Username");
        Validator.email(user.getEmail());
        Validator.notBlank(user.getFirstName(), "First name");
        Validator.notBlank(user.getLastName(), "Last name");
        if (user.getRole() == null) throw new AppException("A role is required.");
    }

    /**
     * Every role that owns profile data gets a starter profile so the new
     * account can open its dashboard immediately. The administrator can refine
     * the generated identifiers from the profile screens afterwards.
     */
    private void createProfile(User user) {
        if (user.getRole() == Role.STUDENT) {
            Student student = new Student();
            student.setUserId(user.getUserId());
            student.setStudentNumber("TBD-" + user.getUserId());
            student.setProgram("Not specified");
            student.setYearLevel(1);
            student.setSection("");
            student.setEnrollmentStatus("Enrolled");
            studentRepo.save(student);
        } else if (user.getRole() == Role.INSTRUCTOR) {
            Instructor instructor = new Instructor();
            instructor.setUserId(user.getUserId());
            instructor.setEmployeeNumber("TBD-" + user.getUserId());
            instructor.setDepartment("Not specified");
            instructor.setSpecialization("");
            instructorRepo.save(instructor);
        } else if (user.getRole() == Role.EMPLOYER) {
            Employer employer = new Employer();
            employer.setUserId(user.getUserId());
            String company = (user.getFirstName() + " " + user.getLastName()).trim();
            employer.setCompanyName(company.isBlank() ? user.getUsername() : company + " Company");
            employer.setIndustry("");
            employer.setContactPerson(company);
            employer.setContactNumber("");
            employer.setAddress("");
            employerRepo.save(employer);
        }
    }
}
