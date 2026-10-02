# NextIT Database Schema

## 1. Overview

NextIT uses a relational database to centralize student academic records, technical competencies, projects, achievements, portfolios, academic support activities, and career opportunities.

The schema is designed around three primary user roles:

- **Students** — manage projects, skills, achievements, and portfolios while viewing academic progress.
- **Instructors** — manage academic records, attendance, evaluations, projects, and student support activities.
- **Employers** — view approved student portfolios and manage internship or employment opportunities.

---

## 2. Entity Relationship Overview

The main entities of the system are:

- Users
- Students
- Instructors
- Employers
- Subjects
- Enrollments
- Grades
- Attendance
- Technical Skills
- Student Skills
- Projects
- Project Members
- Project Evaluations
- Achievements
- Certifications
- Portfolios
- Portfolio Access
- Academic Support
- Job Opportunities
- Applications

### Relationship Summary

```text
Users
 ├── Students
 ├── Instructors
 └── Employers

Students
 ├── Enrollments ─── Subjects
 ├── Grades
 ├── Attendance
 ├── Student Skills ─── Technical Skills
 ├── Projects ─── Project Members
 ├── Achievements
 ├── Certifications
 ├── Portfolios
 ├── Academic Support
 └── Applications ─── Job Opportunities

Instructors
 ├── Grades
 ├── Attendance
 ├── Project Evaluations
 └── Academic Support

Employers
 ├── Job Opportunities
 └── Portfolio Access
```

---

# 3. Database Tables

## 3.1 Users

Stores authentication and basic account information for all system users.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| user_id | INT | PK, AUTO_INCREMENT | Unique user identifier |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| password_hash | VARCHAR(255) | NOT NULL | Hashed password |
| email | VARCHAR(100) | UNIQUE, NOT NULL | User email |
| role | ENUM | NOT NULL | STUDENT, INSTRUCTOR, EMPLOYER, ADMIN |
| first_name | VARCHAR(50) | NOT NULL | First name |
| last_name | VARCHAR(50) | NOT NULL | Last name |
| created_at | DATETIME | NOT NULL | Account creation date |
| updated_at | DATETIME | NOT NULL | Last account update |

---

## 3.2 Students

Stores information specific to IT students.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| student_id | INT | PK, AUTO_INCREMENT | Unique student identifier |
| user_id | INT | FK, UNIQUE, NOT NULL | References Users |
| student_number | VARCHAR(30) | UNIQUE, NOT NULL | Official student number |
| program | VARCHAR(100) | NOT NULL | Degree/program |
| year_level | INT | NOT NULL | Current year level |
| section | VARCHAR(30) | NULL | Student section |
| enrollment_status | VARCHAR(30) | NOT NULL | Current enrollment status |

**Relationship:**

```text
Users 1 ─── 1 Students
```

---

## 3.3 Instructors

Stores information about instructors and faculty members.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| instructor_id | INT | PK, AUTO_INCREMENT | Unique instructor identifier |
| user_id | INT | FK, UNIQUE, NOT NULL | References Users |
| employee_number | VARCHAR(30) | UNIQUE | Employee/faculty number |
| department | VARCHAR(100) | NOT NULL | Academic department |
| specialization | VARCHAR(150) | NULL | Area of specialization |

**Relationship:**

```text
Users 1 ─── 1 Instructors
```

---

## 3.4 Employers

Stores information about industry partners and employers.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| employer_id | INT | PK, AUTO_INCREMENT | Unique employer identifier |
| user_id | INT | FK, UNIQUE, NOT NULL | References Users |
| company_name | VARCHAR(150) | NOT NULL | Company name |
| industry | VARCHAR(100) | NULL | Company industry |
| contact_person | VARCHAR(100) | NULL | Company representative |
| contact_number | VARCHAR(30) | NULL | Contact number |
| address | VARCHAR(255) | NULL | Company address |

**Relationship:**

```text
Users 1 ─── 1 Employers
```

---

# 4. Academic Records

## 4.1 Subjects

Stores subjects offered by the IT department.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| subject_id | INT | PK, AUTO_INCREMENT | Unique subject identifier |
| subject_code | VARCHAR(30) | UNIQUE, NOT NULL | Subject code |
| subject_name | VARCHAR(150) | NOT NULL | Subject name |
| units | DECIMAL(3,1) | NOT NULL | Number of units |
| description | TEXT | NULL | Subject description |

---

## 4.2 Enrollments

Connects students with the subjects they take.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| enrollment_id | INT | PK, AUTO_INCREMENT | Unique enrollment identifier |
| student_id | INT | FK, NOT NULL | References Students |
| subject_id | INT | FK, NOT NULL | References Subjects |
| academic_year | VARCHAR(20) | NOT NULL | Academic year |
| semester | VARCHAR(20) | NOT NULL | Semester |
| status | VARCHAR(30) | NOT NULL | Enrollment status |

**Relationship:**

```text
Students 1 ───< Enrollments >─── 1 Subjects
```

---

## 4.3 Grades

Stores academic grades and performance records.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| grade_id | INT | PK, AUTO_INCREMENT | Unique grade identifier |
| enrollment_id | INT | FK, NOT NULL | References Enrollments |
| instructor_id | INT | FK, NOT NULL | Instructor who recorded the grade |
| activity_name | VARCHAR(150) | NOT NULL | Assessment/activity name |
| activity_type | VARCHAR(50) | NOT NULL | Exam, quiz, laboratory, project, etc. |
| score | DECIMAL(6,2) | NOT NULL | Student score |
| max_score | DECIMAL(6,2) | NOT NULL | Maximum possible score |
| grade | DECIMAL(5,2) | NULL | Computed/assigned grade |
| remarks | TEXT | NULL | Instructor remarks |
| recorded_at | DATETIME | NOT NULL | Date recorded |

---

## 4.4 Attendance

Records student attendance in classes and activities.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| attendance_id | INT | PK, AUTO_INCREMENT | Unique attendance identifier |
| enrollment_id | INT | FK, NOT NULL | References Enrollments |
| attendance_date | DATE | NOT NULL | Date of attendance |
| status | VARCHAR(20) | NOT NULL | Present, absent, late, excused |
| remarks | TEXT | NULL | Attendance remarks |
| recorded_by | INT | FK, NOT NULL | Instructor who recorded attendance |

---

# 5. Technical Skills

## 5.1 Technical Skills

Stores technical competencies that can be evaluated.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| skill_id | INT | PK, AUTO_INCREMENT | Unique skill identifier |
| skill_name | VARCHAR(100) | UNIQUE, NOT NULL | Skill name |
| category | VARCHAR(100) | NOT NULL | Programming, Networking, Database, Cybersecurity, etc. |
| description | TEXT | NULL | Skill description |

### Example Categories

```text
Programming
Networking
Database Management
Cybersecurity
Web Development
Software Development
```

---

## 5.2 Student Skills

Associates students with their technical skill levels.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| student_skill_id | INT | PK, AUTO_INCREMENT | Unique record identifier |
| student_id | INT | FK, NOT NULL | References Students |
| skill_id | INT | FK, NOT NULL | References Technical Skills |
| proficiency_level | VARCHAR(30) | NOT NULL | Beginner, Intermediate, Advanced, Expert |
| assessment_score | DECIMAL(5,2) | NULL | Evaluation score |
| assessed_by | INT | FK | Instructor who evaluated the skill |
| assessed_at | DATETIME | NULL | Assessment date |
| remarks | TEXT | NULL | Evaluation remarks |

**Relationship:**

```text
Students 1 ───< Student Skills >─── 1 Technical Skills
```

---

# 6. Projects

## 6.1 Projects

Stores academic, laboratory, programming, and capstone projects.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| project_id | INT | PK, AUTO_INCREMENT | Unique project identifier |
| student_id | INT | FK, NULL | Owner for individual projects |
| subject_id | INT | FK, NULL | Related subject |
| project_title | VARCHAR(200) | NOT NULL | Project title |
| project_type | VARCHAR(50) | NOT NULL | Laboratory, academic, capstone, personal, etc. |
| description | TEXT | NULL | Project description |
| technologies_used | TEXT | NULL | Technologies used |
| repository_url | VARCHAR(255) | NULL | Source code repository |
| demo_url | VARCHAR(255) | NULL | Project demonstration URL |
| project_date | DATE | NULL | Project completion date |
| status | VARCHAR(30) | NOT NULL | Draft, completed, archived |

---

## 6.2 Project Members

Allows multiple students to participate in group projects.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| project_member_id | INT | PK, AUTO_INCREMENT | Unique membership identifier |
| project_id | INT | FK, NOT NULL | References Projects |
| student_id | INT | FK, NOT NULL | References Students |
| role | VARCHAR(100) | NULL | Member role |
| contribution | TEXT | NULL | Description of contribution |

**Relationship:**

```text
Projects 1 ───< Project Members >─── 1 Students
```

---

## 6.3 Project Evaluations

Allows instructors to evaluate individual project contributions.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| evaluation_id | INT | PK, AUTO_INCREMENT | Unique evaluation identifier |
| project_id | INT | FK, NOT NULL | References Projects |
| student_id | INT | FK, NOT NULL | Student being evaluated |
| instructor_id | INT | FK, NOT NULL | Evaluating instructor |
| contribution_score | DECIMAL(5,2) | NULL | Contribution score |
| technical_score | DECIMAL(5,2) | NULL | Technical performance score |
| teamwork_score | DECIMAL(5,2) | NULL | Teamwork score |
| overall_score | DECIMAL(5,2) | NULL | Overall evaluation |
| feedback | TEXT | NULL | Instructor feedback |
| evaluated_at | DATETIME | NOT NULL | Evaluation date |

---

# 7. Achievements and Certifications

## 7.1 Achievements

Stores student accomplishments.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| achievement_id | INT | PK, AUTO_INCREMENT | Unique achievement identifier |
| student_id | INT | FK, NOT NULL | References Students |
| title | VARCHAR(200) | NOT NULL | Achievement title |
| description | TEXT | NULL | Achievement details |
| organization | VARCHAR(150) | NULL | Issuing organization |
| achievement_date | DATE | NULL | Date achieved |
| verification_url | VARCHAR(255) | NULL | Verification link |

---

## 7.2 Certifications

Stores technical and professional certifications.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| certification_id | INT | PK, AUTO_INCREMENT | Unique certification identifier |
| student_id | INT | FK, NOT NULL | References Students |
| certification_name | VARCHAR(200) | NOT NULL | Certification name |
| issuing_organization | VARCHAR(150) | NOT NULL | Issuing organization |
| issue_date | DATE | NULL | Date issued |
| expiration_date | DATE | NULL | Expiration date |
| credential_url | VARCHAR(255) | NULL | Credential verification URL |

---

# 8. Digital Portfolio

## 8.1 Portfolios

Stores the student's digital portfolio information.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| portfolio_id | INT | PK, AUTO_INCREMENT | Unique portfolio identifier |
| student_id | INT | FK, UNIQUE, NOT NULL | References Students |
| headline | VARCHAR(200) | NULL | Portfolio headline |
| bio | TEXT | NULL | Student biography |
| career_goal | TEXT | NULL | Career objective |
| visibility | VARCHAR(30) | NOT NULL | Private, approved, public |
| created_at | DATETIME | NOT NULL | Portfolio creation date |
| updated_at | DATETIME | NOT NULL | Last update |

A portfolio can display:

- Technical skills
- Projects
- Achievements
- Certifications
- Academic accomplishments

---

## 8.2 Portfolio Access

Controls employer access to student portfolios.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| access_id | INT | PK, AUTO_INCREMENT | Unique access record |
| portfolio_id | INT | FK, NOT NULL | References Portfolios |
| employer_id | INT | FK, NOT NULL | References Employers |
| access_status | VARCHAR(30) | NOT NULL | Pending, approved, revoked |
| granted_at | DATETIME | NULL | Date access was granted |

---

# 9. Academic Support

## 9.1 Academic Support

Records additional academic assistance provided to students.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| support_id | INT | PK, AUTO_INCREMENT | Unique support identifier |
| student_id | INT | FK, NOT NULL | Student receiving support |
| instructor_id | INT | FK, NOT NULL | Instructor providing support |
| subject_id | INT | FK, NULL | Related subject |
| support_type | VARCHAR(50) | NOT NULL | Review, remedial, consultation, exercise |
| support_date | DATE | NOT NULL | Date of support |
| reason | TEXT | NULL | Reason for support |
| action_taken | TEXT | NULL | Support provided |
| outcome | TEXT | NULL | Result of support |
| remarks | TEXT | NULL | Additional remarks |

---

# 10. Career Opportunities

## 10.1 Job Opportunities

Stores internship and employment opportunities posted by employers.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| opportunity_id | INT | PK, AUTO_INCREMENT | Unique opportunity identifier |
| employer_id | INT | FK, NOT NULL | Employer who posted the opportunity |
| title | VARCHAR(200) | NOT NULL | Position title |
| opportunity_type | VARCHAR(30) | NOT NULL | Internship, employment |
| description | TEXT | NOT NULL | Opportunity description |
| required_skills | TEXT | NULL | Required technical skills |
| location | VARCHAR(150) | NULL | Job location |
| deadline | DATE | NULL | Application deadline |
| status | VARCHAR(30) | NOT NULL | Open, closed, archived |
| created_at | DATETIME | NOT NULL | Posting date |

---

## 10.2 Applications

Stores student applications to career opportunities.

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| application_id | INT | PK, AUTO_INCREMENT | Unique application identifier |
| opportunity_id | INT | FK, NOT NULL | References Job Opportunities |
| student_id | INT | FK, NOT NULL | Applicant |
| application_date | DATETIME | NOT NULL | Date applied |
| status | VARCHAR(30) | NOT NULL | Pending, reviewed, shortlisted, accepted, rejected |
| remarks | TEXT | NULL | Application remarks |

---

# 11. Core Entity Relationships

```text
                         ┌───────────────┐
                         │     USERS     │
                         └───────┬───────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
              ▼                  ▼                  ▼
        ┌───────────┐      ┌────────────┐     ┌────────────┐
        │ STUDENTS  │      │ INSTRUCTORS│     │ EMPLOYERS  │
        └─────┬─────┘      └──────┬─────┘     └──────┬─────┘
              │                   │                  │
       ┌──────┼─────────┐         │          ┌───────┴────────┐
       │      │         │         │          │                │
       ▼      ▼         ▼         ▼          ▼                ▼
   ENROLLMENT SKILLS  PROJECTS  GRADES   OPPORTUNITIES   PORTFOLIO ACCESS
       │      │         │         │          │
       ▼      ▼         ▼         ▼          ▼
   SUBJECTS  TECHNICAL  PROJECT   ATTENDANCE APPLICATIONS
             SKILLS     MEMBERS
                          │
                          ▼
                   PROJECT EVALUATIONS

        STUDENTS
           │
     ┌─────┼──────────┬────────────┬──────────────┐
     ▼     ▼          ▼            ▼              ▼
 ACHIEVEMENTS CERTIFICATIONS PORTFOLIOS ACADEMIC SUPPORT
                              │
                              ▼
                       PORTFOLIO ACCESS
```

---

# 12. Role-Based Access

## Student

Students can:

- View academic progress.
- View grades and attendance.
- View technical skill evaluations.
- Add and organize projects.
- Record achievements and certifications.
- Create and manage their digital portfolio.
- View internship and employment opportunities.
- Submit applications.

## Instructor

Instructors can:

- Manage student grades.
- Record attendance.
- Evaluate technical competencies.
- Review student projects.
- Evaluate individual project contributions.
- Record academic support activities.
- Provide feedback on student development.

## Employer

Employers can:

- Manage company information.
- View approved student portfolios.
- Review relevant skills and projects.
- Post internship opportunities.
- Post employment opportunities.
- Review student applications.

## Administrator

Administrators can manage:

- User accounts.
- System roles.
- Academic structure.
- Subjects.
- Technical skill categories.
- System-wide configuration.

---

# 13. Student Development Model

NextIT does not rely on academic grades alone to represent student development.

```text
Academic Performance
        +
Attendance
        +
Technical Skills
        +
Projects
        +
Achievements
        +
Certifications
        +
Academic Support
        │
        ▼
Student Development Profile
        │
        ▼
Digital Portfolio
        │
        ▼
Internship / Employment Opportunities
```

This structure allows the system to connect academic progress with practical technical competencies and career preparation.

---

# 14. Portfolio Data Flow

```text
Students
   │
   ├── Student Skills
   ├── Projects
   ├── Achievements
   ├── Certifications
   └── Academic Records
             │
             ▼
        Digital Portfolio
             │
             ▼
       Portfolio Approval
             │
             ▼
          Employers
```

Portfolio information is retrieved from related student records rather than duplicating the same information across multiple tables.

---

# 15. Main Functional Modules

| Module | Primary Tables |
|---|---|
| User Management | Users, Students, Instructors, Employers |
| Academic Monitoring | Subjects, Enrollments, Grades, Attendance |
| Technical Skills | Technical Skills, Student Skills |
| Project Management | Projects, Project Members, Project Evaluations |
| Achievements | Achievements, Certifications |
| Digital Portfolio | Portfolios, Portfolio Access |
| Academic Support | Academic Support |
| Career Opportunities | Job Opportunities, Applications |

---

# 16. Database Design Principles

### Data Integrity

Foreign keys maintain relationships between users, students, instructors, employers, academic records, projects, portfolios, and career opportunities.

### Normalization

Student, academic, skill, project, portfolio, and career information are stored in separate related tables to reduce unnecessary duplication.

### Role-Based Access

The `Users` table provides centralized authentication and role identification. Application-level authorization should determine which operations each role can perform.

### Referential Integrity

Records that depend on another entity should reference that entity through foreign keys.

### Security

Passwords must never be stored as plain text. The `password_hash` field should contain a secure password hash.

---

# 17. Suggested Database Technology

NextIT can use a relational database such as **MySQL** or **PostgreSQL** because the system requires structured relationships among students, academic records, technical skills, projects, portfolios, instructors, employers, and career opportunities.

The database should support:

- Primary and foreign keys
- Referential integrity
- Transactions
- Data validation
- Indexing
- Secure password storage
- Role-based data access
- Structured relational queries
