# NextIT

**NextIT: Track Your Progress, Build Your Future** — a JavaFX desktop application for IT students, instructors, employers, and administrators.

## Features

### Student

- Dashboard with subject, grade, attendance, skill, project, portfolio, and opportunity statistics
- Academic progress: subjects, grades, attendance, project performance, and academic support records
- Technical skills assessed by instructors
- Full project CRUD for owners, group membership with shared project visibility, repository/demo links
- Achievements and certifications CRUD
- Digital portfolio editor with Private, Public, and approval-requested visibility, preview, and employer access list
- Opportunity search/filtering, application submission, and application status tracking
- Personal profile screen

### Instructor

- Dashboard statistics
- Student and subject directories with search
- Grade recording with automatic percentage calculation and validation
- Attendance recording with status validation
- Technical skill assessments
- Technical skill catalogue management shared with administrators
- Student project review with group member contributions and evaluations
- Project evaluations with contribution, technical, teamwork, and overall scores
- Academic support records
- Achievement and certification records for students, with an explicit acting-student selector
- Opportunity browsing (read-only) so instructors can advise students on current openings
- Read-only browser for approved and public student portfolios
- Personal profile screen

### Employer

- Dashboard statistics
- Company profile editing
- Internship and employment opportunity CRUD, search, and closing
- Application review with status workflow (Pending, Reviewed, Shortlisted, Rejected, Accepted)
- Portfolio search across approved and explicitly shared student portfolios plus public portfolios, with full portfolio detail

### Administrator

- Dashboard statistics
- User account CRUD with BCrypt password hashing, password resets, and automatic role profiles
- Subject and technical skill catalogue CRUD with duplicate validation
- Enrollment management linking students to subjects and academic terms
- Grade and attendance management with an explicit acting-instructor selector for accurate audit trails
- Technical skill, project, project evaluation, and academic support management
- Achievement and certification records for any student
- Student, instructor, and employer profile management
- Project, opportunity, and application management across all owners
- Portfolio review with full student portfolio detail, approval, employer access granting, and access revocation
- Read-only system diagnostics with connection testing and database row counts
- Personal profile screen

### Cross-cutting

- Role-based navigation and session handling
- Layered architecture: model, repository, service, controller, security, util, exception
- Parameterized SQL through JDBC
- Input validation (required fields, formats, ranges, and database column lengths) and friendly database error handling
- Search and filtering on the main data tables
- Dashboard charts for grade trends, attendance composition, activity performance, and application/enrollment/portfolio status
- Consistent empty-state placeholders, password visibility toggle, and responsive scrollable navigation

## Technology Stack

- Java 21 (LTS)
- JavaFX 21 (FXML + CSS)
- Maven
- MySQL 8
- JDBC, jBCrypt, SLF4J/Logback, JUnit 5

No Spring Boot or web UI is used; this is a native desktop application.

## Project Structure

```text
NextIT-/
├── pom.xml
├── .env.example
├── requirements.md
├── schema.md
├── database/
│   ├── schema.sql
│   └── seed.sql
└── src/
    ├── main/
    │   ├── java/com/nextit/
    │   │   ├── Main.java
    │   │   ├── config/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   ├── service/
    │   │   ├── controller/
    │   │   ├── security/
    │   │   ├── util/
    │   │   └── exception/
    │   └── resources/com/nextit/
    │       ├── view/
    │       ├── css/
    │       └── assets/
    └── test/java/com/nextit/
```

## Database Setup

1. Create the database and tables:

   ```bash
   mysql -u root -p < database/schema.sql
   ```

2. Load demo data:

   ```bash
   mysql -u root -p < database/seed.sql
   ```

The seed script resolves generated primary keys by username/subject code, so it does not depend on auto-increment values starting at 1. Run it once against a fresh schema.

## Configuration

Copy `.env.example` to `.env` (or set environment variables):

```text
NEXTIT_DB_URL=jdbc:mysql://localhost:3306/nextit?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
NEXTIT_DB_USER=root
NEXTIT_DB_PASSWORD=yourpassword
```

The application reads `NEXTIT_DB_URL`, `NEXTIT_DB_USER`, and `NEXTIT_DB_PASSWORD` in this order: process environment, JVM system properties, then the optional `.env` file in the working directory. This lets the app work from an IDE or a terminal without hard-coding credentials.

## How to Run

```bash
mvn clean javafx:run
```

On Windows with the Maven wrapper and a local JDK:

```powershell
$env:JAVA_HOME='C:\path\to\jdk-21'
.\mvnw.cmd clean javafx:run
```

## Demo Accounts (password: `password123`)

| Username     | Role       |
|--------------|------------|
| student1     | STUDENT    |
| student2     | STUDENT    |
| instructor1  | INSTRUCTOR |
| employer1    | EMPLOYER   |
| admin1       | ADMIN      |

## Tests

```bash
mvn clean test
mvn clean package
```

The automated tests cover password hashing, validation, session behaviour, grade and attendance calculations, portfolio visibility rules, project evaluation averages, and database-backed ownership/authorization boundaries for achievements, certifications, projects, portfolios, opportunities, applications, enrollments, grades, attendance, and technical-skill assessments.

The read-only schema integration tests check every documented table's columns, constraints, unique keys, and foreign-key relationships. By default they start an embedded MariaDB instance on a free port and load `database/schema.sql`, so no local MySQL installation is required. To run them against an existing database instead, set `NEXTIT_TEST_DB_URL` to a test database already initialized with `database/schema.sql`; optionally set `NEXTIT_TEST_DB_USER` and `NEXTIT_TEST_DB_PASSWORD`.
