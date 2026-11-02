package com.student.dao;

import com.student.model.Admin;
import com.student.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO (Data Access Object) for the "admin" table.
 * Used by LoginServlet (to check credentials) and by the AdminSetup tool
 * (to create the first administrator account).
 */
public class AdminDAO {

    private static final String FIND_BY_USERNAME_SQL =
            "SELECT admin_id, username, password_hash FROM admin WHERE username = ?";

    private static final String INSERT_ADMIN_SQL =
            "INSERT INTO admin (username, password_hash) VALUES (?, ?)";

    private static final String UPDATE_PASSWORD_SQL =
            "UPDATE admin SET password_hash = ? WHERE username = ?";

    /**
     * Finds an administrator by username.
     *
     * @return the Admin (including the stored BCrypt hash), or null if not found
     */
    public Admin findByUsername(String username) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME_SQL)) {

            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Admin(
                            resultSet.getInt("admin_id"),
                            resultSet.getString("username"),
                            resultSet.getString("password_hash"));
                }
                return null;
            }
        }
    }

    /**
     * Creates a new administrator. The password must ALREADY be hashed
     * with PasswordUtil.hashPassword(...).
     */
    public void createAdmin(String username, String passwordHash) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ADMIN_SQL)) {

            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.executeUpdate();
        }
    }

    /** Replaces an administrator's password hash (used to reset a forgotten password). */
    public boolean updatePassword(String username, String passwordHash) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD_SQL)) {

            statement.setString(1, passwordHash);
            statement.setString(2, username);
            return statement.executeUpdate() > 0;
        }
    }
}
