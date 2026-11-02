package com.student.servlet;

import com.student.dao.StudentDAO;
import com.student.model.Student;
import com.student.model.StudentFilter;
import com.student.util.AppConstants;
import com.student.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

/**
 * Small helper methods shared by several servlets.
 *
 * The public registration form and the admin "Add / Edit Student" forms
 * contain the same fields. Instead of copying the same code into three
 * servlets, the common steps live here:
 *   - reading form fields into a Student object
 *   - validating the student (format checks + duplicate checks)
 *   - putting the drop-down lists into the request for the JSP
 *   - reading the search/filter options of the student list
 */
final class StudentRequestHelper {

    /** Name of the request attribute that holds validation errors (a Map). */
    static final String ERRORS_ATTRIBUTE = "errors";

    private StudentRequestHelper() {
    }

    /**
     * Reads the submitted form fields and builds a Student object.
     * Text is trimmed; roll numbers are stored in upper case and emails in
     * lower case so "23cse001" and "23CSE001" count as the same roll number.
     */
    static Student readStudentFromRequest(HttpServletRequest request) {
        Student student = new Student();
        student.setFullName(ValidationUtil.normalizeSpaces(request.getParameter("fullName")));

        String rollNumber = ValidationUtil.normalizeSpaces(request.getParameter("rollNumber"));
        student.setRollNumber(rollNumber == null ? null : rollNumber.toUpperCase(Locale.ROOT));

        String email = ValidationUtil.normalizeSpaces(request.getParameter("email"));
        student.setEmail(email == null ? null : email.toLowerCase(Locale.ROOT));

        student.setPhone(ValidationUtil.normalizeSpaces(request.getParameter("phone")));
        student.setGender(request.getParameter("gender"));
        student.setDob(ValidationUtil.parseDate(request.getParameter("dob")));
        student.setDepartment(request.getParameter("department"));
        student.setCourse(request.getParameter("course"));

        Integer semester = ValidationUtil.parseInteger(request.getParameter("semester"));
        student.setSemester(semester == null ? 0 : semester);  // 0 = not chosen; fails validation

        String address = request.getParameter("address");
        student.setAddress(address == null ? null : address.trim());

        String status = request.getParameter("status");
        if (status != null) {
            student.setStatus(status);
        }
        return student;
    }

    /**
     * Validates a student in two steps:
     *   1. Format rules (ValidationUtil) - required fields, email, phone, semester...
     *   2. Uniqueness - asks the database whether the roll number or email is already used.
     *
     * @param excludeStudentId 0 for a new student; the student's own ID when editing,
     *                         so a student is not reported as a duplicate of itself
     * @return field name -> error message (empty when everything is valid)
     */
    static Map<String, String> validateStudent(Student student, StudentDAO studentDAO, int excludeStudentId)
            throws SQLException {
        Map<String, String> errors = ValidationUtil.validateStudent(student);

        if (!errors.containsKey("rollNumber")
                && studentDAO.isRollNumberTaken(student.getRollNumber(), excludeStudentId)) {
            errors.put("rollNumber", "This roll number is already registered.");
        }
        if (!errors.containsKey("email")
                && studentDAO.isEmailTaken(student.getEmail(), excludeStudentId)) {
            errors.put("email", "This email address is already registered.");
        }
        return errors;
    }

    /** Puts the drop-down lists and date limits used by the student form into the request. */
    static void setFormOptions(HttpServletRequest request) {
        request.setAttribute("departments", AppConstants.DEPARTMENTS);
        request.setAttribute("courses", AppConstants.COURSES);
        request.setAttribute("genders", AppConstants.GENDERS);
        request.setAttribute("semesters", AppConstants.SEMESTERS);
        request.setAttribute("statuses", AppConstants.STATUSES);
        LocalDate today = LocalDate.now();
        request.setAttribute("maxDob", today.minusYears(AppConstants.MIN_STUDENT_AGE));
        request.setAttribute("minDob", today.minusYears(AppConstants.MAX_STUDENT_AGE));
    }

    /**
     * Reads the search box and filter drop-downs of the student list.
     * Unknown values (for example a department that is not in the list) are
     * ignored, so the filter only ever contains sensible values.
     */
    static StudentFilter readFilterFromRequest(HttpServletRequest request) {
        StudentFilter filter = new StudentFilter();

        String keyword = ValidationUtil.normalizeSpaces(request.getParameter("keyword"));
        if (keyword != null && !keyword.isEmpty()) {
            filter.setKeyword(keyword.length() > 100 ? keyword.substring(0, 100) : keyword);
        }

        String department = request.getParameter("department");
        if (ValidationUtil.isValidDepartment(department)) {
            filter.setDepartment(department);
        }

        Integer semester = ValidationUtil.parseInteger(request.getParameter("semester"));
        if (semester != null && ValidationUtil.isValidSemester(semester)) {
            filter.setSemester(semester);
        }

        String status = request.getParameter("status");
        if (ValidationUtil.isValidStatus(status)) {
            filter.setStatus(status);
        }
        return filter;
    }

    /**
     * Turns the filter back into URL parameters, e.g.
     * "keyword=mehta&department=Civil+Engineering&". Used to keep the filter
     * when moving between pages and when exporting to CSV.
     */
    static String buildFilterQuery(StudentFilter filter) {
        StringBuilder query = new StringBuilder();
        appendParameter(query, "keyword", filter.getKeyword());
        appendParameter(query, "department", filter.getDepartment());
        appendParameter(query, "semester", filter.getSemester() == null ? null : filter.getSemester().toString());
        appendParameter(query, "status", filter.getStatus());
        return query.toString();
    }

    private static void appendParameter(StringBuilder query, String name, String value) {
        if (value != null && !value.isEmpty()) {
            query.append(name).append('=')
                 .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                 .append('&');
        }
    }

    /**
     * Stores a one-time success message in the session. It is shown on the
     * next page (see flash-messages.jspf) and then removed.
     */
    static void setFlashSuccess(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashSuccess", message);
    }

    /** Stores a one-time error message in the session (shown on the next page). */
    static void setFlashError(HttpServletRequest request, String message) {
        request.getSession().setAttribute("flashError", message);
    }

    /**
     * Reads a positive whole number (such as a student ID) from a request parameter.
     * Returns -1 when the parameter is missing or not a positive number.
     */
    static int readPositiveInt(HttpServletRequest request, String parameterName) {
        Integer value = ValidationUtil.parseInteger(request.getParameter(parameterName));
        return (value == null || value <= 0) ? -1 : value;
    }
}
