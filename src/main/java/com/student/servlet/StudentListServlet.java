package com.student.servlet;

import com.student.dao.StudentDAO;
import com.student.model.Student;
import com.student.model.StudentFilter;
import com.student.util.AppConstants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * CONTROLLER for the "All Students" page: list, search, filter and paging.
 *
 * URL: /admin/students (GET)
 * Optional URL parameters:
 *   keyword    - searched in name, roll number and email
 *   department - exact department
 *   semester   - 1 to 8
 *   status     - Active or Inactive
 *   page       - page number (10 students per page)
 *
 * Example: /admin/students?keyword=mehta&semester=3&page=1
 * Searching uses GET (not POST) because it only READS data, and the
 * resulting URL can be bookmarked or shared.
 */
@WebServlet("/admin/students")
public class StudentListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Number of students shown on one page. */
    static final int PAGE_SIZE = 10;

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StudentFilter filter = StudentRequestHelper.readFilterFromRequest(request);

        try {
            // 1. How many students match? -> how many pages are needed?
            int totalMatches = studentDAO.countStudents(filter);
            int totalPages = Math.max(1, (int) Math.ceil(totalMatches / (double) PAGE_SIZE));

            // 2. Which page was requested? Keep it between 1 and totalPages.
            int page = StudentRequestHelper.readPositiveInt(request, "page");
            if (page < 1) {
                page = 1;
            }
            page = Math.min(page, totalPages);

            // 3. Load only the students for this page (LIMIT / OFFSET in SQL)
            int offset = (page - 1) * PAGE_SIZE;
            List<Student> students = studentDAO.searchStudents(filter, offset, PAGE_SIZE);

            // 4. Give the JSP everything it needs
            request.setAttribute("students", students);
            request.setAttribute("filter", filter);
            request.setAttribute("filterQuery", StudentRequestHelper.buildFilterQuery(filter));
            request.setAttribute("totalMatches", totalMatches);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("firstRow", totalMatches == 0 ? 0 : offset + 1);
            request.setAttribute("lastRow", offset + students.size());
            request.setAttribute("departments", AppConstants.DEPARTMENTS);
            request.setAttribute("semesters", AppConstants.SEMESTERS);
            request.setAttribute("statuses", AppConstants.STATUSES);

            request.getRequestDispatcher("/WEB-INF/views/admin/students.jsp").forward(request, response);

        } catch (SQLException e) {
            log("Could not load the student list", e);
            throw new ServletException("Could not load the student list.", e);
        }
    }
}
