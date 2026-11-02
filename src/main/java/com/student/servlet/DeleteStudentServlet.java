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
 * CONTROLLER that permanently deletes a student.
 *
 * URL: /admin/students/delete  - POST only (with id and csrfToken).
 *
 * Why POST and not a simple link (GET)? GET requests must never change
 * data: browsers, search engines and link previews may open GET links
 * automatically. Only doPost() is written here, so a GET request receives
 * "405 Method Not Allowed" from HttpServlet and nothing is deleted.
 */
@WebServlet("/admin/students/delete")
public class DeleteStudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int studentId = StudentRequestHelper.readPositiveInt(request, "id");
        try {
            // Load the student first so the message can include the name
            Student student = studentId < 1 ? null : studentDAO.getStudentById(studentId);

            if (student != null && studentDAO.deleteStudent(studentId)) {
                StudentRequestHelper.setFlashSuccess(request, "Student " + student.getFullName()
                        + " (" + student.getRollNumber() + ") was deleted.");
            } else {
                StudentRequestHelper.setFlashError(request, "Student not found. It may already have been deleted.");
            }
            response.sendRedirect(request.getContextPath() + "/admin/students");

        } catch (SQLException e) {
            log("Could not delete student " + studentId, e);
            throw new ServletException("Could not delete the student.", e);
        }
    }
}
