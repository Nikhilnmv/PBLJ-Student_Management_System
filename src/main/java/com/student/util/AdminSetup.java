package com.student.util;

import com.student.dao.AdminDAO;
import com.student.model.Admin;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * ONE-TIME SETUP TOOL: creates the first administrator account
 * (or resets the password of an existing administrator).
 *
 * Why a separate tool? Putting an admin password inside schema.sql would
 * upload it to GitHub. Instead, you run this small program on your own
 * computer, type the password, and only its BCrypt HASH is stored in MySQL.
 *
 * How to run (from the project folder):
 *     mvn compile exec:java
 * or, in IntelliJ / Eclipse: right-click this file -> Run 'AdminSetup.main()'.
 *
 * It uses the same database settings as the web application
 * (db.properties or the DB_URL / DB_USER / DB_PASSWORD environment variables).
 */
public final class AdminSetup {

    /** Usernames: 4-50 letters, digits, dots or underscores. */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._]{4,50}$");

    private final AdminDAO adminDAO = new AdminDAO();
    private final Console console = System.console();
    private final BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    private AdminSetup() {
    }

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println(" Student Registration System - Admin Setup");
        System.out.println("==============================================");
        int exitCode = new AdminSetup().run();
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /** Runs the setup steps. Returns 0 on success, 1 on failure. */
    private int run() {
        try {
            String username = askUsername();
            if (username == null) {
                return 1;
            }

            Admin existing = adminDAO.findByUsername(username);
            if (existing != null) {
                String answer = readLine("Admin '" + username + "' already exists. Reset its password? (yes/no): ");
                if (answer == null || !answer.trim().equalsIgnoreCase("yes")) {
                    System.out.println("No changes made.");
                    return 0;
                }
            }

            char[] password = askNewPassword();
            if (password == null) {
                return 1;
            }

            // Hash the password with BCrypt; only the hash is stored in MySQL
            String passwordHash = PasswordUtil.hashPassword(new String(password));
            Arrays.fill(password, ' ');  // wipe the password from memory

            if (existing == null) {
                adminDAO.createAdmin(username, passwordHash);
                System.out.println("SUCCESS: administrator '" + username + "' created.");
            } else {
                adminDAO.updatePassword(username, passwordHash);
                System.out.println("SUCCESS: password for '" + username + "' was reset.");
            }
            System.out.println("You can now log in at http://localhost:8080/student-registration/login");
            return 0;

        } catch (SQLException e) {
            System.out.println("ERROR: could not connect to or update the database.");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Check that MySQL is running, schema.sql has been imported and");
            System.out.println("src/main/resources/db.properties contains the correct username and password.");
            return 1;
        }
    }

    private String askUsername() {
        for (int attempt = 0; attempt < 3; attempt++) {
            String username = readLine("Admin username (4-50 letters, digits, . or _): ");
            if (username == null) {
                return null;
            }
            username = username.trim();
            if (USERNAME_PATTERN.matcher(username).matches()) {
                return username;
            }
            System.out.println("Invalid username. Please try again.");
        }
        System.out.println("Too many invalid attempts.");
        return null;
    }

    private char[] askNewPassword() {
        System.out.println("Password rules: " + PasswordUtil.MIN_PASSWORD_LENGTH + "-"
                + PasswordUtil.MAX_PASSWORD_LENGTH + " characters, with at least one letter and one digit.");
        for (int attempt = 0; attempt < 3; attempt++) {
            char[] password = readPassword("New password: ");
            char[] confirmation = readPassword("Confirm password: ");
            if (password == null || confirmation == null) {
                return null;
            }
            if (!Arrays.equals(password, confirmation)) {
                System.out.println("The passwords do not match. Please try again.");
            } else if (!PasswordUtil.isStrongPassword(new String(password))) {
                System.out.println("The password does not meet the rules. Please try again.");
            } else {
                Arrays.fill(confirmation, ' ');
                return password;
            }
        }
        System.out.println("Too many invalid attempts.");
        return null;
    }

    /**
     * Reads a password without showing it on screen when a real terminal is
     * available. Some IDE consoles have no terminal (System.console() is null);
     * then the password is read as normal text and a warning is printed.
     */
    private char[] readPassword(String prompt) {
        if (console != null) {
            return console.readPassword("%s", prompt);  // typed characters are hidden
        }
        String line = readLine(prompt + "(visible - IDE console) ");
        return line == null ? null : line.toCharArray();
    }

    private String readLine(String prompt) {
        if (console != null) {
            return console.readLine("%s", prompt);
        }
        System.out.print(prompt);
        System.out.flush();
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
