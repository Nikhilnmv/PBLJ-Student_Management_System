package com.student.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates connections to the MySQL database.
 *
 * Every DAO class calls {@link #getConnection()} when it needs to run SQL.
 * Keeping the connection code in ONE place means the database URL, username
 * and password are configured once, not repeated in every class.
 *
 * Where do the database settings come from? (checked in this order)
 *   1. Environment variables DB_URL, DB_USER and DB_PASSWORD.
 *   2. The file src/main/resources/db.properties (keys db.url, db.user, db.password).
 *
 * The real db.properties file is listed in .gitignore, so the password is
 * never uploaded to GitHub. Only db.properties.example (with no real
 * password) is committed.
 */
public final class DBConnection {

    /** Fully qualified name of the MySQL JDBC driver class. */
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    /** Name of the optional settings file on the classpath. */
    private static final String PROPERTIES_FILE = "db.properties";

    // Settings are loaded once, when this class is first used.
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    /*
     * Static initialisation block: runs exactly once, when the JVM loads
     * this class. We register the driver and read the configuration here.
     */
    static {
        try {
            // Inside Tomcat, explicitly loading the driver class makes sure it
            // registers itself with DriverManager.
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "MySQL JDBC driver not found. Check the mysql-connector-j dependency in pom.xml.", e);
        }

        Properties fileSettings = loadPropertiesFile();
        URL = readSetting("DB_URL", "db.url", fileSettings);
        USER = readSetting("DB_USER", "db.user", fileSettings);
        PASSWORD = readSetting("DB_PASSWORD", "db.password", fileSettings);
    }

    /** Private constructor: this is a utility class, so no objects are created. */
    private DBConnection() {
    }

    /**
     * Opens a new connection to MySQL.
     *
     * The caller MUST close the connection. The easiest way is
     * try-with-resources:
     * <pre>
     * try (Connection con = DBConnection.getConnection()) {
     *     ... use the connection ...
     * } // con.close() is called automatically here
     * </pre>
     *
     * @return an open JDBC connection
     * @throws SQLException if the database is not configured or cannot be reached
     */
    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null) {
            throw new SQLException("Database is not configured. Set DB_URL, DB_USER and DB_PASSWORD "
                    + "environment variables, or create src/main/resources/db.properties "
                    + "from db.properties.example.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD == null ? "" : PASSWORD);
    }

    /**
     * Returns the database URL being used (without the password).
     * Useful for log messages at start-up.
     */
    public static String getConfiguredUrl() {
        return URL;
    }

    /**
     * Turns a database error into a short, beginner-friendly explanation of
     * what to fix. Used on the login and first-time setup pages, where a new
     * user is most likely to meet a configuration problem.
     *
     * MySQL error codes used: 1045 = wrong username/password,
     * 1049 = unknown database, 1146 = table does not exist.
     * SQL states starting with "08" mean the connection itself failed.
     */
    public static String explain(SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage();
        String sqlState = e.getSQLState() == null ? "" : e.getSQLState();

        if (message.startsWith("Database is not configured")) {
            return "The database settings are missing. Create src/main/resources/db.properties "
                    + "(copy db.properties.example) or simply run start.bat / start.sh, then restart.";
        }
        switch (e.getErrorCode()) {
            case 1045:
                return "MySQL refused the username or password. Check db.user and db.password in "
                        + "src/main/resources/db.properties, then restart the application.";
            case 1049:
                return "The database 'student_registration_db' does not exist yet. "
                        + "Run database/schema.sql in MySQL Workbench.";
            case 1146:
                return "The database tables are missing. Run database/schema.sql in MySQL Workbench.";
            default:
                break;
        }
        if (sqlState.startsWith("08")) {
            return "Cannot reach the MySQL server on localhost:3306. Make sure MySQL is running "
                    + "(Windows: Services -> MySQL80 -> Start; macOS: System Settings -> MySQL -> Start).";
        }
        return "Database error: " + message;
    }

    /**
     * Reads db.properties from the classpath. Returns an empty Properties
     * object if the file does not exist (environment variables may be used instead).
     */
    private static Properties loadPropertiesFile() {
        Properties properties = new Properties();
        ClassLoader loader = DBConnection.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Could not read " + PROPERTIES_FILE + ": " + e.getMessage());
        }
        return properties;
    }

    /**
     * Returns the environment variable if it is set, otherwise the value from
     * the properties file, otherwise null.
     */
    private static String readSetting(String envName, String propertyName, Properties fileSettings) {
        String value = System.getenv(envName);
        if (value == null || value.isBlank()) {
            value = fileSettings.getProperty(propertyName);
        }
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
