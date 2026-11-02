package com.student.util;

import com.student.model.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for ValidationUtil (no database or server needed).
 * Run with:  mvn test
 */
class ValidationUtilTest {

    /** A student whose every field is valid; each test changes one field. */
    private Student validStudent() {
        return new Student("23CSE001", "Aarav Mehta", "aarav@example.com", "9876543210", "Male",
                LocalDate.now().minusYears(20), "Computer Science and Engineering", "B.E.", 3,
                "12 Green Park, Chandigarh");
    }

    @Test
    @DisplayName("A completely valid student has no errors")
    void validStudentHasNoErrors() {
        assertTrue(ValidationUtil.validateStudent(validStudent()).isEmpty());
    }

    @Test
    @DisplayName("An empty student reports every required field")
    void emptyStudentReportsAllRequiredFields() {
        Student empty = new Student();  // status is Active by default
        Map<String, String> errors = ValidationUtil.validateStudent(empty);
        assertEquals(10, errors.size());
        assertEquals("Full name is required.", errors.get("fullName"));
        assertEquals("Roll number is required.", errors.get("rollNumber"));
        assertEquals("Email address is required.", errors.get("email"));
        assertEquals("Phone number is required.", errors.get("phone"));
        assertTrue(errors.containsKey("gender"));
        assertTrue(errors.containsKey("dob"));
        assertTrue(errors.containsKey("department"));
        assertTrue(errors.containsKey("course"));
        assertTrue(errors.containsKey("semester"));
        assertEquals("Address is required.", errors.get("address"));
    }

    @Test
    void emailValidation() {
        assertTrue(ValidationUtil.isValidEmail("name@example.com"));
        assertTrue(ValidationUtil.isValidEmail("first.last+tag@college.ac.in"));
        assertFalse(ValidationUtil.isValidEmail("not-an-email"));
        assertFalse(ValidationUtil.isValidEmail("missing@tld"));
        assertFalse(ValidationUtil.isValidEmail("two@@example.com"));
        assertFalse(ValidationUtil.isValidEmail("space in@example.com"));
        assertFalse(ValidationUtil.isValidEmail("a@b..com"));
        assertFalse(ValidationUtil.isValidEmail(null));
        assertFalse(ValidationUtil.isValidEmail("a".repeat(95) + "@x.com")); // longer than 100
    }

    @Test
    void phoneValidation() {
        assertTrue(ValidationUtil.isValidPhone("9876543210"));
        assertTrue(ValidationUtil.isValidPhone("6000000000"));
        assertFalse(ValidationUtil.isValidPhone("5876543210"));   // must start with 6-9
        assertFalse(ValidationUtil.isValidPhone("98765"));        // too short
        assertFalse(ValidationUtil.isValidPhone("98765432101"));  // too long
        assertFalse(ValidationUtil.isValidPhone("98765abcde"));   // letters
        assertFalse(ValidationUtil.isValidPhone("+919876543210"));
    }

    @Test
    void nameValidation() {
        assertTrue(ValidationUtil.isValidName("Aarav Mehta"));
        assertTrue(ValidationUtil.isValidName("Mary-Jane O'Neil"));
        assertTrue(ValidationUtil.isValidName("Zoë Dr. Kṛṣṇa"));   // letters from other languages
        assertFalse(ValidationUtil.isValidName("A"));              // too short
        assertFalse(ValidationUtil.isValidName("Robert123"));      // digits
        assertFalse(ValidationUtil.isValidName("<script>"));       // HTML characters
        assertFalse(ValidationUtil.isValidName("x".repeat(101)));  // too long
    }

    @Test
    void rollNumberValidation() {
        assertTrue(ValidationUtil.isValidRollNumber("23CSE001"));
        assertTrue(ValidationUtil.isValidRollNumber("2023/IT-045"));
        assertFalse(ValidationUtil.isValidRollNumber("AB"));                       // too short
        assertFalse(ValidationUtil.isValidRollNumber("23cse001"));                 // must be upper case (servlet converts)
        assertFalse(ValidationUtil.isValidRollNumber("23 CSE 001"));               // spaces
        assertFalse(ValidationUtil.isValidRollNumber("' OR '1'='1"));              // SQL injection text
        assertFalse(ValidationUtil.isValidRollNumber("A123456789012345678901"));   // longer than 20
    }

    @Test
    void adminUsernameValidation() {
        assertTrue(ValidationUtil.isValidUsername("admin"));
        assertTrue(ValidationUtil.isValidUsername("office.staff_01"));
        assertFalse(ValidationUtil.isValidUsername("abc"));           // too short
        assertFalse(ValidationUtil.isValidUsername("with space"));
        assertFalse(ValidationUtil.isValidUsername("admin'--"));      // SQL characters
        assertFalse(ValidationUtil.isValidUsername(null));
    }

    @Test
    void semesterRange() {
        assertFalse(ValidationUtil.isValidSemester(0));
        assertTrue(ValidationUtil.isValidSemester(1));
        assertTrue(ValidationUtil.isValidSemester(8));
        assertFalse(ValidationUtil.isValidSemester(9));
        assertFalse(ValidationUtil.isValidSemester(-1));
    }

    @Test
    void dateOfBirthRange() {
        LocalDate today = LocalDate.now();
        assertTrue(ValidationUtil.isValidDateOfBirth(today.minusYears(18)));
        assertTrue(ValidationUtil.isValidDateOfBirth(today.minusYears(15)));     // exactly 15 today
        assertFalse(ValidationUtil.isValidDateOfBirth(today.minusYears(15).plusDays(1)));
        assertFalse(ValidationUtil.isValidDateOfBirth(today.plusDays(1)));       // future
        assertFalse(ValidationUtil.isValidDateOfBirth(today.minusYears(61)));    // too old
        assertFalse(ValidationUtil.isValidDateOfBirth(null));
    }

    @Test
    void dropDownValuesMustComeFromTheLists() {
        assertTrue(ValidationUtil.isValidDepartment("Information Technology"));
        assertFalse(ValidationUtil.isValidDepartment("Hacking 101"));
        assertTrue(ValidationUtil.isValidCourse("BCA"));
        assertFalse(ValidationUtil.isValidCourse("PhD"));
        assertTrue(ValidationUtil.isValidGender("Other"));
        assertFalse(ValidationUtil.isValidGender("male"));   // exact value required
        assertTrue(ValidationUtil.isValidStatus("Inactive"));
        assertFalse(ValidationUtil.isValidStatus("Deleted"));
    }

    @Test
    void addressLength() {
        assertTrue(ValidationUtil.isValidAddress("Pune 1"));
        assertFalse(ValidationUtil.isValidAddress("abc"));
        assertFalse(ValidationUtil.isValidAddress("x".repeat(501)));
    }

    @Test
    void safeConversions() {
        assertEquals(5, ValidationUtil.parseInteger(" 5 "));
        assertNull(ValidationUtil.parseInteger("five"));
        assertNull(ValidationUtil.parseInteger(""));
        assertEquals(LocalDate.of(2005, 3, 14), ValidationUtil.parseDate("2005-03-14"));
        assertNull(ValidationUtil.parseDate("14/03/2005"));
        assertNull(ValidationUtil.parseDate("2005-02-30"));  // no such date
        assertEquals("Aarav Mehta", ValidationUtil.normalizeSpaces("  Aarav    Mehta "));
    }

    @Test
    @DisplayName("SQL injection text in email/phone is rejected as invalid data")
    void sqlInjectionTextIsRejectedAsInvalidData() {
        Student student = validStudent();
        student.setEmail("x@example.com' OR '1'='1");
        student.setPhone("1 OR 1=1");
        Map<String, String> errors = ValidationUtil.validateStudent(student);
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("phone"));
    }
}
