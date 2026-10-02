-- NextIT demo seed data
-- Demo credentials for all accounts: username / password123
USE nextit;

INSERT INTO users (username, password_hash, email, role, first_name, last_name) VALUES
('student1',    '$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO', 'student@nextit.edu',    'STUDENT',   'Alex',   'Reyes'),
('instructor1', '$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO', 'instructor@nextit.edu', 'INSTRUCTOR','Maria',  'Santos'),
('employer1',   '$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO', 'employer@techcorp.com', 'EMPLOYER',  'John',   'Dela Cruz'),
('admin1',      '$2a$10$AwV9Ci7bWr2MEFJiXixMOuyTY/UvFPDJtbyjva7sFKCcN0iESR2GO', 'admin@nextit.edu',      'ADMIN',     'System', 'Admin');

INSERT INTO students (user_id, student_number, program, year_level, section, enrollment_status) VALUES
(1, '2023-0001', 'BS Information Technology', 3, 'A', 'Enrolled');

INSERT INTO instructors (user_id, employee_number, department, specialization) VALUES
(2, 'EMP-1001', 'Computer Science', 'Software Engineering');

INSERT INTO employers (user_id, company_name, industry, contact_person, contact_number, address) VALUES
(3, 'TechCorp Solutions', 'Software Development', 'John Dela Cruz', '09171234567', 'Makati City');

INSERT INTO subjects (subject_code, subject_name, units, description) VALUES
('IT301', 'Object-Oriented Programming', 3.0, 'OOP concepts with Java'),
('IT302', 'Database Management Systems', 3.0, 'Relational databases and SQL'),
('IT303', 'Web Development', 3.0, 'Modern web application development');

INSERT INTO enrollments (student_id, subject_id, academic_year, semester, status) VALUES
(1, 1, '2025-2026', '1st Semester', 'Enrolled'),
(1, 2, '2025-2026', '1st Semester', 'Enrolled'),
(1, 3, '2025-2026', '1st Semester', 'Enrolled');

INSERT INTO grades (enrollment_id, instructor_id, activity_name, activity_type, score, max_score, grade, remarks) VALUES
(1, 1, 'Midterm Exam', 'Examination', 42.00, 50.00, 84.00, 'Good performance'),
(1, 1, 'Quiz 1', 'Quiz', 18.00, 20.00, 90.00, 'Well done'),
(2, 1, 'Lab 1', 'Laboratory', 45.00, 50.00, 90.00, 'Excellent queries');

INSERT INTO attendance (enrollment_id, attendance_date, status, remarks, recorded_by) VALUES
(1, '2026-09-01', 'Present', NULL, 1),
(1, '2026-09-08', 'Late', 'Arrived 10 minutes late', 1),
(2, '2026-09-01', 'Present', NULL, 1);

INSERT INTO technical_skills (skill_name, category, description) VALUES
('Java Programming', 'Programming', 'Core Java development'),
('MySQL', 'Database Management', 'Relational database design and queries'),
('Cybersecurity Basics', 'Cybersecurity', 'Fundamental security concepts');

INSERT INTO student_skills (student_id, skill_id, proficiency_level, assessment_score, assessed_by, assessed_at, remarks) VALUES
(1, 1, 'Intermediate', 85.00, 1, '2026-09-10 09:00:00', 'Solid OOP skills'),
(1, 2, 'Beginner', 70.00, 1, '2026-09-10 09:00:00', 'Needs practice with joins');

INSERT INTO projects (student_id, subject_id, project_title, project_type, description, technologies_used, repository_url, demo_url, project_date, status) VALUES
(1, 1, 'NextIT Tracker', 'Capstone', 'Student progress tracking desktop app', 'Java, JavaFX, MySQL', 'https://github.com/alex/nextit', NULL, '2026-09-15', 'Draft');

INSERT INTO achievements (student_id, title, description, organization, achievement_date, verification_url) VALUES
(1, 'Dean\'s Lister', 'Top of class for AY 2025-2026', 'State University', '2026-08-30', NULL);

INSERT INTO certifications (student_id, certification_name, issuing_organization, issue_date, expiration_date, credential_url) VALUES
(1, 'Oracle Certified Java Foundations', 'Oracle', '2026-03-01', '2029-03-01', 'https://certs.example.com/java-foundations');

INSERT INTO portfolios (student_id, headline, bio, career_goal, visibility) VALUES
(1, 'Aspiring Software Engineer', 'IT student passionate about desktop and web systems.', 'Become a full-stack developer', 'Approved');

INSERT INTO portfolio_access (portfolio_id, employer_id, access_status, granted_at) VALUES
(1, 1, 'Approved', '2026-09-05 10:00:00');

INSERT INTO job_opportunities (employer_id, title, opportunity_type, description, required_skills, location, deadline, status) VALUES
(1, 'Software Development Intern', 'Internship', 'Assist in building internal tools.', 'Java, MySQL, Git', 'Makati City', '2026-12-31', 'Open'),
(1, 'Junior Java Developer', 'Employment', 'Entry-level Java developer role.', 'Java, JavaFX, SQL', 'Remote', '2026-11-30', 'Open');

INSERT INTO applications (opportunity_id, student_id, status, remarks) VALUES
(1, 1, 'Pending', 'Interested in the internship program');

INSERT INTO academic_support (student_id, instructor_id, subject_id, support_type, support_date, reason, action_taken, outcome, remarks) VALUES
(1, 1, 2, 'Remedial', '2026-09-12', 'Difficulty with SQL joins', 'Extra exercises on joins', 'Improved', 'Follow up next week');
