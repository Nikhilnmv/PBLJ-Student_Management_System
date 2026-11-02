package com.student.model;

import com.student.util.CsrfUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the Student and StudentFilter model helper methods. */
class StudentTest {

    @Test
    void newStudentIsActiveByDefault() {
        assertEquals("Active", new Student().getStatus());
        assertTrue(new Student().isActive());
    }

    @Test
    void registrationIdIsPaddedStudentId() {
        Student student = new Student();
        student.setStudentId(12);
        assertEquals("SRS-00012", student.getRegistrationId());
        student.setStudentId(123456);
        assertEquals("SRS-123456", student.getRegistrationId());
    }

    @Test
    void initialsUseFirstAndLastName() {
        Student student = new Student();
        student.setFullName("aarav kumar mehta");
        assertEquals("AM", student.getInitials());
        student.setFullName("Zara");
        assertEquals("Z", student.getInitials());
        student.setFullName(" ");
        assertEquals("?", student.getInitials());
    }

    @Test
    void datesAreFormattedForDisplay() {
        Student student = new Student();
        student.setDob(LocalDate.of(2005, 3, 14));
        student.setCreatedAt(LocalDateTime.of(2026, 11, 2, 17, 30));
        assertEquals("14 Mar 2005", student.getDobFormatted());
        assertEquals("2005-03-14", student.getDobIso());
        assertEquals("02 Nov 2026", student.getCreatedDateFormatted());
        assertEquals("02 Nov 2026, 05:30 PM", student.getCreatedAtFormatted());
    }

    @Test
    void ageIsCalculatedFromDateOfBirth() {
        Student student = new Student();
        student.setDob(LocalDate.now().minusYears(20));
        assertEquals(20, student.getAge());
    }

    @Test
    void filterKnowsWhetherAnyOptionIsChosen() {
        assertFalse(new StudentFilter().isActive());
        assertFalse(new StudentFilter(" ", "", null, null).isActive());
        assertTrue(new StudentFilter("mehta", null, null, null).isActive());
        assertTrue(new StudentFilter(null, null, 3, null).isActive());
    }

    @Test
    void csrfTokensAreLongAndRandom() {
        String first = CsrfUtil.generateToken();
        String second = CsrfUtil.generateToken();
        assertEquals(43, first.length());  // 32 random bytes in Base64 without padding
        assertNotEquals(first, second);
        assertFalse(CsrfUtil.isValid(null, first));
    }
}
