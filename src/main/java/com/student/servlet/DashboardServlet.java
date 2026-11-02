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
import java.util.List;
import java.util.Map;

/**
 * CONTROLLER for the admin dashboard.
 *
 * URL: /admin/dashboard (also /admin) - GET only, protected by AuthFilter.
 *
 * Every number on the dashboard comes from a live SQL query in StudentDAO
 * (COUNT, COUNT DISTINCT, GROUP BY ...). Nothing is hard-coded, so the
 * dashboard changes as soon as a student registers, is edited or deleted.
 */
@WebServlet({"/admin/dashboard", "/admin"})
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** How many of the newest registrations to list on the dashboard. */
    private static final int RECENT_LIMIT = 5;

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int totalStudents = studentDAO.countAllStudents();
            int activeStudents = studentDAO.countStudentsByStatus(Student.STATUS_ACTIVE);
            int inactiveStudents = studentDAO.countStudentsByStatus(Student.STATUS_INACTIVE);
            int totalDepartments = studentDAO.countDepartments();
            List<Student> recentStudents = studentDAO.getRecentStudents(RECENT_LIMIT);
            Map<String, Integer> departmentCounts = studentDAO.getDepartmentCounts();

            // Put the results in the request so dashboard.jsp can display them
            request.setAttribute("totalStudents", totalStudents);
            request.setAttribute("activeStudents", activeStudents);
            request.setAttribute("inactiveStudents", inactiveStudents);
            request.setAttribute("totalDepartments", totalDepartments);
            request.setAttribute("activePercent", percentage(activeStudents, totalStudents));
            request.setAttribute("inactivePercent", percentage(inactiveStudents, totalStudents));
            request.setAttribute("recentStudents", recentStudents);
            request.setAttribute("departmentCounts", departmentCounts);

            request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            log("Could not load dashboard statistics", e);
            throw new ServletException("Could not load dashboard statistics.", e);
        }
    }

    /** Whole-number percentage, e.g. percentage(3, 4) = 75. Avoids dividing by zero. */
    private int percentage(int part, int total) {
        return total == 0 ? 0 : Math.round(part * 100f / total);
    }
}
