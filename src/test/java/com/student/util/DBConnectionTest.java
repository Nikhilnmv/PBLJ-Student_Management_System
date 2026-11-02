package com.student.util;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the beginner-friendly database error explanations. */
class DBConnectionTest {

    @Test
    void missingConfigurationIsExplained() {
        String hint = DBConnection.explain(new SQLException("Database is not configured. Set DB_URL ..."));
        assertTrue(hint.contains("db.properties"));
    }

    @Test
    void wrongPasswordIsExplained() {
        String hint = DBConnection.explain(new SQLException("Access denied for user 'root'@'localhost'", "28000", 1045));
        assertTrue(hint.contains("username or password"));
    }

    @Test
    void missingDatabaseOrTablesAreExplained() {
        assertTrue(DBConnection.explain(new SQLException("Unknown database", "42000", 1049)).contains("schema.sql"));
        assertTrue(DBConnection.explain(new SQLException("Table doesn't exist", "42S02", 1146)).contains("schema.sql"));
    }

    @Test
    void stoppedServerIsExplained() {
        String hint = DBConnection.explain(new SQLException("Communications link failure", "08S01", 0));
        assertTrue(hint.contains("MySQL is running"));
    }

    @Test
    void otherErrorsShowTheOriginalMessage() {
        assertTrue(DBConnection.explain(new SQLException("Something unusual", "HY000", 9999)).contains("Something unusual"));
    }
}
