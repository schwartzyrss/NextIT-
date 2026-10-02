# OpenCode Prompt: Build NextIT as a JavaFX Desktop Application

You are an experienced Java developer and software architect.

Build a complete, functional JavaFX desktop application called **NextIT: Track Your Progress, Build Your Future**.

NextIT is an Object-Oriented Programming project for Group 9. It is a desktop system for IT students, instructors, administrators, and employers that centralizes academic progress, technical skills, projects, achievements, certifications, academic support, digital portfolios, internships, employment opportunities, and applications.

The implementation must be a real working application, not a static mockup.

---

## 1. Primary Goal

Create a polished JavaFX desktop application that implements the NextIT requirements and database schema.

The application must provide:

- Authentication
- Role-based access
- Student academic monitoring
- Grade management
- Attendance management
- Technical skill tracking
- Project management
- Group project contribution tracking
- Project evaluation
- Achievement management
- Certification management
- Digital portfolio management
- Portfolio approval/access
- Academic support tracking
- Internship and employment opportunities
- Student applications
- Search and filtering
- Validation
- Database persistence

---

# 2. Technology Requirements

Use:

- Java 21 or newer LTS
- JavaFX
- Maven
- FXML where appropriate
- CSS for styling
- MySQL or PostgreSQL
- JDBC
- BCrypt or another appropriate password hashing library
- SLF4J/Logback or another simple logging solution

Do not build this as a web application.

Do not use Spring Boot.

Do not use React, Angular, Electron, or other web UI frameworks.

This must be a native JavaFX desktop application.

---

# 3. Project Architecture

Use a clean layered architecture:

```text
src/main/java/
└── com/nextit/
    ├── Main.java
    ├── config/
    ├── model/
    ├── repository/
    ├── service/
    ├── controller/
    ├── security/
    ├── util/
    └── exception/

src/main/resources/
└── com/nextit/
    ├── view/
    ├── css/
    └── assets/
```

Use clear separation of responsibilities.

### Model

Represent database entities using Java classes.

Examples:

- User
- Student
- Instructor
- Employer
- Subject
- Enrollment
- Grade
- Attendance
- TechnicalSkill
- StudentSkill
- Project
- ProjectMember
- ProjectEvaluation
- Achievement
- Certification
- Portfolio
- PortfolioAccess
- AcademicSupport
- JobOpportunity
- Application

### Repository / DAO

Handle database operations.

Examples:

```text
UserRepository
StudentRepository
InstructorRepository
EmployerRepository
SubjectRepository
EnrollmentRepository
GradeRepository
AttendanceRepository
TechnicalSkillRepository
StudentSkillRepository
ProjectRepository
ProjectMemberRepository
ProjectEvaluationRepository
AchievementRepository
CertificationRepository
PortfolioRepository
PortfolioAccessRepository
AcademicSupportRepository
JobOpportunityRepository
ApplicationRepository
```

### Service

Business logic belongs here.

Examples:

```text
AuthenticationService
StudentService
AcademicService
SkillService
ProjectService
PortfolioService
CareerService
AcademicSupportService
```

### Controller

Controllers should handle JavaFX UI interactions and delegate business logic to services.

Do not put SQL directly inside JavaFX controllers.

---

# 4. Database

Implement the database according to the NextIT schema.

Required tables:

```text
users
students
instructors
employers
subjects
enrollments
grades
attendance
technical_skills
student_skills
projects
project_members
project_evaluations
achievements
certifications
portfolios
portfolio_access
academic_support
job_opportunities
applications
```

Use:

- Primary keys
- Foreign keys
- Unique constraints
- NOT NULL constraints
- Appropriate indexes
- Transactions for multi-step operations

Create a database initialization SQL script:

```text
database/schema.sql
```

Also create:

```text
database/seed.sql
```

with realistic demo data.

Do not hard-code application data in Java if it belongs in the database.

---

# 5. Authentication

Create a professional login screen.

Fields:

- Username/email
- Password

Features:

- Login button
- Password visibility toggle if appropriate
- Validation
- Error message for invalid credentials
- Logout
- Session management

After login, redirect to the appropriate dashboard based on role.

Roles:

```text
STUDENT
INSTRUCTOR
EMPLOYER
ADMIN
```

Never store plain-text passwords.

Use password hashing.

---

# 6. Main UI Design

Create a modern, clean academic/professional desktop interface.

The visual design should feel like a modern student information and career management platform.

Use:

- Sidebar navigation
- Top header
- Dashboard cards
- Tables
- Forms
- Dialogs
- Tabs where appropriate
- Consistent spacing
- Clean typography
- Responsive JavaFX layouts

Avoid excessive visual decoration.

Use JavaFX CSS instead of embedding styling throughout Java code.

Create reusable components where possible.

---

# 7. Navigation

Create a reusable dashboard shell.

Example:

```text
------------------------------------------------------------
| NextIT | Dashboard                         User / Logout |
------------------------------------------------------------
| Sidebar        |                                      |
|                |                                      |
| Dashboard      |          Main Content                |
| Academic       |                                      |
| Skills         |                                      |
| Projects       |                                      |
| Portfolio      |                                      |
| Opportunities  |                                      |
| Applications   |                                      |
| Settings       |                                      |
------------------------------------------------------------
```

Navigation options must change based on the logged-in user's role.

---

# 8. Student Module

Create a Student Dashboard.

Show useful summary cards such as:

- Current subjects
- Average grade
- Attendance rate
- Technical skills
- Active projects
- Achievements
- Portfolio status
- Available opportunities

Do not expose instructor-only management functions to students.

## Academic Progress

Students should be able to view:

- Subjects
- Grades
- Activity performance
- Attendance
- Instructor remarks

Use tables and summary information.

## Technical Skills

Display:

- Skill
- Category
- Proficiency
- Assessment score
- Assessed by
- Assessment date
- Remarks

Use progress indicators or badges for proficiency levels.

## Projects

Students can:

- Add projects
- Edit projects
- Delete their own projects where appropriate
- View project details
- Add technologies
- Add repository URL
- Add demo URL
- Add group members

## Achievements

Students can add:

- Title
- Description
- Organization
- Date
- Verification URL

## Certifications

Students can add:

- Certification
- Issuing organization
- Issue date
- Expiration date
- Credential URL

## Portfolio

Create a portfolio editor.

The portfolio should display:

```text
Student Profile
Career Goal
Biography
Technical Skills
Projects
Achievements
Certifications
Academic Highlights
```

Provide a portfolio preview.

---

# 9. Instructor Module

Create an Instructor Dashboard.

Show:

- Assigned students/classes
- Recent grades
- Attendance records
- Pending evaluations
- Student projects
- Academic support activity
- Technical skill evaluations

## Grade Management

Provide:

- Student selection
- Subject selection
- Activity name
- Activity type
- Score
- Maximum score
- Grade
- Remarks

Validate:

```text
score >= 0
score <= max_score
```

## Attendance

Provide:

- Student
- Subject
- Date
- Status
- Remarks

Statuses:

```text
Present
Absent
Late
Excused
```

## Technical Evaluation

Allow instructors to assess:

- Technical skill
- Proficiency
- Score
- Remarks

## Project Evaluation

Allow instructors to evaluate individual group members using:

- Contribution score
- Technical score
- Teamwork score
- Overall score
- Feedback

## Academic Support

Allow instructors to record:

- Student
- Subject
- Support type
- Date
- Reason
- Action taken
- Outcome
- Remarks

Support types:

```text
Review
Remedial
Consultation
Additional Exercise
```

---

# 10. Employer Module

Create an Employer Dashboard.

Show:

- Posted opportunities
- Active opportunities
- Applications
- Approved student portfolios

## Student Portfolio Search

Employers can search approved portfolios.

Allow filtering by:

- Technical skill
- Skill category
- Project experience
- Certification
- Achievement

Employers must not see private or unapproved portfolio information.

## Opportunities

Employers can:

- Create internship opportunities
- Create employment opportunities
- Edit their opportunities
- Close opportunities
- View applications

Opportunity fields:

```text
Title
Type
Description
Required Skills
Location
Deadline
Status
```

Types:

```text
Internship
Employment
```

## Applications

Employers can view applications for their own opportunities.

Show:

- Student
- Opportunity
- Application date
- Status
- Remarks

---

# 11. Administrator Module

Create an Administrator Dashboard.

Provide management for:

- Users
- Students
- Instructors
- Employers
- Subjects
- Technical skills
- System configuration

Administrators should have broad management access but should still use services and repositories rather than bypassing the architecture.

---

# 12. Search and Filtering

Implement reusable search fields and filters.

Examples:

```text
Student search
Project search
Subject search
Skill search
Opportunity search
Portfolio search
Application search
```

Filtering should happen through repository/service queries where practical rather than loading huge datasets unnecessarily.

---

# 13. Validation

Every form must validate input before saving.

Examples:

- Required fields cannot be empty.
- Email format must be valid.
- Scores cannot be negative.
- Scores cannot exceed maximum score.
- Dates must be valid.
- Duplicate usernames must be rejected.
- Duplicate emails must be rejected.
- Duplicate student numbers must be rejected.
- Duplicate subject codes must be rejected.
- Application deadline must be valid.

Show clear user-friendly validation messages.

---

# 14. Error Handling

The application must not crash because of normal user mistakes.

Handle:

- Database connection failures
- SQL errors
- Invalid input
- Missing records
- Authentication failures
- Duplicate records
- Unauthorized operations

Use centralized exception handling where appropriate.

Show user-friendly JavaFX alerts.

Do not expose raw SQL stack traces to users.

Log technical errors for debugging.

---

# 15. Security

Implement:

- Password hashing
- Role-based authorization
- Parameterized SQL queries
- Session handling
- Logout
- Restricted portfolio access
- Restricted academic record modification
- Input validation

Never concatenate user input directly into SQL queries.

Do not hard-code production passwords or database credentials.

Use configuration/environment variables for database credentials.

Provide an example configuration file such as:

```text
.env.example
```

Do not commit real credentials.

---

# 16. Seed Data

Create demo data so the application can be tested immediately.

Include:

### Users

At least:

```text
1 student
1 instructor
1 employer
1 administrator
```

Use clearly documented demo credentials in the README only.

### Student Data

Create realistic:

- Subjects
- Enrollments
- Grades
- Attendance
- Technical skills
- Projects
- Achievements
- Certifications
- Portfolio

### Instructor Data

Include:

- Grade records
- Attendance
- Technical evaluations
- Project evaluations
- Academic support records

### Employer Data

Include:

- Company
- Approved portfolio access
- Internship opportunity
- Employment opportunity
- Example application

---

# 17. JavaFX Requirements

Use FXML for complex views where appropriate.

Create reusable UI components for:

- Sidebar
- Header
- Dashboard cards
- Data tables
- Forms
- Confirmation dialogs
- Alerts

Use JavaFX properties and observable collections appropriately.

Do not use one giant controller for the entire application.

Each major module should have its own controller.

---

# 18. Object-Oriented Programming Requirements

Because this is an Object-Oriented Programming project, demonstrate proper OOP principles.

Use:

- Encapsulation
- Abstraction
- Inheritance where appropriate
- Polymorphism where appropriate
- Interfaces
- Composition
- Separation of concerns

Avoid unnecessary inheritance just for demonstration.

Use interfaces for repository/service abstractions where useful.

Example:

```java
public interface StudentRepository {
    Optional<Student> findById(int id);
    List<Student> findAll();
    void save(Student student);
    void update(Student student);
    void delete(int id);
}
```

Then provide a JDBC implementation.

---

# 19. Recommended Package Structure

Use a structure similar to:

```text
com.nextit
├── Main.java
├── config
│   ├── DatabaseConfig.java
│   └── AppConfig.java
├── model
│   ├── User.java
│   ├── Student.java
│   ├── Instructor.java
│   ├── Employer.java
│   ├── Subject.java
│   ├── Enrollment.java
│   ├── Grade.java
│   ├── Attendance.java
│   ├── TechnicalSkill.java
│   ├── StudentSkill.java
│   ├── Project.java
│   ├── ProjectMember.java
│   ├── ProjectEvaluation.java
│   ├── Achievement.java
│   ├── Certification.java
│   ├── Portfolio.java
│   ├── PortfolioAccess.java
│   ├── AcademicSupport.java
│   ├── JobOpportunity.java
│   └── Application.java
├── repository
├── service
├── controller
├── security
├── util
└── exception
```

Resources:

```text
src/main/resources
└── com/nextit
    ├── view
    │   ├── Login.fxml
    │   ├── student
    │   ├── instructor
    │   ├── employer
    │   └── admin
    ├── css
    │   └── application.css
    └── assets
```

---

# 20. Database Connection

Create a centralized database connection utility.

Do not create a new unmanaged connection in every controller.

Prefer a repository/DAO approach with controlled connection management.

Use prepared statements.

Close resources safely using try-with-resources.

---

# 21. UX Requirements

The application should feel cohesive.

Use consistent:

- Button styles
- Table styles
- Form spacing
- Typography
- Navigation
- Dialogs
- Empty states
- Error messages

Every major screen should provide:

- Clear title
- Description or contextual information where useful
- Primary action
- Search/filter where appropriate
- Refresh/reload capability where appropriate

Tables should support useful column sizing and scrolling.

Forms should use appropriate controls such as:

```text
TextField
PasswordField
ComboBox
DatePicker
TextArea
CheckBox
TableView
ListView
ProgressBar
```

---

# 22. Dashboard Visualization

Use simple JavaFX charts where they add actual value.

Student dashboard may show:

- Grade trend
- Attendance summary
- Skill distribution

Instructor dashboard may show:

- Student performance overview
- Attendance overview
- Evaluation status

Avoid unnecessary charts.

---

# 23. Navigation and Session Rules

After login:

```text
STUDENT     -> Student Dashboard
INSTRUCTOR  -> Instructor Dashboard
EMPLOYER    -> Employer Dashboard
ADMIN       -> Admin Dashboard
```

The current authenticated user should be stored in an application session object.

On logout:

1. Clear the session.
2. Return to login.
3. Prevent access to protected screens.

---

# 24. Important Business Rules

Implement these rules:

1. Students can only manage their own personal projects, achievements, certifications, and portfolio.
2. Students cannot modify instructor-recorded grades.
3. Students cannot modify instructor-recorded attendance.
4. Instructors can manage records for students under their academic responsibility.
5. Employers can only manage opportunities they created.
6. Employers can only view portfolios they are authorized to access.
7. Students can only apply to available opportunities.
8. Closed or archived opportunities should not accept new applications.
9. Employers should only manage applications belonging to their own opportunities.
10. Administrators can manage system-level records.
11. Portfolio visibility must be respected.
12. Foreign-key relationships must be preserved.

---

# 25. Deliverables

Create a complete project containing:

```text
README.md
requirements.md
schema.md

pom.xml

database/
├── schema.sql
└── seed.sql

src/
└── main/
    ├── java/
    └── resources/
```

The README must explain:

- Project overview
- Features
- Requirements
- Technology stack
- Database setup
- Configuration
- How to run
- Demo accounts
- Project structure

---

# 26. Development Process

Before implementation:

1. Inspect the existing repository.
2. Determine whether a Java/Maven project already exists.
3. Preserve useful existing work if present.
4. Do not unnecessarily overwrite unrelated files.
5. Create the project structure.
6. Implement the database schema.
7. Implement database connection.
8. Implement models.
9. Implement repositories.
10. Implement services.
11. Implement authentication.
12. Implement JavaFX views.
13. Implement role-specific dashboards.
14. Implement CRUD operations.
15. Implement validation and authorization.
16. Add seed data.
17. Test the application.
18. Fix compilation errors.
19. Fix runtime errors.
20. Update README.

---

# 27. Testing Requirements

Create at minimum:

- Authentication tests
- Password verification tests
- Repository/database tests where practical
- Validation tests
- Role authorization tests
- Core service tests

Manually verify:

- Login for each role
- Student dashboard
- Instructor dashboard
- Employer dashboard
- Admin dashboard
- CRUD operations
- Portfolio access
- Opportunity creation
- Application submission
- Logout

Run:

```bash
mvn clean test
```

and:

```bash
mvn clean package
```

Fix all compilation and test failures before considering the project complete.

---

# 28. Implementation Quality

Do not create fake buttons that do nothing.

Every visible CRUD action should perform its intended operation.

Do not use placeholder text such as:

```text
TODO
Coming soon
Feature unavailable
```

for required functionality.

Do not use mock arrays as the permanent data source.

Persist required application data in the database.

Do not put the entire application in one Java class.

Do not put SQL statements directly inside JavaFX event handlers.

Keep the implementation understandable for an Object-Oriented Programming academic project.

---

# 29. Final Verification

Before finishing, verify:

```text
[ ] Maven project builds
[ ] JavaFX launches
[ ] Database connects
[ ] Schema initializes
[ ] Seed data loads
[ ] Login works
[ ] Student role works
[ ] Instructor role works
[ ] Employer role works
[ ] Admin role works
[ ] Role restrictions work
[ ] Academic records work
[ ] Technical skills work
[ ] Projects work
[ ] Project evaluations work
[ ] Achievements work
[ ] Certifications work
[ ] Portfolio works
[ ] Portfolio access works
[ ] Academic support works
[ ] Opportunities work
[ ] Applications work
[ ] Validation works
[ ] Error handling works
[ ] Logout works
[ ] Tests pass
[ ] README is complete
```

Build the system incrementally, compile frequently, and resolve errors before moving to the next major module.

The final result should be a coherent, functional JavaFX desktop application that implements the NextIT requirements rather than merely presenting a visual prototype.
