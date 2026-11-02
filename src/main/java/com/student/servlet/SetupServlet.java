package com.student.servlet;

import com.student.dao.AdminDAO;
import com.student.util.DBConnection;
import com.student.util.PasswordUtil;
import com.student.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CONTROLLER for the FIRST-TIME SETUP page: creates the first administrator.
 *
 * URL: /setup
 *   GET  -> shows the "Create administrator account" form
 *   POST -> validates the form, hashes the password with BCrypt and saves the admin
 *
 * SAFETY RULE: this page only works while the admin table is EMPTY.
 * As soon as one administrator exists, every request to /setup is sent to
 * the login page, so nobody can use it to create extra admin accounts.
 *
 * (The command-line tool AdminSetup does the same job from a terminal and can
 * also reset a forgotten password.)
 */
@WebServlet("/setup")
public class SetupServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String SETUP_PAGE = "/WEB-INF/views/setup.jsp";

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            if (adminDAO.countAdmins() > 0) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        } catch (SQLException e) {
            // Most likely a configuration problem on a new installation: explain how to fix it
            log("Setup page: database problem", e);
            request.setAttribute("dbError", DBConnection.explain(e));
        }
        request.getRequestDispatcher(SETUP_PAGE).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        username = (username == null) ? "" : username.trim();
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        try {
            // Safety rule: never create an admin through this page if one already exists
            if (adminDAO.countAdmins() > 0) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            Map<String, String> errors = new LinkedHashMap<>();
            if (!ValidationUtil.isValidUsername(username)) {
                errors.put("username", "Use 4-50 letters, digits, dots or underscores (no spaces).");
            }
            if (!PasswordUtil.isStrongPassword(password)) {
                errors.put("password", "Use 8-72 characters with at least one letter and one digit.");
            } else if (!password.equals(confirmPassword)) {
                errors.put("confirmPassword", "The two passwords do not match.");
            }

            if (!errors.isEmpty()) {
                request.setAttribute("errors", errors);
                request.setAttribute("username", username);
                request.getRequestDispatcher(SETUP_PAGE).forward(request, response);
                return;
            }

            // Only the BCrypt hash is stored - never the password itself
            adminDAO.createAdmin(username, PasswordUtil.hashPassword(password));
            log("First administrator '" + username + "' created through the setup page.");
            response.sendRedirect(request.getContextPath() + "/login?setup=done");

        } catch (SQLException e) {
            log("Setup page: could not create the administrator", e);
            request.setAttribute("dbError", DBConnection.explain(e));
            request.setAttribute("username", username);
            request.getRequestDispatcher(SETUP_PAGE).forward(request, response);
        }
    }
}
