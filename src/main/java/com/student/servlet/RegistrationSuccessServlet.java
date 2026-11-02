package com.student.servlet;

import com.student.model.Student;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * CONTROLLER for the "Registration Successful" page.
 *
 * URL: /register/success (GET)
 *
 * RegisterServlet stores the newly saved Student in the session and redirects
 * here. This servlet takes the student out of the session (so it is shown
 * only once) and forwards to registration-success.jsp.
 * If there is nothing to show (e.g. someone opens the URL directly), the
 * visitor is sent back to the registration form.
 */
@WebServlet("/register/success")
public class RegistrationSuccessServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);  // false = do not create a new session
        Student student = null;
        if (session != null) {
            student = (Student) session.getAttribute(RegisterServlet.REGISTERED_STUDENT_ATTRIBUTE);
            session.removeAttribute(RegisterServlet.REGISTERED_STUDENT_ATTRIBUTE);
        }

        if (student == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }

        request.setAttribute("student", student);
        request.getRequestDispatcher("/WEB-INF/views/registration-success.jsp").forward(request, response);
    }
}
