package com.student.util;

import java.util.List;

/**
 * Fixed lists of choices used by the forms and by validation.
 *
 * Keeping them in ONE place means the drop-down menus on the JSP pages and
 * the server-side validation always agree with each other.
 * List.of(...) creates an unmodifiable list, so no code can change it by accident.
 */
public final class AppConstants {

    /** Departments offered by the college (shown in the Department drop-down). */
    public static final List<String> DEPARTMENTS = List.of(
            "Computer Science and Engineering",
            "Information Technology",
            "Electronics and Communication Engineering",
            "Electrical Engineering",
            "Mechanical Engineering",
            "Civil Engineering",
            "Computer Applications"
    );

    /** Degree programmes (shown in the Course drop-down). */
    public static final List<String> COURSES = List.of(
            "B.E.",
            "B.Tech",
            "M.E.",
            "M.Tech",
            "BCA",
            "MCA"
    );

    /** Allowed gender values (must match the CHECK constraint in schema.sql). */
    public static final List<String> GENDERS = List.of("Male", "Female", "Other");

    /** Allowed status values (must match the CHECK constraint in schema.sql). */
    public static final List<String> STATUSES = List.of("Active", "Inactive");

    /** Semester range (must match the CHECK constraint in schema.sql). */
    public static final int MIN_SEMESTER = 1;
    public static final int MAX_SEMESTER = 8;

    /** Semesters 1 to 8, used to build the Semester drop-down. */
    public static final List<Integer> SEMESTERS = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    /** A student must be between these ages (in years) on the day they register. */
    public static final int MIN_STUDENT_AGE = 15;
    public static final int MAX_STUDENT_AGE = 60;

    private AppConstants() {
        // utility class: no objects needed
    }
}
