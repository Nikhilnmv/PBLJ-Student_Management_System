package com.student.model;

import java.io.Serializable;

/**
 * MODEL class: represents one administrator account (one row of the "admin" table).
 *
 * After a successful login, an Admin object is stored in the HttpSession.
 * It implements Serializable because Tomcat may save session objects to disk
 * when it restarts.
 */
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    private int adminId;
    private String username;
    private String passwordHash;  // BCrypt hash read from the database, never the real password

    public Admin() {
    }

    public Admin(int adminId, String username, String passwordHash) {
        this.adminId = adminId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
