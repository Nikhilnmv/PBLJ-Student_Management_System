package com.student.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for BCrypt password hashing. */
class PasswordUtilTest {

    @Test
    void hashIsNotThePlainPasswordAndCanBeVerified() {
        String hash = PasswordUtil.hashPassword("Secret123");
        assertNotEquals("Secret123", hash);
        assertTrue(hash.startsWith("$2a$12$"), "BCrypt hash with cost 12");
        assertEquals(60, hash.length());
        assertTrue(PasswordUtil.checkPassword("Secret123", hash));
    }

    @Test
    void wrongPasswordIsRejected() {
        String hash = PasswordUtil.hashPassword("Secret123");
        assertFalse(PasswordUtil.checkPassword("secret123", hash));   // case matters
        assertFalse(PasswordUtil.checkPassword("' OR '1'='1", hash));  // SQL injection text
        assertFalse(PasswordUtil.checkPassword("", hash));
        assertFalse(PasswordUtil.checkPassword(null, hash));
    }

    @Test
    void samePasswordGivesDifferentHashesBecauseOfRandomSalt() {
        String first = PasswordUtil.hashPassword("Secret123");
        String second = PasswordUtil.hashPassword("Secret123");
        assertNotEquals(first, second);
        assertTrue(PasswordUtil.checkPassword("Secret123", first));
        assertTrue(PasswordUtil.checkPassword("Secret123", second));
    }

    @Test
    void invalidStoredHashNeverMatches() {
        assertFalse(PasswordUtil.checkPassword("Secret123", "Secret123"));  // plain text in DB
        assertFalse(PasswordUtil.checkPassword("Secret123", "$2a$invalid"));
        assertFalse(PasswordUtil.checkPassword("Secret123", null));
    }

    @Test
    void passwordStrengthRules() {
        assertTrue(PasswordUtil.isStrongPassword("Admin2026"));
        assertFalse(PasswordUtil.isStrongPassword("short1"));        // fewer than 8 characters
        assertFalse(PasswordUtil.isStrongPassword("onlyletters"));   // no digit
        assertFalse(PasswordUtil.isStrongPassword("12345678"));      // no letter
        assertFalse(PasswordUtil.isStrongPassword("a1".repeat(37))); // longer than 72
        assertFalse(PasswordUtil.isStrongPassword(null));
    }
}
