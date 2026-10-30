package com.nextit.service;

import com.nextit.model.Employer;
import com.nextit.model.Instructor;
import com.nextit.model.Student;
import com.nextit.model.Subject;
import com.nextit.model.TechnicalSkill;
import com.nextit.model.User;
import com.nextit.repository.JdbcEmployerRepository;
import com.nextit.repository.JdbcInstructorRepository;
import com.nextit.repository.JdbcStudentRepository;
import com.nextit.repository.JdbcSubjectRepository;
import com.nextit.repository.JdbcTechnicalSkillRepository;
import com.nextit.repository.JdbcUserRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-only lookup service used by the dashboards to turn foreign keys into
 * human-readable labels without leaking SQL into the JavaFX controllers.
 */
public class DirectoryService {

    private final JdbcStudentRepository studentRepo = new JdbcStudentRepository();
    private final JdbcInstructorRepository instructorRepo = new JdbcInstructorRepository();
    private final JdbcEmployerRepository employerRepo = new JdbcEmployerRepository();
    private final JdbcSubjectRepository subjectRepo = new JdbcSubjectRepository();
    private final JdbcTechnicalSkillRepository skillRepo = new JdbcTechnicalSkillRepository();
    private final JdbcUserRepository userRepo = new JdbcUserRepository();

    public List<Student> students() { return studentRepo.findAll(); }
    public List<Subject> subjects() { return subjectRepo.findAll(); }
    public List<TechnicalSkill> technicalSkills() { return skillRepo.findAll(); }
    public List<Instructor> instructors() { return instructorRepo.findAll(); }
    public List<Employer> employers() { return employerRepo.findAll(); }
    public List<User> users() { return userRepo.findAll(); }

    public Map<Integer, String> studentNames() {
        Map<Integer, String> names = userNames();
        return studentRepo.findAll().stream().collect(Collectors.toMap(
                Student::getStudentId,
                s -> names.getOrDefault(s.getUserId(), "Student #" + s.getStudentId()),
                (a, b) -> a));
    }

    public Map<Integer, String> instructorNames() {
        Map<Integer, String> names = userNames();
        return instructorRepo.findAll().stream().collect(Collectors.toMap(
                Instructor::getInstructorId,
                i -> names.getOrDefault(i.getUserId(), "Instructor #" + i.getInstructorId()),
                (a, b) -> a));
    }

    public Map<Integer, String> employerNames() {
        Map<Integer, String> names = userNames();
        return employerRepo.findAll().stream().collect(Collectors.toMap(
                Employer::getEmployerId,
                e -> names.getOrDefault(e.getUserId(), e.getCompanyName()),
                (a, b) -> a));
    }

    public Map<Integer, String> subjectNames() {
        return subjectRepo.findAll().stream().collect(Collectors.toMap(
                Subject::getSubjectId, Subject::getSubjectName, (a, b) -> a));
    }

    public Map<Integer, String> skillNames() {
        return skillRepo.findAll().stream().collect(Collectors.toMap(
                TechnicalSkill::getSkillId,
                s -> s.getSkillName() + " (" + s.getCategory() + ")",
                (a, b) -> a));
    }

    public String studentName(int studentId) {
        return studentNames().getOrDefault(studentId, "Student #" + studentId);
    }

    public String instructorName(int instructorId) {
        return instructorNames().getOrDefault(instructorId, "Instructor #" + instructorId);
    }

    public String employerName(int employerId) {
        return employerNames().getOrDefault(employerId, "Employer #" + employerId);
    }

    public String subjectName(int subjectId) {
        return subjectNames().getOrDefault(subjectId, "Subject #" + subjectId);
    }

    public String skillName(int skillId) {
        return skillNames().getOrDefault(skillId, "Skill #" + skillId);
    }

    private Map<Integer, String> userNames() {
        return userRepo.findAll().stream().collect(Collectors.toMap(
                User::getUserId, User::getFullName, (a, b) -> a));
    }

    public static <T> Map<Integer, T> index(List<T> values, Function<T, Integer> id) {
        return values.stream().collect(Collectors.toMap(id, Function.identity(), (a, b) -> a));
    }
}
