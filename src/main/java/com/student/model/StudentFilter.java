package com.student.model;

/**
 * Holds the search and filter options chosen on the "All Students" page.
 *
 * Instead of passing four separate values to every DAO method, the servlet
 * puts them into one StudentFilter object. A null (or empty) value means
 * "do not filter by this".
 */
public class StudentFilter {

    private String keyword;      // matches name, roll number or email
    private String department;   // exact department name
    private Integer semester;    // 1 to 8; Integer (not int) so it can be null
    private String status;       // "Active" or "Inactive"

    public StudentFilter() {
    }

    public StudentFilter(String keyword, String department, Integer semester, String status) {
        this.keyword = keyword;
        this.department = department;
        this.semester = semester;
        this.status = status;
    }

    /** True if at least one search or filter option has been chosen. */
    public boolean isActive() {
        return hasText(keyword) || hasText(department) || semester != null || hasText(status);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
