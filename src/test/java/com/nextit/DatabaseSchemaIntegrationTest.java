package com.nextit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseSchemaIntegrationTest {

    private static DatabaseTestServer database;

    private record ForeignKey(String table, String column, String referencedTable, String referencedColumn) {}
    private record Column(String type, int size, boolean nullable) {}
    private record TableColumn(String type, int size, int scale, boolean nullable) {}
    private record TableSchema(Map<String, TableColumn> columns, String primaryKey,
                               Set<Set<String>> uniqueConstraints, String autoIncrementColumn) {}

    @Test
    void usersTableMatchesItsDocumentedColumnsAndConstraints() throws SQLException {
        Map<String, Column> actualColumns = new HashMap<>();
        Set<String> primaryKeyColumns = new HashSet<>();
        Set<String> uniqueColumns = new HashSet<>();
        boolean userIdAutoIncrement = false;

        try (Connection connection = openTestConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, "users", null)) {
                while (columns.next()) {
                    String name = normalize(columns.getString("COLUMN_NAME"));
                    String type = normalize(columns.getString("TYPE_NAME"));
                    if (type.startsWith("enum(")) type = "enum";
                    actualColumns.put(name, new Column(type, columns.getInt("COLUMN_SIZE"),
                            columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable));
                    if (name.equals("user_id")) {
                        userIdAutoIncrement = "YES".equalsIgnoreCase(columns.getString("IS_AUTOINCREMENT"));
                    }
                }
            }
            try (ResultSet keys = metadata.getPrimaryKeys(connection.getCatalog(), null, "users")) {
                while (keys.next()) primaryKeyColumns.add(normalize(keys.getString("COLUMN_NAME")));
            }
            try (ResultSet indexes = metadata.getIndexInfo(connection.getCatalog(), null, "users", true, false)) {
                while (indexes.next()) {
                    String column = indexes.getString("COLUMN_NAME");
                    if (column != null && !indexes.getBoolean("NON_UNIQUE")) uniqueColumns.add(normalize(column));
                }
            }
        }

        Map<String, Column> expectedColumns = Map.of(
                "user_id", new Column("int", 10, false),
                "username", new Column("varchar", 50, false),
                "password_hash", new Column("varchar", 255, false),
                "email", new Column("varchar", 100, false),
                "role", new Column("enum", 0, false),
                "first_name", new Column("varchar", 50, false),
                "last_name", new Column("varchar", 50, false),
                "created_at", new Column("datetime", 0, false),
                "updated_at", new Column("datetime", 0, false));

        assertEquals(expectedColumns.keySet(), actualColumns.keySet(), "Users columns");
        for (Map.Entry<String, Column> expected : expectedColumns.entrySet()) {
            Column actual = actualColumns.get(expected.getKey());
            assertEquals(expected.getValue().type(), actual.type(), expected.getKey() + " type");
            if (expected.getValue().size() > 0) {
                assertEquals(expected.getValue().size(), actual.size(), expected.getKey() + " size");
            }
            assertEquals(expected.getValue().nullable(), actual.nullable(), expected.getKey() + " nullability");
        }
        assertEquals(Set.of("user_id"), primaryKeyColumns, "Users primary key");
        assertTrue(userIdAutoIncrement, "users.user_id must auto-increment");
        assertTrue(uniqueColumns.containsAll(Set.of("username", "email")),
                "users.username and users.email must be unique");
    }

    @Test
    void studentsTableMatchesItsDocumentedColumnsAndConstraints() throws SQLException {
        Map<String, Column> actualColumns = new HashMap<>();
        Set<String> primaryKeyColumns = new HashSet<>();
        Set<String> uniqueColumns = new HashSet<>();
        boolean studentIdAutoIncrement = false;

        try (Connection connection = openTestConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, "students", null)) {
                while (columns.next()) {
                    String name = normalize(columns.getString("COLUMN_NAME"));
                    actualColumns.put(name, new Column(normalize(columns.getString("TYPE_NAME")),
                            columns.getInt("COLUMN_SIZE"),
                            columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable));
                    if (name.equals("student_id")) {
                        studentIdAutoIncrement = "YES".equalsIgnoreCase(columns.getString("IS_AUTOINCREMENT"));
                    }
                }
            }
            try (ResultSet keys = metadata.getPrimaryKeys(connection.getCatalog(), null, "students")) {
                while (keys.next()) primaryKeyColumns.add(normalize(keys.getString("COLUMN_NAME")));
            }
            try (ResultSet indexes = metadata.getIndexInfo(connection.getCatalog(), null, "students", true, false)) {
                while (indexes.next()) {
                    String column = indexes.getString("COLUMN_NAME");
                    if (column != null && !indexes.getBoolean("NON_UNIQUE")) uniqueColumns.add(normalize(column));
                }
            }
        }

        Map<String, Column> expectedColumns = Map.of(
                "student_id", new Column("int", 10, false),
                "user_id", new Column("int", 10, false),
                "student_number", new Column("varchar", 30, false),
                "program", new Column("varchar", 100, false),
                "year_level", new Column("int", 10, false),
                "section", new Column("varchar", 30, true),
                "enrollment_status", new Column("varchar", 30, false));

        assertEquals(expectedColumns.keySet(), actualColumns.keySet(), "Students columns");
        for (Map.Entry<String, Column> expected : expectedColumns.entrySet()) {
            Column actual = actualColumns.get(expected.getKey());
            assertEquals(expected.getValue().type(), actual.type(), expected.getKey() + " type");
            assertEquals(expected.getValue().size(), actual.size(), expected.getKey() + " size");
            assertEquals(expected.getValue().nullable(), actual.nullable(), expected.getKey() + " nullability");
        }
        assertEquals(Set.of("student_id"), primaryKeyColumns, "Students primary key");
        assertTrue(studentIdAutoIncrement, "students.student_id must auto-increment");
        assertTrue(uniqueColumns.containsAll(Set.of("user_id", "student_number")),
                "students.user_id and students.student_number must be unique");
    }

    @Test
    void remainingDocumentedTablesMatchTheirColumnsAndConstraints() throws SQLException {
        Map<String, TableSchema> expectedTables = Map.ofEntries(
                Map.entry("instructors", schema("instructor_id",
                        columns(col("int", 10, 0, false), "instructor_id",
                                col("int", 10, 0, false), "user_id",
                                col("varchar", 30, 0, true), "employee_number",
                                col("varchar", 100, 0, false), "department",
                                col("varchar", 150, 0, true), "specialization"),
                        Set.of(Set.of("user_id"), Set.of("employee_number")))),
                Map.entry("employers", schema("employer_id",
                        columns(col("int", 10, 0, false), "employer_id",
                                col("int", 10, 0, false), "user_id",
                                col("varchar", 150, 0, false), "company_name",
                                col("varchar", 100, 0, true), "industry",
                                col("varchar", 100, 0, true), "contact_person",
                                col("varchar", 30, 0, true), "contact_number",
                                col("varchar", 255, 0, true), "address"),
                        Set.of(Set.of("user_id")))),
                Map.entry("subjects", schema("subject_id",
                        columns(col("int", 10, 0, false), "subject_id",
                                col("varchar", 30, 0, false), "subject_code",
                                col("varchar", 150, 0, false), "subject_name",
                                col("decimal", 3, 1, false), "units",
                                col("text", 0, 0, true), "description"),
                        Set.of(Set.of("subject_code")))),
                Map.entry("enrollments", schema("enrollment_id",
                        columns(col("int", 10, 0, false), "enrollment_id",
                                col("int", 10, 0, false), "student_id",
                                col("int", 10, 0, false), "subject_id",
                                col("varchar", 20, 0, false), "academic_year",
                                col("varchar", 20, 0, false), "semester",
                                col("varchar", 30, 0, false), "status"), Set.of())),
                Map.entry("grades", schema("grade_id",
                        columns(col("int", 10, 0, false), "grade_id",
                                col("int", 10, 0, false), "enrollment_id",
                                col("int", 10, 0, false), "instructor_id",
                                col("varchar", 150, 0, false), "activity_name",
                                col("varchar", 50, 0, false), "activity_type",
                                col("decimal", 6, 2, false), "score",
                                col("decimal", 6, 2, false), "max_score",
                                col("decimal", 5, 2, true), "grade",
                                col("text", 0, 0, true), "remarks",
                                col("datetime", 0, 0, false), "recorded_at"), Set.of())),
                Map.entry("attendance", schema("attendance_id",
                        columns(col("int", 10, 0, false), "attendance_id",
                                col("int", 10, 0, false), "enrollment_id",
                                col("date", 0, 0, false), "attendance_date",
                                col("varchar", 20, 0, false), "status",
                                col("text", 0, 0, true), "remarks",
                                col("int", 10, 0, false), "recorded_by"), Set.of())),
                Map.entry("technical_skills", schema("skill_id",
                        columns(col("int", 10, 0, false), "skill_id",
                                col("varchar", 100, 0, false), "skill_name",
                                col("varchar", 100, 0, false), "category",
                                col("text", 0, 0, true), "description"),
                        Set.of(Set.of("skill_name")))),
                Map.entry("student_skills", schema("student_skill_id",
                        columns(col("int", 10, 0, false), "student_skill_id",
                                col("int", 10, 0, false), "student_id",
                                col("int", 10, 0, false), "skill_id",
                                col("varchar", 30, 0, false), "proficiency_level",
                                col("decimal", 5, 2, true), "assessment_score",
                                col("int", 10, 0, true), "assessed_by",
                                col("datetime", 0, 0, true), "assessed_at",
                                col("text", 0, 0, true), "remarks"),
                        Set.of(Set.of("student_id", "skill_id")))),
                Map.entry("projects", schema("project_id",
                        columns(col("int", 10, 0, false), "project_id",
                                col("int", 10, 0, true), "student_id",
                                col("int", 10, 0, true), "subject_id",
                                col("varchar", 200, 0, false), "project_title",
                                col("varchar", 50, 0, false), "project_type",
                                col("text", 0, 0, true), "description",
                                col("text", 0, 0, true), "technologies_used",
                                col("varchar", 255, 0, true), "repository_url",
                                col("varchar", 255, 0, true), "demo_url",
                                col("date", 0, 0, true), "project_date",
                                col("varchar", 30, 0, false), "status"), Set.of())),
                Map.entry("project_members", schema("project_member_id",
                        columns(col("int", 10, 0, false), "project_member_id",
                                col("int", 10, 0, false), "project_id",
                                col("int", 10, 0, false), "student_id",
                                col("varchar", 100, 0, true), "role",
                                col("text", 0, 0, true), "contribution"),
                        Set.of(Set.of("project_id", "student_id")))),
                Map.entry("project_evaluations", schema("evaluation_id",
                        columns(col("int", 10, 0, false), "evaluation_id",
                                col("int", 10, 0, false), "project_id",
                                col("int", 10, 0, false), "student_id",
                                col("int", 10, 0, false), "instructor_id",
                                col("decimal", 5, 2, true), "contribution_score",
                                col("decimal", 5, 2, true), "technical_score",
                                col("decimal", 5, 2, true), "teamwork_score",
                                col("decimal", 5, 2, true), "overall_score",
                                col("text", 0, 0, true), "feedback",
                                col("datetime", 0, 0, false), "evaluated_at"),
                        Set.of(Set.of("project_id", "student_id")))),
                Map.entry("achievements", schema("achievement_id",
                        columns(col("int", 10, 0, false), "achievement_id",
                                col("int", 10, 0, false), "student_id",
                                col("varchar", 200, 0, false), "title",
                                col("text", 0, 0, true), "description",
                                col("varchar", 150, 0, true), "organization",
                                col("date", 0, 0, true), "achievement_date",
                                col("varchar", 255, 0, true), "verification_url"), Set.of())),
                Map.entry("certifications", schema("certification_id",
                        columns(col("int", 10, 0, false), "certification_id",
                                col("int", 10, 0, false), "student_id",
                                col("varchar", 200, 0, false), "certification_name",
                                col("varchar", 150, 0, false), "issuing_organization",
                                col("date", 0, 0, true), "issue_date",
                                col("date", 0, 0, true), "expiration_date",
                                col("varchar", 255, 0, true), "credential_url"), Set.of())),
                Map.entry("portfolios", schema("portfolio_id",
                        columns(col("int", 10, 0, false), "portfolio_id",
                                col("int", 10, 0, false), "student_id",
                                col("varchar", 200, 0, true), "headline",
                                col("text", 0, 0, true), "bio",
                                col("text", 0, 0, true), "career_goal",
                                col("varchar", 30, 0, false), "visibility",
                                col("datetime", 0, 0, false), "created_at",
                                col("datetime", 0, 0, false), "updated_at"),
                        Set.of(Set.of("student_id")))),
                Map.entry("portfolio_access", schema("access_id",
                        columns(col("int", 10, 0, false), "access_id",
                                col("int", 10, 0, false), "portfolio_id",
                                col("int", 10, 0, false), "employer_id",
                                col("varchar", 30, 0, false), "access_status",
                                col("datetime", 0, 0, true), "granted_at"),
                        Set.of(Set.of("portfolio_id", "employer_id")))),
                Map.entry("academic_support", schema("support_id",
                        columns(col("int", 10, 0, false), "support_id",
                                col("int", 10, 0, false), "student_id",
                                col("int", 10, 0, false), "instructor_id",
                                col("int", 10, 0, true), "subject_id",
                                col("varchar", 50, 0, false), "support_type",
                                col("date", 0, 0, false), "support_date",
                                col("text", 0, 0, true), "reason",
                                col("text", 0, 0, true), "action_taken",
                                col("text", 0, 0, true), "outcome",
                                col("text", 0, 0, true), "remarks"), Set.of())),
                Map.entry("job_opportunities", schema("opportunity_id",
                        columns(col("int", 10, 0, false), "opportunity_id",
                                col("int", 10, 0, false), "employer_id",
                                col("varchar", 200, 0, false), "title",
                                col("varchar", 30, 0, false), "opportunity_type",
                                col("text", 0, 0, false), "description",
                                col("text", 0, 0, true), "required_skills",
                                col("varchar", 150, 0, true), "location",
                                col("date", 0, 0, true), "deadline",
                                col("varchar", 30, 0, false), "status",
                                col("datetime", 0, 0, false), "created_at"), Set.of())),
                Map.entry("applications", schema("application_id",
                        columns(col("int", 10, 0, false), "application_id",
                                col("int", 10, 0, false), "opportunity_id",
                                col("int", 10, 0, false), "student_id",
                                col("datetime", 0, 0, false), "application_date",
                                col("varchar", 30, 0, false), "status",
                                col("text", 0, 0, true), "remarks"),
                        Set.of(Set.of("opportunity_id", "student_id")))));

        try (Connection connection = openTestConnection()) {
            for (Map.Entry<String, TableSchema> entry : expectedTables.entrySet()) {
                assertTableMatches(connection.getMetaData(), connection.getCatalog(),
                        entry.getKey(), entry.getValue());
            }
        }
    }

    @Test
    void databaseContainsTheDocumentedRelationships() throws SQLException {
        Set<ForeignKey> actual = new HashSet<>();
        try (Connection connection = openTestConnection()) {
            var metadata = connection.getMetaData();
            try (ResultSet tables = metadata.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String table = tables.getString("TABLE_NAME");
                    try (ResultSet keys = metadata.getImportedKeys(connection.getCatalog(), null, table)) {
                        while (keys.next()) {
                            actual.add(new ForeignKey(
                                    normalize(keys.getString("FKTABLE_NAME")),
                                    normalize(keys.getString("FKCOLUMN_NAME")),
                                    normalize(keys.getString("PKTABLE_NAME")),
                                    normalize(keys.getString("PKCOLUMN_NAME"))));
                        }
                    }
                }
            }
        }

        Set<ForeignKey> expected = Set.of(
                fk("students", "user_id", "users", "user_id"),
                fk("instructors", "user_id", "users", "user_id"),
                fk("employers", "user_id", "users", "user_id"),
                fk("enrollments", "student_id", "students", "student_id"),
                fk("enrollments", "subject_id", "subjects", "subject_id"),
                fk("grades", "enrollment_id", "enrollments", "enrollment_id"),
                fk("grades", "instructor_id", "instructors", "instructor_id"),
                fk("attendance", "enrollment_id", "enrollments", "enrollment_id"),
                fk("attendance", "recorded_by", "instructors", "instructor_id"),
                fk("student_skills", "student_id", "students", "student_id"),
                fk("student_skills", "skill_id", "technical_skills", "skill_id"),
                fk("student_skills", "assessed_by", "instructors", "instructor_id"),
                fk("projects", "student_id", "students", "student_id"),
                fk("projects", "subject_id", "subjects", "subject_id"),
                fk("project_members", "project_id", "projects", "project_id"),
                fk("project_members", "student_id", "students", "student_id"),
                fk("project_evaluations", "project_id", "projects", "project_id"),
                fk("project_evaluations", "student_id", "students", "student_id"),
                fk("project_evaluations", "instructor_id", "instructors", "instructor_id"),
                fk("achievements", "student_id", "students", "student_id"),
                fk("certifications", "student_id", "students", "student_id"),
                fk("portfolios", "student_id", "students", "student_id"),
                fk("portfolio_access", "portfolio_id", "portfolios", "portfolio_id"),
                fk("portfolio_access", "employer_id", "employers", "employer_id"),
                fk("academic_support", "student_id", "students", "student_id"),
                fk("academic_support", "instructor_id", "instructors", "instructor_id"),
                fk("academic_support", "subject_id", "subjects", "subject_id"),
                fk("job_opportunities", "employer_id", "employers", "employer_id"),
                fk("applications", "opportunity_id", "job_opportunities", "opportunity_id"),
                fk("applications", "student_id", "students", "student_id"));

        Set<ForeignKey> missing = new HashSet<>(expected);
        missing.removeAll(actual);
        assertTrue(missing.isEmpty(), () -> "Database is missing foreign-key relationships: " + missing);
    }

    @BeforeAll
    static void startDatabase() throws SQLException {
        database = DatabaseTestServer.getInstance();
    }

    // The embedded database is intentionally shared with the other integration
    // tests and is stopped by the JVM shutdown hook registered by DatabaseTestServer.

    private static Connection openTestConnection() throws SQLException {
        return database.openConnection();
    }

    private static TableSchema schema(String primaryKey, Map<String, TableColumn> columns,
                                      Set<Set<String>> uniqueConstraints) {
        return new TableSchema(columns, primaryKey, uniqueConstraints, primaryKey);
    }

    private static Map<String, TableColumn> columns(Object... nameAndColumn) {
        Map<String, TableColumn> columns = new HashMap<>();
        for (int i = 0; i < nameAndColumn.length; i += 2) {
            columns.put((String) nameAndColumn[i + 1], (TableColumn) nameAndColumn[i]);
        }
        return columns;
    }

    private static TableColumn col(String type, int size, int scale, boolean nullable) {
        return new TableColumn(type, size, scale, nullable);
    }

    private static void assertTableMatches(DatabaseMetaData metadata, String catalog,
                                           String table, TableSchema expected) throws SQLException {
        Map<String, TableColumn> actualColumns = new HashMap<>();
        Set<String> primaryKeyColumns = new HashSet<>();
        Map<String, Set<String>> uniqueIndexes = new HashMap<>();
        boolean autoIncrement = false;

        try (ResultSet columns = metadata.getColumns(catalog, null, table, null)) {
            while (columns.next()) {
                String name = normalize(columns.getString("COLUMN_NAME"));
                actualColumns.put(name, new TableColumn(
                        normalize(columns.getString("TYPE_NAME")),
                        columns.getInt("COLUMN_SIZE"),
                        columns.getInt("DECIMAL_DIGITS"),
                        columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable));
                if (name.equals(expected.autoIncrementColumn())) {
                    autoIncrement = "YES".equalsIgnoreCase(columns.getString("IS_AUTOINCREMENT"));
                }
            }
        }
        try (ResultSet keys = metadata.getPrimaryKeys(catalog, null, table)) {
            while (keys.next()) primaryKeyColumns.add(normalize(keys.getString("COLUMN_NAME")));
        }
        try (ResultSet indexes = metadata.getIndexInfo(catalog, null, table, true, false)) {
            while (indexes.next()) {
                String column = indexes.getString("COLUMN_NAME");
                String indexName = indexes.getString("INDEX_NAME");
                if (column != null && indexName != null && !indexes.getBoolean("NON_UNIQUE")) {
                    uniqueIndexes.computeIfAbsent(indexName, ignored -> new HashSet<>()).add(normalize(column));
                }
            }
        }

        assertEquals(expected.columns().keySet(), actualColumns.keySet(), table + " columns");
        for (Map.Entry<String, TableColumn> column : expected.columns().entrySet()) {
            TableColumn actual = actualColumns.get(column.getKey());
            TableColumn definition = column.getValue();
            assertEquals(definition.type(), actual.type(), table + "." + column.getKey() + " type");
            if (definition.size() > 0) {
                assertEquals(definition.size(), actual.size(), table + "." + column.getKey() + " size");
            }
            if (definition.type().equals("decimal")) {
                assertEquals(definition.scale(), actual.scale(), table + "." + column.getKey() + " scale");
            }
            assertEquals(definition.nullable(), actual.nullable(), table + "." + column.getKey() + " nullability");
        }
        assertEquals(Set.of(expected.primaryKey()), primaryKeyColumns, table + " primary key");
        assertTrue(autoIncrement, table + "." + expected.autoIncrementColumn() + " must auto-increment");
        Set<Set<String>> actualUniqueConstraints = new HashSet<>(uniqueIndexes.values());
        assertTrue(actualUniqueConstraints.containsAll(expected.uniqueConstraints()),
                () -> table + " missing unique constraints: " + expected.uniqueConstraints());
    }

    private static ForeignKey fk(String table, String column, String referencedTable, String referencedColumn) {
        return new ForeignKey(table, column, referencedTable, referencedColumn);
    }

    private static String normalize(String identifier) {
        return identifier.toLowerCase(Locale.ROOT);
    }
}
