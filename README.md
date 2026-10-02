# NextIT

**NextIT: Track Your Progress, Build Your Future** — a JavaFX desktop application for IT students, instructors, employers, and administrators.

## Features (planned)

- Authentication with role-based dashboards (Student, Instructor, Employer, Admin)
- Academic monitoring: subjects, enrollments, grades, attendance
- Technical skill tracking
- Project management with group contributions and instructor evaluations
- Achievements and certifications
- Digital portfolio with approval/access control
- Academic support records
- Internship/employment opportunities and applications

## Technology Stack

- Java 21 (LTS)
- JavaFX 21 (FXML + CSS)
- Maven
- MySQL 8
- JDBC, jBCrypt, SLF4J/Logback, JUnit 5

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

## Configuration

Copy `.env.example` to `.env` (or set environment variables):

```text
NEXTIT_DB_URL=jdbc:mysql://localhost:3306/nextit?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
NEXTIT_DB_USER=root
NEXTIT_DB_PASSWORD=yourpassword
```

## How to Run

```bash
mvn clean javafx:run
```

## Demo Accounts (password: `password123`)

| Username     | Role       |
|--------------|------------|
| student1     | STUDENT    |
| instructor1  | INSTRUCTOR |
| employer1    | EMPLOYER   |
| admin1       | ADMIN      |

## Tests

```bash
mvn clean test
mvn clean package
```
