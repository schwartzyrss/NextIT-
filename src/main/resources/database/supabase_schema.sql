-- NextIT Supabase PostgreSQL Schema
-- Run this script in your Supabase SQL Editor (https://supabase.com/dashboard/project/_/sql)

CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT','INSTRUCTOR','EMPLOYER','ADMIN')),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS students (
    student_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    student_number VARCHAR(30) NOT NULL UNIQUE,
    program VARCHAR(100) NOT NULL,
    year_level INT NOT NULL,
    section VARCHAR(30) NULL,
    enrollment_status VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS instructors (
    instructor_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    employee_number VARCHAR(30) UNIQUE,
    department VARCHAR(100) NOT NULL,
    specialization VARCHAR(150) NULL
);

CREATE TABLE IF NOT EXISTS employers (
    employer_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    company_name VARCHAR(150) NOT NULL,
    industry VARCHAR(100) NULL,
    contact_person VARCHAR(100) NULL,
    contact_number VARCHAR(30) NULL,
    address VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS subjects (
    subject_id SERIAL PRIMARY KEY,
    subject_code VARCHAR(30) NOT NULL UNIQUE,
    subject_name VARCHAR(150) NOT NULL,
    units DECIMAL(3,1) NOT NULL,
    description TEXT NULL
);

CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    subject_id INT NOT NULL REFERENCES subjects(subject_id) ON DELETE RESTRICT,
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_enrollments_student ON enrollments(student_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_subject ON enrollments(subject_id);

CREATE TABLE IF NOT EXISTS grades (
    grade_id SERIAL PRIMARY KEY,
    enrollment_id INT NOT NULL REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    instructor_id INT NOT NULL REFERENCES instructors(instructor_id) ON DELETE RESTRICT,
    activity_name VARCHAR(150) NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    score DECIMAL(6,2) NOT NULL,
    max_score DECIMAL(6,2) NOT NULL,
    grade DECIMAL(5,2) NULL,
    remarks TEXT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_grades_enrollment ON grades(enrollment_id);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id SERIAL PRIMARY KEY,
    enrollment_id INT NOT NULL REFERENCES enrollments(enrollment_id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks TEXT NULL,
    recorded_by INT NOT NULL REFERENCES instructors(instructor_id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_attendance_enrollment ON attendance(enrollment_id);

CREATE TABLE IF NOT EXISTS technical_skills (
    skill_id SERIAL PRIMARY KEY,
    skill_name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    description TEXT NULL
);

CREATE TABLE IF NOT EXISTS student_skills (
    student_skill_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    skill_id INT NOT NULL REFERENCES technical_skills(skill_id) ON DELETE RESTRICT,
    proficiency_level VARCHAR(30) NOT NULL,
    assessment_score DECIMAL(5,2) NULL,
    assessed_by INT NULL REFERENCES instructors(instructor_id) ON DELETE SET NULL,
    assessed_at TIMESTAMPTZ NULL,
    remarks TEXT NULL,
    CONSTRAINT uq_student_skill UNIQUE (student_id, skill_id)
);
CREATE INDEX IF NOT EXISTS idx_student_skills_student ON student_skills(student_id);

CREATE TABLE IF NOT EXISTS projects (
    project_id SERIAL PRIMARY KEY,
    student_id INT NULL REFERENCES students(student_id) ON DELETE SET NULL,
    subject_id INT NULL REFERENCES subjects(subject_id) ON DELETE SET NULL,
    project_title VARCHAR(200) NOT NULL,
    project_type VARCHAR(50) NOT NULL,
    description TEXT NULL,
    technologies_used TEXT NULL,
    repository_url VARCHAR(255) NULL,
    demo_url VARCHAR(255) NULL,
    project_date DATE NULL,
    status VARCHAR(30) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_projects_student ON projects(student_id);

CREATE TABLE IF NOT EXISTS project_members (
    project_member_id SERIAL PRIMARY KEY,
    project_id INT NOT NULL REFERENCES projects(project_id) ON DELETE CASCADE,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    role VARCHAR(100) NULL,
    contribution TEXT NULL,
    CONSTRAINT uq_project_member UNIQUE (project_id, student_id)
);
CREATE INDEX IF NOT EXISTS idx_project_members_project ON project_members(project_id);

CREATE TABLE IF NOT EXISTS project_evaluations (
    evaluation_id SERIAL PRIMARY KEY,
    project_id INT NOT NULL REFERENCES projects(project_id) ON DELETE CASCADE,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    instructor_id INT NOT NULL REFERENCES instructors(instructor_id) ON DELETE RESTRICT,
    contribution_score DECIMAL(5,2) NULL,
    technical_score DECIMAL(5,2) NULL,
    teamwork_score DECIMAL(5,2) NULL,
    overall_score DECIMAL(5,2) NULL,
    feedback TEXT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_project_evaluation UNIQUE (project_id, student_id)
);

CREATE TABLE IF NOT EXISTS achievements (
    achievement_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    organization VARCHAR(150) NULL,
    achievement_date DATE NULL,
    verification_url VARCHAR(255) NULL
);
CREATE INDEX IF NOT EXISTS idx_achievements_student ON achievements(student_id);

CREATE TABLE IF NOT EXISTS certifications (
    certification_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    certification_name VARCHAR(200) NOT NULL,
    issuing_organization VARCHAR(150) NOT NULL,
    issue_date DATE NULL,
    expiration_date DATE NULL,
    credential_url VARCHAR(255) NULL
);
CREATE INDEX IF NOT EXISTS idx_certifications_student ON certifications(student_id);

CREATE TABLE IF NOT EXISTS portfolios (
    portfolio_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL UNIQUE REFERENCES students(student_id) ON DELETE CASCADE,
    headline VARCHAR(200) NULL,
    bio TEXT NULL,
    career_goal TEXT NULL,
    visibility VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS portfolio_access (
    access_id SERIAL PRIMARY KEY,
    portfolio_id INT NOT NULL REFERENCES portfolios(portfolio_id) ON DELETE CASCADE,
    employer_id INT NOT NULL REFERENCES employers(employer_id) ON DELETE CASCADE,
    access_status VARCHAR(30) NOT NULL,
    granted_at TIMESTAMPTZ NULL,
    CONSTRAINT uq_portfolio_employer UNIQUE (portfolio_id, employer_id)
);
CREATE INDEX IF NOT EXISTS idx_portfolio_access_portfolio ON portfolio_access(portfolio_id);

CREATE TABLE IF NOT EXISTS academic_support (
    support_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    instructor_id INT NOT NULL REFERENCES instructors(instructor_id) ON DELETE RESTRICT,
    subject_id INT NULL REFERENCES subjects(subject_id) ON DELETE SET NULL,
    support_type VARCHAR(50) NOT NULL,
    support_date DATE NOT NULL,
    reason TEXT NULL,
    action_taken TEXT NULL,
    outcome TEXT NULL,
    remarks TEXT NULL
);
CREATE INDEX IF NOT EXISTS idx_academic_support_student ON academic_support(student_id);

CREATE TABLE IF NOT EXISTS job_opportunities (
    opportunity_id SERIAL PRIMARY KEY,
    employer_id INT NOT NULL REFERENCES employers(employer_id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    opportunity_type VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    required_skills TEXT NULL,
    location VARCHAR(150) NULL,
    deadline DATE NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_opportunities_employer ON job_opportunities(employer_id);

CREATE TABLE IF NOT EXISTS applications (
    application_id SERIAL PRIMARY KEY,
    opportunity_id INT NOT NULL REFERENCES job_opportunities(opportunity_id) ON DELETE CASCADE,
    student_id INT NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    application_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status VARCHAR(30) NOT NULL,
    remarks TEXT NULL,
    CONSTRAINT uq_application UNIQUE (opportunity_id, student_id)
);
CREATE INDEX IF NOT EXISTS idx_applications_opportunity ON applications(opportunity_id);
CREATE INDEX IF NOT EXISTS idx_applications_student ON applications(student_id);

