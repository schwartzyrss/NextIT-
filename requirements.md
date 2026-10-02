# NextIT Software Requirements Specification

## 1. Project Information

**Project Title:** NextIT: Track Your Progress, Build Your Future  
**Project Type:** Desktop Application  
**Subject:** Object-Oriented Programming  
**Group:** Group 9 – Desktop Proposals  
**SDG Alignment:** SDG 4 – Quality Education  
**Target:** SDG 4.4 – Relevant technical and vocational skills for employment, decent work, and entrepreneurship

---

# 2. System Overview

NextIT is a desktop application designed to centralize academic monitoring, technical competency tracking, project documentation, digital portfolios, academic support, and career opportunities for IT students.

The system connects students, instructors, and employers through a centralized platform. Students can monitor their academic progress and organize their projects, skills, achievements, and certifications. Instructors can manage academic records, attendance, technical evaluations, project evaluations, and academic support activities. Employers can review approved student portfolios and post internship or employment opportunities.

The system is intended to provide a more complete view of student development by combining academic performance with practical technical skills, project experience, achievements, and career preparation.

The requirements below are based on the submitted NextIT proposal and the accompanying database schema.

---

# 3. Problem Statement

IT students complete numerous subjects, laboratory activities, programming projects, group projects, and other academic activities throughout their college years. These records are often maintained separately, making it difficult to monitor overall development.

Students may have difficulty organizing previous projects and identifying the technical skills they have developed when preparing portfolios for internships or employment.

Instructors also need to monitor both academic performance and practical technical competencies. Grades alone may not clearly represent a student's technical abilities or areas that require improvement.

Students who need academic or technical support may also be difficult to identify early. Employers may likewise have difficulty finding students with relevant technical skills and project experience.

NextIT addresses these concerns by integrating academic monitoring, learning support, technical competency tracking, project documentation, digital portfolios, and career opportunities into one desktop application.

---

# 4. Objectives

## 4.1 General Objective

To develop a desktop-based system that centralizes academic performance, technical competencies, projects, achievements, academic support, digital portfolios, and career opportunities for IT students.

## 4.2 Specific Objectives

The system shall:

1. Allow students to view their academic progress.
2. Allow instructors to manage grades and attendance.
3. Record laboratory and academic performance.
4. Track student technical competencies.
5. Allow students to organize academic, programming, laboratory, and capstone projects.
6. Support documentation of individual contributions to group projects.
7. Allow instructors to record project evaluations.
8. Record student achievements and certifications.
9. Allow students to create and maintain digital portfolios.
10. Allow instructors to record review sessions, remedial activities, consultations, and additional exercises.
11. Allow employers to view approved student portfolios.
12. Allow employers to post internship and employment opportunities.
13. Allow students to view and apply for available opportunities.
14. Provide a centralized view of student development.

---

# 5. Target Users

## 5.1 Primary Users

### Students

Students use NextIT to:

- View academic progress.
- View grades.
- View attendance.
- View technical competency evaluations.
- Record projects.
- Record achievements.
- Record certifications.
- Maintain a digital portfolio.
- View internship and employment opportunities.
- Submit applications.

### Instructors

Instructors use NextIT to:

- Manage grades.
- Record attendance.
- Record laboratory performance.
- Evaluate technical competencies.
- Review student projects.
- Evaluate individual contributions to group projects.
- Record academic support activities.
- Provide feedback regarding student development.

### Capstone Project Advisers

Capstone advisers are treated as instructor/faculty users with project-related responsibilities, including project monitoring and evaluation.

### Department Faculty

Department faculty may access relevant academic and student development records according to their assigned permissions.

## 5.2 Secondary Users

### School Administrators

Administrators manage system-level accounts, academic configuration, and other administrative records.

### Internship Coordinators

Internship coordinators may use student and opportunity information to support internship placement.

### Career Placement Offices

Career placement personnel may use approved portfolio and opportunity information.

### Employers / Industry Partners

Employers can:

- View approved student portfolios.
- Review relevant technical skills and projects.
- Review achievements and certifications.
- Post internship opportunities.
- Post employment opportunities.
- Review applications.

---

# 6. User Roles and Permissions

| Function | Student | Instructor | Employer | Administrator |
|---|---:|---:|---:|---:|
| Login | Yes | Yes | Yes | Yes |
| View own academic records | Yes | Yes | No | Yes |
| Manage grades | No | Yes | No | Yes |
| Manage attendance | No | Yes | No | Yes |
| Manage technical evaluations | No | Yes | No | Yes |
| Manage own projects | Yes | Yes | No | Yes |
| Evaluate projects | No | Yes | No | Yes |
| Manage achievements | Yes | Yes | No | Yes |
| Manage certifications | Yes | Yes | No | Yes |
| Manage own portfolio | Yes | No | No | Yes |
| View approved portfolios | No | Yes | Yes | Yes |
| Record academic support | No | Yes | No | Yes |
| Post opportunities | No | No | Yes | Yes |
| View opportunities | Yes | Yes | Yes | Yes |
| Apply to opportunities | Yes | No | No | Yes |
| Manage users | No | No | No | Yes |
| Manage subjects | No | No | No | Yes |
| Manage technical skills | No | Yes | No | Yes |

---

# 7. Functional Requirements

## FR-01 Authentication and User Management

The system shall provide authentication for registered users.

The system shall:

- Require a username/email and password.
- Authenticate the user's credentials.
- Identify the user's role.
- Open the appropriate dashboard based on the role.
- Prevent unauthorized users from accessing protected modules.
- Store passwords using secure hashing rather than plain text.
- Allow administrators to manage user accounts.

---

## FR-02 Student Profile

The system shall maintain student information including:

- Student number
- Name
- Program
- Year level
- Section
- Enrollment status
- Associated user account

Students shall be able to view their profile information.

---

## FR-03 Instructor Profile

The system shall maintain instructor information including:

- Employee number
- Name
- Department
- Specialization
- Associated user account

---

## FR-04 Employer Profile

The system shall maintain employer information including:

- Company name
- Industry
- Contact person
- Contact number
- Address
- Associated user account

---

# 8. Academic Monitoring Requirements

## FR-05 Subject Management

The system shall maintain subjects with:

- Subject code
- Subject name
- Units
- Description

Administrators shall be able to add, update, and manage subjects.

---

## FR-06 Enrollment Management

The system shall associate students with their enrolled subjects.

Enrollment records shall include:

- Student
- Subject
- Academic year
- Semester
- Enrollment status

---

## FR-07 Grade Management

Instructors shall be able to record academic performance.

Grade records shall include:

- Student enrollment
- Instructor
- Activity name
- Activity type
- Score
- Maximum score
- Grade
- Remarks
- Date recorded

Activity types may include:

- Examination
- Quiz
- Laboratory
- Project
- Other academic activities

Students shall be able to view their own grades.

---

## FR-08 Attendance Management

Instructors shall be able to record attendance.

Attendance records shall include:

- Student enrollment
- Date
- Status
- Remarks
- Recording instructor

Attendance statuses may include:

- Present
- Absent
- Late
- Excused

Students shall be able to view their attendance records.

---

## FR-09 Academic Progress

The system shall provide students with a centralized view of their academic progress.

The progress view should include:

- Subjects
- Grades
- Attendance
- Laboratory/project performance
- Relevant instructor remarks

---

# 9. Technical Competency Requirements

## FR-10 Technical Skill Management

The system shall maintain technical skills grouped into categories such as:

- Programming
- Networking
- Database Management
- Cybersecurity
- Web Development
- Software Development

Each skill shall have:

- Skill name
- Category
- Description

---

## FR-11 Student Skill Tracking

The system shall associate students with technical skills.

Skill records shall include:

- Student
- Technical skill
- Proficiency level
- Assessment score
- Assessing instructor
- Assessment date
- Remarks

Suggested proficiency levels:

- Beginner
- Intermediate
- Advanced
- Expert

Students shall be able to view their assessed skills.

---

# 10. Project Management Requirements

## FR-12 Project Management

Students shall be able to record academic and technical projects.

Project information shall include:

- Project title
- Related subject
- Project type
- Description
- Technologies used
- Repository URL
- Demo URL
- Project date
- Status

Project types may include:

- Laboratory
- Academic project
- Programming project
- Capstone project
- Personal project

---

## FR-13 Group Project Management

The system shall support projects with multiple student members.

Each project member record shall include:

- Student
- Project
- Role
- Contribution description

---

## FR-14 Project Evaluation

Instructors shall be able to evaluate individual student contributions.

Evaluations may include:

- Contribution score
- Technical score
- Teamwork score
- Overall score
- Feedback
- Evaluation date

---

# 11. Achievement and Certification Requirements

## FR-15 Achievement Management

Students shall be able to record achievements.

Achievement records shall include:

- Title
- Description
- Organization
- Achievement date
- Verification URL

---

## FR-16 Certification Management

Students shall be able to record certifications.

Certification records shall include:

- Certification name
- Issuing organization
- Issue date
- Expiration date
- Credential URL

---

# 12. Digital Portfolio Requirements

## FR-17 Portfolio Creation

Students shall be able to create and maintain a digital portfolio.

The portfolio shall contain:

- Student information
- Headline
- Biography
- Career goal
- Technical skills
- Projects
- Achievements
- Certifications
- Relevant academic accomplishments

---

## FR-18 Portfolio Visibility

The system shall support portfolio visibility states such as:

- Private
- Approved
- Public

Students shall control their portfolio content while approved portfolio access shall be required before employers can view restricted student portfolio information.

---

## FR-19 Portfolio Access

The system shall manage employer access to student portfolios.

Access records shall include:

- Portfolio
- Employer
- Access status
- Date granted

Access statuses may include:

- Pending
- Approved
- Revoked

Employers shall only be able to view portfolios they are authorized to access.

---

# 13. Academic Support Requirements

## FR-20 Academic Support Records

Instructors shall be able to record additional academic support activities.

Support activities may include:

- Review sessions
- Remedial activities
- Consultations
- Additional exercises

Records shall include:

- Student
- Instructor
- Subject
- Support type
- Date
- Reason
- Action taken
- Outcome
- Remarks

---

# 14. Career Opportunity Requirements

## FR-21 Opportunity Management

Employers shall be able to create internship and employment opportunities.

Opportunity information shall include:

- Position title
- Opportunity type
- Description
- Required skills
- Location
- Application deadline
- Status
- Posting date

Opportunity types:

- Internship
- Employment

Opportunity statuses:

- Open
- Closed
- Archived

---

## FR-22 Opportunity Browsing

Students shall be able to:

- View available opportunities.
- Review descriptions.
- Review required skills.
- Review locations.
- Review application deadlines.
- Submit applications.

---

## FR-23 Application Management

The system shall record student applications.

Application records shall include:

- Student
- Opportunity
- Application date
- Application status
- Remarks

Application statuses may include:

- Pending
- Reviewed
- Shortlisted
- Accepted
- Rejected

---

# 15. Dashboard Requirements

## 15.1 Student Dashboard

The student dashboard should provide an overview of:

- Academic progress
- Attendance
- Technical skills
- Recent projects
- Achievements
- Certifications
- Portfolio status
- Available career opportunities

## 15.2 Instructor Dashboard

The instructor dashboard should provide:

- Assigned students/classes
- Grade records
- Attendance records
- Technical evaluations
- Project evaluations
- Academic support records
- Student development information

## 15.3 Employer Dashboard

The employer dashboard should provide:

- Company profile
- Posted opportunities
- Approved student portfolios
- Student applications
- Opportunity status

## 15.4 Administrator Dashboard

The administrator dashboard should provide:

- User management
- Subject management
- Technical skill management
- System configuration
- Basic system statistics

---

# 16. Search and Filtering Requirements

The system should provide search and filtering where appropriate.

Users should be able to search:

- Students
- Subjects
- Projects
- Technical skills
- Achievements
- Certifications
- Career opportunities

Employers should be able to filter approved portfolios based on relevant information such as:

- Technical skill
- Skill category
- Project experience
- Certification
- Achievement

---

# 17. Validation Requirements

The system shall validate user input before saving records.

Examples include:

- Required fields cannot be empty.
- Email addresses must use a valid format.
- Scores cannot exceed the maximum score.
- Scores cannot be negative.
- Dates must use valid date formats.
- Application deadlines must be valid dates.
- Duplicate usernames and emails shall not be allowed.
- Duplicate student numbers shall not be allowed.
- Duplicate subject codes shall not be allowed.

---

# 18. Security Requirements

The system shall:

1. Hash passwords before storing them.
2. Restrict modules according to user roles.
3. Prevent students from modifying instructor-recorded grades and attendance.
4. Prevent employers from viewing unapproved private portfolios.
5. Prevent users from accessing another user's private records without authorization.
6. Validate user input before database operations.
7. Use parameterized database queries.
8. Provide logout functionality.
9. Handle invalid login attempts safely.
10. Avoid storing sensitive credentials in source code.

---

# 19. Non-Functional Requirements

## NFR-01 Usability

The application should provide a clear and consistent graphical interface suitable for students, instructors, administrators, and employers.

## NFR-02 Performance

Normal operations such as login, navigation, searching, and viewing records should respond without unnecessary delay under normal desktop usage.

## NFR-03 Reliability

The application should handle invalid input and database errors without crashing.

## NFR-04 Maintainability

The application should use an object-oriented architecture with clearly separated models, services, controllers/view models, database access, and user interface components.

## NFR-05 Scalability

The database design should allow additional students, instructors, employers, subjects, skills, projects, and opportunities without requiring structural redesign.

## NFR-06 Security

Authentication, authorization, password hashing, and parameterized database operations shall be implemented.

## NFR-07 Compatibility

The application shall run as a Java desktop application using JavaFX.

---

# 20. Proposed Technical Architecture

The application should use a layered architecture:

```text
┌───────────────────────────────┐
│          JavaFX UI            │
│  Views / Components / CSS     │
└───────────────┬───────────────┘
                │
┌───────────────▼───────────────┐
│   Controllers / ViewModels    │
│  User Interaction & State     │
└───────────────┬───────────────┘
                │
┌───────────────▼───────────────┐
│          Services             │
│ Business Logic / Validation   │
└───────────────┬───────────────┘
                │
┌───────────────▼───────────────┐
│       Repository / DAO        │
│       Database Access         │
└───────────────┬───────────────┘
                │
┌───────────────▼───────────────┐
│       Relational DB           │
│     MySQL / PostgreSQL        │
└───────────────────────────────┘
```

---

# 21. Database Requirements

The implementation should follow the NextIT database schema.

Core entities include:

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

The application shall use foreign-key relationships and transactions where appropriate.

---

# 22. Expected Main Screens

### Common

1. Login
2. Logout
3. Profile

### Student

1. Student Dashboard
2. Academic Progress
3. Grades
4. Attendance
5. Technical Skills
6. Projects
7. Achievements
8. Certifications
9. Portfolio
10. Career Opportunities
11. Applications

### Instructor

1. Instructor Dashboard
2. Students
3. Subjects
4. Grades
5. Attendance
6. Technical Evaluations
7. Projects
8. Project Evaluations
9. Academic Support

### Employer

1. Employer Dashboard
2. Company Profile
3. Student Portfolios
4. Career Opportunities
5. Applications

### Administrator

1. Administrator Dashboard
2. Users
3. Students
4. Instructors
5. Employers
6. Subjects
7. Technical Skills
8. System Configuration

---

# 23. Acceptance Criteria

The system shall be considered functionally complete when:

- Users can securely log in and are redirected according to their role.
- Students can view their academic progress.
- Instructors can record and manage grades.
- Instructors can record attendance.
- Technical competencies can be recorded and evaluated.
- Students can create and manage projects.
- Group project membership and contributions can be recorded.
- Instructors can evaluate project contributions.
- Students can record achievements and certifications.
- Students can create a digital portfolio.
- Employers can access approved portfolios.
- Employers can post internship and employment opportunities.
- Students can view opportunities and submit applications.
- Instructors can record academic support activities.
- Unauthorized users cannot access restricted records.
- Data is stored in the relational database according to the defined schema.
- The application can handle validation and database errors without crashing.

---

# 24. Scope Summary

NextIT focuses on connecting academic development, technical competency development, project experience, student achievements, learning support, digital portfolios, and career opportunities.

The system is not intended to replace a complete institutional student information system. Its primary purpose is to provide an integrated view of IT student development and connect academic progress with practical skills and career preparation.
