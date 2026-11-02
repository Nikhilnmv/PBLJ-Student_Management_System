package com.student.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hashes and checks administrator passwords with BCrypt.
 *
 * We NEVER store the real password. Instead we store a BCrypt hash such as
 *   $2a$12$Qm3Tq... (60 characters)
 *
 * - A hash is one-way: you cannot turn it back into the password.
 * - BCrypt adds a random "salt", so the same password gives a different hash
 *   each time. This defeats pre-computed "rainbow table" attacks.
 * - BCrypt is deliberately slow (cost factor 12), which makes guessing
 *   millions of passwords impractical if the database is ever stolen.
 */
public final class PasswordUtil {

    /** Work factor: each +1 doubles the time needed to compute a hash. */
    private static final int BCRYPT_COST = 12;

    /** Minimum password length accepted by the admin setup tool. */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /** BCrypt only uses the first 72 bytes of a password, so longer ones are rejected. */
    public static final int MAX_PASSWORD_LENGTH = 72;

    private PasswordUtil() {
    }

    /** Creates a salted BCrypt hash of the given password. */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Checks whether a password matches a stored hash.
     * BCrypt reads the salt from the stored hash, hashes the typed password
     * the same way, and compares the two results.
     *
     * @return true only if the password is correct
     */
    public static boolean checkPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || !storedHash.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // The stored value is not a valid BCrypt hash
            return false;
        }
    }

    /**
     * Basic strength rule for admin passwords:
     * 8-72 characters with at least one letter and one digit.
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH
                || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char ch : password.toCharArray()) {
            if (Character.isLetter(ch)) {
                hasLetter = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }
}
