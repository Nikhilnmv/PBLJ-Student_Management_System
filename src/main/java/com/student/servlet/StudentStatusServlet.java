package com.student.servlet;

import com.student.dao.StudentDAO;
import com.student.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * CONTROLLER that changes a student's status between Active and Inactive.
 *
 * URL: /admin/students/status - POST only, parameters: id, status, csrfToken.
 * After the change the admin is sent back to the student's profile.
 */
@WebServlet("/admin/students/status")
public class StudentStatusServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int studentId = StudentRequestHelper.readPositiveInt(request, "id");
        String newStatus = request.getParameter("status");

        if (studentId < 1 || !ValidationUtil.isValidStatus(newStatus)) {
            StudentRequestHelper.setFlashError(request, "Invalid status change request.");
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }

        try {
            if (studentDAO.updateStatus(studentId, newStatus)) {
                StudentRequestHelper.setFlashSuccess(request, "Status changed to " + newStatus + ".");
                response.sendRedirect(request.getContextPath() + "/admin/students/view?id=" + studentId);
            } else {
                StudentRequestHelper.setFlashError(request, "Student not found.");
                response.sendRedirect(request.getContextPath() + "/admin/students");
            }
        } catch (SQLException e) {
            log("Could not change status of student " + studentId, e);
            throw new ServletException("Could not change the student's status.", e);
        }
    }
}
