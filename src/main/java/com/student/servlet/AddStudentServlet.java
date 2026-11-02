package com.student.servlet;

import com.student.dao.DuplicateStudentException;
import com.student.dao.StudentDAO;
import com.student.model.Student;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * CONTROLLER for "Add Student" (admin adds a student manually).
 *
 * URL: /admin/students/add
 *   GET  -> empty student form (student-form.jsp in "add" mode)
 *   POST -> validate, insert into MySQL, redirect to the new student's profile
 *
 * Same steps as the public registration, but the admin may also choose the status.
 */
@WebServlet("/admin/students/add")
public class AddStudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showForm(request, response, new Student(), null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Student student = StudentRequestHelper.readStudentFromRequest(request);
        try {
            Map<String, String> errors = StudentRequestHelper.validateStudent(student, studentDAO, 0);
            if (!errors.isEmpty()) {
                showForm(request, response, student, errors);
                return;
            }

            int newStudentId = studentDAO.addStudent(student);
            StudentRequestHelper.setFlashSuccess(request,
                    "Student " + student.getFullName() + " was added with registration ID "
                            + student.getRegistrationId() + ".");
            response.sendRedirect(request.getContextPath() + "/admin/students/view?id=" + newStudentId);

        } catch (DuplicateStudentException e) {
            showForm(request, response, student, Map.of(e.getField(), e.getMessage()));
        } catch (SQLException e) {
            log("Could not add student", e);
            throw new ServletException("Could not add the student.", e);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Student student, Map<String, String> errors) throws ServletException, IOException {
        request.setAttribute("student", student);
        request.setAttribute(StudentRequestHelper.ERRORS_ATTRIBUTE, errors);
        request.setAttribute("mode", "add");
        request.setAttribute("showStatusField", true);
        StudentRequestHelper.setFormOptions(request);
        request.getRequestDispatcher("/WEB-INF/views/admin/student-form.jsp").forward(request, response);
    }
}
