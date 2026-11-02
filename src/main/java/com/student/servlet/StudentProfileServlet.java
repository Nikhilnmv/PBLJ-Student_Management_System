package com.student.servlet;

import com.student.dao.StudentDAO;
import com.student.model.Student;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * CONTROLLER for one student's full profile.
 *
 * URL: /admin/students/view?id=12 (GET)
 * Shows 404 "Page not found" if the ID is missing, invalid or does not exist.
 */
@WebServlet("/admin/students/view")
public class StudentProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int studentId = StudentRequestHelper.readPositiveInt(request, "id");
        if (studentId < 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Student student = studentDAO.getStudentById(studentId);
            if (student == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("student", student);
            request.getRequestDispatcher("/WEB-INF/views/admin/student-profile.jsp").forward(request, response);

        } catch (SQLException e) {
            log("Could not load student " + studentId, e);
            throw new ServletException("Could not load the student profile.", e);
        }
    }
}
