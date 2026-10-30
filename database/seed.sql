-- NextIT demo seed data
-- Demo credentials for all accounts: username / password123
--
-- The script looks up generated primary keys instead of assuming that the
-- auto-increment counters start at 1, so it also works on a database that has
-- been used before (provided the demo rows do not already exist).
USE nextit;

INSERT INTO users (username, password_hash, email, role, first_name, last_name) VALUES
('student1',    '$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO', 'student@nextit.edu',    'STUDENT',   'Alex',   'Reyes'),
('student2',    '$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO', 'student2@nextit.edu',   'STUDENT',   'Bianca', 'Cruz'),
('instructor1', '$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO', 'instructor@nextit.edu', 'INSTRUCTOR','Maria',  'Santos'),
('employer1',   '$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO', 'employer@techcorp.com', 'EMPLOYER',  'John',   'Dela Cruz'),
('admin1',      '$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO', 'admin@nextit.edu',      'ADMIN',     'System', 'Admin');

SET @student_user    = (SELECT user_id FROM users WHERE username = 'student1');
SET @student2_user   = (SELECT user_id FROM users WHERE username = 'student2');
SET @instructor_user = (SELECT user_id FROM users WHERE username = 'instructor1');
SET @employer_user   = (SELECT user_id FROM users WHERE username = 'employer1');

INSERT INTO students (user_id, student_number, program, year_level, section, enrollment_status) VALUES
(@student_user, '2023-0001', 'BS Information Technology', 3, 'A', 'Enrolled'),
(@student2_user, '2023-0002', 'BS Information Technology', 3, 'A', 'Enrolled');

INSERT INTO instructors (user_id, employee_number, department, specialization) VALUES
(@instructor_user, 'EMP-1001', 'Computer Science', 'Software Engineering');

INSERT INTO employers (user_id, company_name, industry, contact_person, contact_number, address) VALUES
(@employer_user, 'TechCorp Solutions', 'Software Development', 'John Dela Cruz', '09171234567', 'Makati City');

SET @student_id    = (SELECT student_id FROM students WHERE user_id = @student_user);
SET @student2_id   = (SELECT student_id FROM students WHERE user_id = @student2_user);
SET @instructor_id = (SELECT instructor_id FROM instructors WHERE user_id = @instructor_user);
SET @employer_id   = (SELECT employer_id FROM employers WHERE user_id = @employer_user);

INSERT INTO subjects (subject_code, subject_name, units, description) VALUES
('IT301', 'Object-Oriented Programming', 3.0, 'OOP concepts with Java'),
('IT302', 'Database Management Systems', 3.0, 'Relational databases and SQL'),
('IT303', 'Web Development', 3.0, 'Modern web application development');

SET @subject_301 = (SELECT subject_id FROM subjects WHERE subject_code = 'IT301');
SET @subject_302 = (SELECT subject_id FROM subjects WHERE subject_code = 'IT302');
SET @subject_303 = (SELECT subject_id FROM subjects WHERE subject_code = 'IT303');

INSERT INTO enrollments (student_id, subject_id, academic_year, semester, status) VALUES
(@student_id, @subject_301, '2025-2026', '1st Semester', 'Enrolled'),
(@student_id, @subject_302, '2025-2026', '1st Semester', 'Enrolled'),
(@student_id, @subject_303, '2025-2026', '1st Semester', 'Enrolled');

SET @enrollment_301 = (SELECT enrollment_id FROM enrollments
                       WHERE student_id = @student_id AND subject_id = @subject_301);
SET @enrollment_302 = (SELECT enrollment_id FROM enrollments
                       WHERE student_id = @student_id AND subject_id = @subject_302);

INSERT INTO grades (enrollment_id, instructor_id, activity_name, activity_type, score, max_score, grade, remarks) VALUES
(@enrollment_301, @instructor_id, 'Midterm Exam', 'Examination', 42.00, 50.00, 84.00, 'Good performance'),
(@enrollment_301, @instructor_id, 'Quiz 1', 'Quiz', 18.00, 20.00, 90.00, 'Well done'),
(@enrollment_302, @instructor_id, 'Lab 1', 'Laboratory', 45.00, 50.00, 90.00, 'Excellent queries');

INSERT INTO attendance (enrollment_id, attendance_date, status, remarks, recorded_by) VALUES
(@enrollment_301, '2026-09-01', 'Present', NULL, @instructor_id),
(@enrollment_301, '2026-09-08', 'Late', 'Arrived 10 minutes late', @instructor_id),
(@enrollment_302, '2026-09-01', 'Present', NULL, @instructor_id);

INSERT INTO technical_skills (skill_name, category, description) VALUES
('Java Programming', 'Programming', 'Core Java development'),
('MySQL', 'Database Management', 'Relational database design and queries'),
('Cybersecurity Basics', 'Cybersecurity', 'Fundamental security concepts');

SET @skill_java = (SELECT skill_id FROM technical_skills WHERE skill_name = 'Java Programming');
SET @skill_mysql = (SELECT skill_id FROM technical_skills WHERE skill_name = 'MySQL');

INSERT INTO student_skills (student_id, skill_id, proficiency_level, assessment_score, assessed_by, assessed_at, remarks) VALUES
(@student_id, @skill_java, 'Intermediate', 85.00, @instructor_id, '2026-09-10 09:00:00', 'Solid OOP skills'),
(@student_id, @skill_mysql, 'Beginner', 70.00, @instructor_id, '2026-09-10 09:00:00', 'Needs practice with joins');

INSERT INTO projects (student_id, subject_id, project_title, project_type, description, technologies_used, repository_url, demo_url, project_date, status) VALUES
(@student_id, @subject_301, 'NextIT Tracker', 'Capstone', 'Student progress tracking desktop app', 'Java, JavaFX, MySQL', 'https://github.com/alex/nextit', NULL, '2026-09-15', 'Draft');

SET @project_id = (SELECT project_id FROM projects
                   WHERE student_id = @student_id AND project_title = 'NextIT Tracker');

INSERT INTO project_members (project_id, student_id, role, contribution) VALUES
(@project_id, @student2_id, 'Frontend Developer', 'Built the dashboard screens and interaction flows');

INSERT INTO project_evaluations (project_id, student_id, instructor_id, contribution_score, technical_score, teamwork_score, overall_score, feedback) VALUES
(@project_id, @student_id, @instructor_id, 90.00, 88.00, 85.00, 87.67, 'Strong implementation and clear documentation');

INSERT INTO achievements (student_id, title, description, organization, achievement_date, verification_url) VALUES
(@student_id, 'Dean''s Lister', 'Top of class for AY 2025-2026', 'State University', '2026-08-30', NULL);

INSERT INTO certifications (student_id, certification_name, issuing_organization, issue_date, expiration_date, credential_url) VALUES
(@student_id, 'Oracle Certified Java Foundations', 'Oracle', '2026-03-01', '2029-03-01', 'https://certs.example.com/java-foundations');

INSERT INTO portfolios (student_id, headline, bio, career_goal, visibility) VALUES
(@student_id, 'Aspiring Software Engineer', 'IT student passionate about desktop and web systems.', 'Become a full-stack developer', 'Approved');

SET @portfolio_id = (SELECT portfolio_id FROM portfolios WHERE student_id = @student_id);

INSERT INTO portfolio_access (portfolio_id, employer_id, access_status, granted_at) VALUES
(@portfolio_id, @employer_id, 'Approved', '2026-09-05 10:00:00');

INSERT INTO job_opportunities (employer_id, title, opportunity_type, description, required_skills, location, deadline, status) VALUES
(@employer_id, 'Software Development Intern', 'Internship', 'Assist in building internal tools.', 'Java, MySQL, Git', 'Makati City', '2026-12-31', 'Open'),
(@employer_id, 'Junior Java Developer', 'Employment', 'Entry-level Java developer role.', 'Java, JavaFX, SQL', 'Remote', '2026-11-30', 'Open');

SET @opportunity_intern = (SELECT opportunity_id FROM job_opportunities
                           WHERE employer_id = @employer_id AND title = 'Software Development Intern');

INSERT INTO applications (opportunity_id, student_id, status, remarks) VALUES
(@opportunity_intern, @student_id, 'Pending', 'Interested in the internship program');

INSERT INTO academic_support (student_id, instructor_id, subject_id, support_type, support_date, reason, action_taken, outcome, remarks) VALUES
(@student_id, @instructor_id, @subject_302, 'Remedial', '2026-09-12', 'Difficulty with SQL joins', 'Extra exercises on joins', 'Improved', 'Follow up next week');
