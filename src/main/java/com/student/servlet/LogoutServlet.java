package com.student.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * CONTROLLER for logging out.
 *
 * URL: /admin/logout
 *   POST -> destroys the session and returns to the login page
 *   GET  -> just goes back to the dashboard (logout must be a POST with a
 *           valid CSRF token, so another website cannot log the admin out)
 *
 * Because the URL starts with /admin/, AuthFilter has already checked that
 * an admin is logged in and that the CSRF token is correct.
 */
@WebServlet("/admin/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // invalidate() deletes the session and EVERYTHING stored in it (admin, CSRF token)
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login?logout=true");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }
}
