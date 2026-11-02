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
 * CONTROLLER for the public student registration form.
 *
 * URL: /register
 *   GET  -> shows the empty registration form (register.jsp)
 *   POST -> validates the submitted form, saves the student in MySQL and
 *           redirects to the success page
 *
 * Flow:  register.jsp --POST--> RegisterServlet --> StudentDAO --> DBConnection --> MySQL
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Session attribute used to pass the new student to the success page. */
    static final String REGISTERED_STUDENT_ATTRIBUTE = "registeredStudent";

    private static final String FORM_PAGE = "/WEB-INF/views/register.jsp";

    private final StudentDAO studentDAO = new StudentDAO();

    /** GET: display an empty registration form. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("student", new Student());
        showForm(request, response);
    }

    /** POST: process the submitted registration form. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Read the form fields into a Student object
        Student student = StudentRequestHelper.readStudentFromRequest(request);
        // Students cannot choose their own status: every new registration starts as Active
        student.setStatus(Student.STATUS_ACTIVE);

        try {
            // 2. Validate (format rules + duplicate roll number / email)
            Map<String, String> errors = StudentRequestHelper.validateStudent(student, studentDAO, 0);
            if (!errors.isEmpty()) {
                showFormWithErrors(request, response, student, errors);
                return;
            }

            // 3. Save to MySQL. The DAO returns the new auto-generated student_id.
            int newStudentId = studentDAO.addStudent(student);

            // 4. Read the saved row back, so the success page shows exactly what is in
            //    the database (including the registration time set by MySQL).
            Student savedStudent = studentDAO.getStudentById(newStudentId);

            // 5. Post/Redirect/Get: redirect to the success page instead of showing it
            //    directly, so refreshing the page cannot submit the form a second time.
            request.getSession().setAttribute(REGISTERED_STUDENT_ATTRIBUTE,
                    savedStudent != null ? savedStudent : student);
            response.sendRedirect(request.getContextPath() + "/register/success");

        } catch (DuplicateStudentException e) {
            // Rare case: someone else registered the same roll number/email a moment ago.
            // The UNIQUE constraint in MySQL stopped the duplicate; tell the user which field.
            showFormWithErrors(request, response, student, Map.of(e.getField(), e.getMessage()));
        } catch (SQLException e) {
            // Database problem (e.g. MySQL stopped). Log the details for the developer and
            // let the friendly error page tell the user to try again later.
            log("Registration failed for roll number " + student.getRollNumber(), e);
            throw new ServletException("Could not save the registration.", e);
        }
    }

    private void showFormWithErrors(HttpServletRequest request, HttpServletResponse response,
                                    Student student, Map<String, String> errors)
            throws ServletException, IOException {
        request.setAttribute("student", student);  // so the form keeps what the user typed
        request.setAttribute(StudentRequestHelper.ERRORS_ATTRIBUTE, errors);
        showForm(request, response);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StudentRequestHelper.setFormOptions(request);
        request.getRequestDispatcher(FORM_PAGE).forward(request, response);
    }
}
