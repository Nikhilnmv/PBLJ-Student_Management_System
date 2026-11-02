package com.student.util;

import com.student.model.Student;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SERVER-SIDE validation of the data typed into the student forms.
 *
 * Why validate on the server when the browser already checks the form?
 * Because browser (JavaScript/HTML) checks can be switched off or skipped
 * by sending a request directly. The server is the only place the
 * application fully controls, so every rule is checked again here.
 *
 * All methods are static: they do not need any object state.
 */
public final class ValidationUtil {

    /*
     * Regular expressions (patterns) for the text fields.
     * Pattern.compile(...) is done once, here, because compiling is slow.
     */

    /** Letters from any language (\p{L}), plus space, dot, apostrophe and hyphen. 2-100 characters. */
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\p{L}[\\p{L} .'-]{1,99}$");

    /** Upper-case letters, digits, '/' and '-'. 3-20 characters, e.g. 23CSE001 or 2023/IT/045. */
    private static final Pattern ROLL_NUMBER_PATTERN = Pattern.compile("^[A-Z0-9][A-Z0-9/-]{2,19}$");

    /** name@domain.tld - a practical (not perfect) email check. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    /** Indian 10-digit mobile number that starts with 6, 7, 8 or 9. */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");

    /** Administrator usernames: 4-50 letters, digits, dots or underscores. */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._]{4,50}$");

    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MIN_ADDRESS_LENGTH = 5;
    public static final int MAX_ADDRESS_LENGTH = 500;

    private ValidationUtil() {
    }

    // ------------------------------------------------------------------
    // Validation of a complete Student object
    // ------------------------------------------------------------------

    /**
     * Checks every field of a student.
     *
     * @return a map of field name -> error message. An EMPTY map means the
     *         student is valid. LinkedHashMap keeps the errors in form order.
     */
    public static Map<String, String> validateStudent(Student student) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(student.getFullName())) {
            errors.put("fullName", "Full name is required.");
        } else if (!isValidName(student.getFullName())) {
            errors.put("fullName", "Name must be 2-100 characters and contain only letters, spaces, dots, apostrophes or hyphens.");
        }

        if (isBlank(student.getRollNumber())) {
            errors.put("rollNumber", "Roll number is required.");
        } else if (!isValidRollNumber(student.getRollNumber())) {
            errors.put("rollNumber", "Roll number must be 3-20 characters: letters, digits, '/' or '-'.");
        }

        if (isBlank(student.getEmail())) {
            errors.put("email", "Email address is required.");
        } else if (!isValidEmail(student.getEmail())) {
            errors.put("email", "Please enter a valid email address, e.g. name@example.com.");
        }

        if (isBlank(student.getPhone())) {
            errors.put("phone", "Phone number is required.");
        } else if (!isValidPhone(student.getPhone())) {
            errors.put("phone", "Phone number must be 10 digits and start with 6, 7, 8 or 9.");
        }

        if (!isValidGender(student.getGender())) {
            errors.put("gender", "Please select a gender.");
        }

        if (student.getDob() == null) {
            errors.put("dob", "Please enter a valid date of birth.");
        } else if (!isValidDateOfBirth(student.getDob())) {
            errors.put("dob", "Age must be between " + AppConstants.MIN_STUDENT_AGE + " and "
                    + AppConstants.MAX_STUDENT_AGE + " years.");
        }

        if (!isValidDepartment(student.getDepartment())) {
            errors.put("department", "Please select a department from the list.");
        }

        if (!isValidCourse(student.getCourse())) {
            errors.put("course", "Please select a course from the list.");
        }

        if (!isValidSemester(student.getSemester())) {
            errors.put("semester", "Semester must be between " + AppConstants.MIN_SEMESTER + " and "
                    + AppConstants.MAX_SEMESTER + ".");
        }

        if (isBlank(student.getAddress())) {
            errors.put("address", "Address is required.");
        } else if (!isValidAddress(student.getAddress())) {
            errors.put("address", "Address must be " + MIN_ADDRESS_LENGTH + "-" + MAX_ADDRESS_LENGTH + " characters long.");
        }

        if (!isValidStatus(student.getStatus())) {
            errors.put("status", "Status must be Active or Inactive.");
        }

        return errors;
    }

    // ------------------------------------------------------------------
    // Individual field checks (public so they can be unit tested)
    // ------------------------------------------------------------------

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        return name != null && name.length() <= MAX_NAME_LENGTH && NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isValidRollNumber(String rollNumber) {
        return rollNumber != null && ROLL_NUMBER_PATTERN.matcher(rollNumber).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.length() <= MAX_EMAIL_LENGTH && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidGender(String gender) {
        return gender != null && AppConstants.GENDERS.contains(gender);
    }

    /** Date of birth must not be in the future and the age must be within the allowed range. */
    public static boolean isValidDateOfBirth(LocalDate dob) {
        if (dob == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        LocalDate youngestAllowed = today.minusYears(AppConstants.MIN_STUDENT_AGE); // born on/before this date
        LocalDate oldestAllowed = today.minusYears(AppConstants.MAX_STUDENT_AGE);   // born on/after this date
        return !dob.isAfter(youngestAllowed) && !dob.isBefore(oldestAllowed);
    }

    public static boolean isValidDepartment(String department) {
        return department != null && AppConstants.DEPARTMENTS.contains(department);
    }

    public static boolean isValidCourse(String course) {
        return course != null && AppConstants.COURSES.contains(course);
    }

    public static boolean isValidSemester(int semester) {
        return semester >= AppConstants.MIN_SEMESTER && semester <= AppConstants.MAX_SEMESTER;
    }

    public static boolean isValidAddress(String address) {
        if (address == null) {
            return false;
        }
        int length = address.trim().length();
        return length >= MIN_ADDRESS_LENGTH && length <= MAX_ADDRESS_LENGTH;
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidStatus(String status) {
        return status != null && AppConstants.STATUSES.contains(status);
    }

    // ------------------------------------------------------------------
    // Safe conversion helpers (text from a form -> Java types)
    // ------------------------------------------------------------------

    /**
     * Converts text such as "5" to the number 5.
     * Returns null instead of throwing an exception when the text is not a number.
     */
    public static Integer parseInteger(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converts text in yyyy-MM-dd format (what an HTML date input sends) to a LocalDate.
     * Returns null when the text is not a valid date.
     */
    public static LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Cleans user input: removes spaces at both ends and replaces runs of
     * spaces inside the text with one space. Returns null for null input.
     */
    public static String normalizeSpaces(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
