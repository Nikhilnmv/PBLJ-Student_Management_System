package com.student.util;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Protection against CSRF (Cross-Site Request Forgery).
 *
 * The attack: while an admin is logged in, a malicious website secretly
 * submits a form to /admin/students/delete. The browser automatically sends
 * the admin's session cookie, so without protection the delete would succeed.
 *
 * The defence:
 *  1. When the admin logs in, we create a long random secret (the token)
 *     and store it in the session.
 *  2. Every admin form includes the token in a hidden field.
 *  3. AuthFilter rejects any POST request whose token does not match.
 * Another website cannot read our pages, so it cannot know the token.
 */
public final class CsrfUtil {

    /** Name of the session attribute AND of the hidden form field. */
    public static final String TOKEN_NAME = "csrfToken";

    /** SecureRandom produces unpredictable numbers suitable for security. */
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    /** Creates a new random token (32 random bytes, Base64-encoded). */
    public static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Returns true if the submitted token equals the token stored in the session.
     * MessageDigest.isEqual compares in constant time, so an attacker cannot
     * learn the token by measuring how long the comparison takes.
     */
    public static boolean isValid(HttpSession session, String submittedToken) {
        if (session == null || submittedToken == null) {
            return false;
        }
        Object expected = session.getAttribute(TOKEN_NAME);
        if (!(expected instanceof String)) {
            return false;
        }
        return MessageDigest.isEqual(
                ((String) expected).getBytes(StandardCharsets.UTF_8),
                submittedToken.getBytes(StandardCharsets.UTF_8));
    }
}
