package com.student.dao;

/**
 * Thrown by StudentDAO when MySQL rejects a student because the roll number
 * or email address already belongs to another student.
 *
 * This is a CUSTOM EXCEPTION: it INHERITS from Java's Exception class
 * ("extends Exception") and adds one extra piece of information - the name
 * of the form field that caused the problem - so the servlet can show the
 * error message next to the right input box.
 */
public class DuplicateStudentException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Form field that is duplicated: "rollNumber" or "email". */
    private final String field;

    public DuplicateStudentException(String field, String message, Throwable cause) {
        super(message, cause);  // pass the message and original cause to the parent class (Exception)
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
