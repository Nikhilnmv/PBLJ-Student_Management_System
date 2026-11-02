package com.student.servlet;

import com.student.dao.StudentDAO;
import com.student.model.Student;
import com.student.model.StudentFilter;
import com.student.util.CsvUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * CONTROLLER that downloads student records as a CSV file (opens in Excel).
 *
 * URL: /admin/students/export (GET)
 * It accepts the same keyword/department/semester/status parameters as the
 * student list, so the admin can export exactly the filtered results.
 *
 * Instead of forwarding to a JSP, this servlet writes the file directly to
 * the response, and the Content-Disposition header tells the browser to
 * download it instead of displaying it.
 */
@WebServlet("/admin/students/export")
public class ExportStudentsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** The CSV standard (RFC 4180) ends every line with carriage return + line feed. */
    private static final String LINE_END = "\r\n";

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StudentFilter filter = StudentRequestHelper.readFilterFromRequest(request);
        List<Student> students;
        try {
            students = studentDAO.searchStudents(filter);  // all matching rows, no paging
        } catch (SQLException e) {
            log("Could not export students", e);
            throw new ServletException("Could not export the student list.", e);
        }

        String fileName = "students-" + LocalDate.now() + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        PrintWriter writer = response.getWriter();
        // Byte Order Mark: tells Excel the file is UTF-8, so names with accents display correctly
        writer.write('\uFEFF');
        writer.print(CsvUtil.toCsvLine("Registration ID", "Roll Number", "Full Name", "Email", "Phone",
                "Gender", "Date of Birth", "Department", "Course", "Semester", "Address", "Status", "Registered On"));
        writer.print(LINE_END);

        for (Student student : students) {
            writer.print(CsvUtil.toCsvLine(
                    student.getRegistrationId(),
                    student.getRollNumber(),
                    student.getFullName(),
                    student.getEmail(),
                    student.getPhone(),
                    student.getGender(),
                    student.getDobIso(),
                    student.getDepartment(),
                    student.getCourse(),
                    String.valueOf(student.getSemester()),
                    student.getAddress(),
                    student.getStatus(),
                    student.getCreatedAtFormatted()));
            writer.print(LINE_END);
        }
        writer.flush();
    }
}
