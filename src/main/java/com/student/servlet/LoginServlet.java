package com.student.servlet;

import com.student.dao.AdminDAO;
import com.student.filter.AuthFilter;
import com.student.model.Admin;
import com.student.util.CsrfUtil;
import com.student.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * CONTROLLER for administrator login.
 *
 * URL: /login
 *   GET  -> shows the login form (login.jsp)
 *   POST -> checks the username and password against the "admin" table
 *
 * On success, the Admin is stored in a NEW HttpSession and the browser is
 * redirected to the dashboard. From then on, AuthFilter recognises the
 * admin by the session cookie (JSESSIONID) that the browser sends back.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String LOGIN_PAGE = "/WEB-INF/views/login.jsp";

    /** Log the admin out automatically after 30 minutes without activity. */
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private final AdminDAO adminDAO = new AdminDAO();

    /** GET: show the login form (or go straight to the dashboard if already logged in). */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(AuthFilter.ADMIN_SESSION_ATTRIBUTE) != null) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
    }

    /** POST: check the submitted username and password. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        username = (username == null) ? "" : username.trim();

        if (username.isEmpty() || password == null || password.isEmpty()) {
            showLoginError(request, response, username, "Please enter both username and password.");
            return;
        }

        try {
            // 1. Look up the admin by username (PreparedStatement - safe from SQL injection)
            Admin admin = adminDAO.findByUsername(username);

            // 2. Compare the typed password with the stored BCrypt hash
            if (admin == null || !PasswordUtil.checkPassword(password, admin.getPasswordHash())) {
                // Same message for "unknown user" and "wrong password", so an attacker
                // cannot find out which usernames exist.
                showLoginError(request, response, username, "Invalid username or password.");
                return;
            }

            // 3. Prevent "session fixation": throw away any old session and start a new one
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);

            // 4. Remember who is logged in (without the password hash) and create the CSRF token
            session.setAttribute(AuthFilter.ADMIN_SESSION_ATTRIBUTE,
                    new Admin(admin.getAdminId(), admin.getUsername(), null));
            session.setAttribute(CsrfUtil.TOKEN_NAME, CsrfUtil.generateToken());

            log("Administrator '" + admin.getUsername() + "' logged in.");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");

        } catch (SQLException e) {
            log("Login failed because the database is unavailable", e);
            showLoginError(request, response, username,
                    "The database is not available right now. Please try again later.");
        }
    }

    private void showLoginError(HttpServletRequest request, HttpServletResponse response,
                                String username, String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.setAttribute("username", username);
        request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
    }
}
