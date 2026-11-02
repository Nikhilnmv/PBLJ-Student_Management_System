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
 * CONTROLLER for "Edit Student".
 *
 * URL: /admin/students/edit?id=12
 *   GET  -> form pre-filled with the student's current details
 *   POST -> validate the changes, UPDATE the row in MySQL, redirect to the profile
 */
@WebServlet("/admin/students/edit")
public class EditStudentServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int studentId = StudentRequestHelper.readPositiveInt(request, "id");
        try {
            Student student = studentId < 1 ? null : studentDAO.getStudentById(studentId);
            if (student == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            showForm(request, response, student, null);

        } catch (SQLException e) {
            log("Could not load student " + studentId + " for editing", e);
            throw new ServletException("Could not load the student.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int studentId = StudentRequestHelper.readPositiveInt(request, "id");
        if (studentId < 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Student student = StudentRequestHelper.readStudentFromRequest(request);
        student.setStudentId(studentId);

        try {
            // Pass the student's own ID so its current roll number/email are not seen as duplicates
            Map<String, String> errors = StudentRequestHelper.validateStudent(student, studentDAO, studentId);
            if (!errors.isEmpty()) {
                showForm(request, response, student, errors);
                return;
            }

            boolean updated = studentDAO.updateStudent(student);
            if (updated) {
                StudentRequestHelper.setFlashSuccess(request, "Changes to " + student.getFullName() + " were saved.");
                response.sendRedirect(request.getContextPath() + "/admin/students/view?id=" + studentId);
            } else {
                // The student was deleted (perhaps in another tab) while the form was open
                StudentRequestHelper.setFlashError(request, "This student no longer exists.");
                response.sendRedirect(request.getContextPath() + "/admin/students");
            }

        } catch (DuplicateStudentException e) {
            showForm(request, response, student, Map.of(e.getField(), e.getMessage()));
        } catch (SQLException e) {
            log("Could not update student " + studentId, e);
            throw new ServletException("Could not update the student.", e);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Student student, Map<String, String> errors) throws ServletException, IOException {
        request.setAttribute("student", student);
        request.setAttribute(StudentRequestHelper.ERRORS_ATTRIBUTE, errors);
        request.setAttribute("mode", "edit");
        request.setAttribute("showStatusField", true);
        StudentRequestHelper.setFormOptions(request);
        request.getRequestDispatcher("/WEB-INF/views/admin/student-form.jsp").forward(request, response);
    }
}
