-- NextIT database schema (MySQL)
CREATE DATABASE IF NOT EXISTS nextit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nextit;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('STUDENT','INSTRUCTOR','EMPLOYER','ADMIN') NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    student_number VARCHAR(30) NOT NULL UNIQUE,
    program VARCHAR(100) NOT NULL,
    year_level INT NOT NULL,
    section VARCHAR(30) NULL,
    enrollment_status VARCHAR(30) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS instructors (
    instructor_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    employee_number VARCHAR(30) UNIQUE,
    department VARCHAR(100) NOT NULL,
    specialization VARCHAR(150) NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employers (
    employer_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    company_name VARCHAR(150) NOT NULL,
    industry VARCHAR(100) NULL,
    contact_person VARCHAR(100) NULL,
    contact_number VARCHAR(30) NULL,
    address VARCHAR(255) NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS subjects (
    subject_id INT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(30) NOT NULL UNIQUE,
    subject_name VARCHAR(150) NOT NULL,
    units DECIMAL(3,1) NOT NULL,
    description TEXT NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    subject_id INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE RESTRICT,
    INDEX idx_enrollments_student (student_id),
    INDEX idx_enrollments_subject (subject_id)
);

CREATE TABLE IF NOT EXISTS grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    instructor_id INT NOT NULL,
    activity_name VARCHAR(150) NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    score DECIMAL(6,2) NOT NULL,
    max_score DECIMAL(6,2) NOT NULL,
    grade DECIMAL(5,2) NULL,
    remarks TEXT NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id) ON DELETE RESTRICT,
    INDEX idx_grades_enrollment (enrollment_id)
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks TEXT NULL,
    recorded_by INT NOT NULL,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    FOREIGN KEY (recorded_by) REFERENCES instructors(instructor_id) ON DELETE RESTRICT,
    INDEX idx_attendance_enrollment (enrollment_id)
);

CREATE TABLE IF NOT EXISTS technical_skills (
    skill_id INT AUTO_INCREMENT PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    description TEXT NULL
);

CREATE TABLE IF NOT EXISTS student_skills (
    student_skill_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    skill_id INT NOT NULL,
    proficiency_level VARCHAR(30) NOT NULL,
    assessment_score DECIMAL(5,2) NULL,
    assessed_by INT NULL,
    assessed_at DATETIME NULL,
    remarks TEXT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES technical_skills(skill_id) ON DELETE RESTRICT,
    FOREIGN KEY (assessed_by) REFERENCES instructors(instructor_id) ON DELETE SET NULL,
    INDEX idx_student_skills_student (student_id)
);

CREATE TABLE IF NOT EXISTS projects (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NULL,
    subject_id INT NULL,
    project_title VARCHAR(200) NOT NULL,
    project_type VARCHAR(50) NOT NULL,
    description TEXT NULL,
    technologies_used TEXT NULL,
    repository_url VARCHAR(255) NULL,
    demo_url VARCHAR(255) NULL,
    project_date DATE NULL,
    status VARCHAR(30) NOT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE SET NULL,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE SET NULL,
    INDEX idx_projects_student (student_id)
);

CREATE TABLE IF NOT EXISTS project_members (
    project_member_id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    student_id INT NOT NULL,
    role VARCHAR(100) NULL,
    contribution TEXT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_project_members_project (project_id)
);

CREATE TABLE IF NOT EXISTS project_evaluations (
    evaluation_id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    student_id INT NOT NULL,
    instructor_id INT NOT NULL,
    contribution_score DECIMAL(5,2) NULL,
    technical_score DECIMAL(5,2) NULL,
    teamwork_score DECIMAL(5,2) NULL,
    overall_score DECIMAL(5,2) NULL,
    feedback TEXT NULL,
    evaluated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS achievements (
    achievement_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    organization VARCHAR(150) NULL,
    achievement_date DATE NULL,
    verification_url VARCHAR(255) NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_achievements_student (student_id)
);

CREATE TABLE IF NOT EXISTS certifications (
    certification_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    certification_name VARCHAR(200) NOT NULL,
    issuing_organization VARCHAR(150) NOT NULL,
    issue_date DATE NULL,
    expiration_date DATE NULL,
    credential_url VARCHAR(255) NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_certifications_student (student_id)
);

CREATE TABLE IF NOT EXISTS portfolios (
    portfolio_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL UNIQUE,
    headline VARCHAR(200) NULL,
    bio TEXT NULL,
    career_goal TEXT NULL,
    visibility VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS portfolio_access (
    access_id INT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id INT NOT NULL,
    employer_id INT NOT NULL,
    access_status VARCHAR(30) NOT NULL,
    granted_at DATETIME NULL,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE CASCADE,
    FOREIGN KEY (employer_id) REFERENCES employers(employer_id) ON DELETE CASCADE,
    INDEX idx_portfolio_access_portfolio (portfolio_id)
);

CREATE TABLE IF NOT EXISTS academic_support (
    support_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    instructor_id INT NOT NULL,
    subject_id INT NULL,
    support_type VARCHAR(50) NOT NULL,
    support_date DATE NOT NULL,
    reason TEXT NULL,
    action_taken TEXT NULL,
    outcome TEXT NULL,
    remarks TEXT NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors(instructor_id) ON DELETE RESTRICT,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE SET NULL,
    INDEX idx_academic_support_student (student_id)
);

CREATE TABLE IF NOT EXISTS job_opportunities (
    opportunity_id INT AUTO_INCREMENT PRIMARY KEY,
    employer_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    opportunity_type VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    required_skills TEXT NULL,
    location VARCHAR(150) NULL,
    deadline DATE NULL,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employer_id) REFERENCES employers(employer_id) ON DELETE CASCADE,
    INDEX idx_opportunities_employer (employer_id)
);

CREATE TABLE IF NOT EXISTS applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    opportunity_id INT NOT NULL,
    student_id INT NOT NULL,
    application_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    remarks TEXT NULL,
    FOREIGN KEY (opportunity_id) REFERENCES job_opportunities(opportunity_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_applications_opportunity (opportunity_id),
    INDEX idx_applications_student (student_id)
);
