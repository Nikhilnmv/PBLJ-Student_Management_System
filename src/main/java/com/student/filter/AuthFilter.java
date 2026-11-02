package com.student.filter;

import com.student.util.CsrfUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * SECURITY GUARD for every administrator page.
 *
 * A Filter runs BEFORE the servlet. Because of @WebFilter("/admin/*"),
 * Tomcat sends every request whose URL starts with /admin/ through this
 * class first. For each request it checks:
 *
 *   1. Is an administrator logged in? (Is there a "loggedInAdmin" in the session?)
 *      If not, the browser is redirected to the login page.
 *   2. For POST requests (create, update, delete, logout): does the hidden
 *      CSRF token match the one stored in the session? If not, the request
 *      is refused with HTTP 403 Forbidden.
 *   3. Tells the browser not to cache admin pages, so after logging out the
 *      Back button cannot show private data again.
 *
 * Only if all checks pass does chain.doFilter(...) pass the request on to
 * the servlet. This keeps the security check in ONE place instead of
 * repeating it in every admin servlet.
 */
@WebFilter("/admin/*")
public class AuthFilter implements Filter {

    /** Session attribute that holds the logged-in Admin (set by LoginServlet). */
    public static final String ADMIN_SESSION_ATTRIBUTE = "loggedInAdmin";

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to initialise
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // getSession(false): use the existing session, but do NOT create a new one
        HttpSession session = request.getSession(false);
        boolean loggedIn = session != null && session.getAttribute(ADMIN_SESSION_ATTRIBUTE) != null;

        if (!loggedIn) {
            response.sendRedirect(request.getContextPath() + "/login?required=true");
            return;  // stop here: the admin servlet is never called
        }

        if ("POST".equalsIgnoreCase(request.getMethod())
                && !CsrfUtil.isValid(session, request.getParameter(CsrfUtil.TOKEN_NAME))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Do not let the browser store copies of admin pages
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // All checks passed: continue to the requested admin servlet
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nothing to clean up
    }
}
