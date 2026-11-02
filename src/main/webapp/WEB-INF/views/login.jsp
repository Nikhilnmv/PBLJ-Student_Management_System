<%--
    VIEW: administrator login page (shown by LoginServlet for GET /login,
    and again with an error message after a failed POST /login).
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Admin Login"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body>
<div class="auth-wrapper">
    <%-- Left: branding panel (hidden on small screens) --%>
    <aside class="auth-aside hero">
        <a href="${ctx}/" class="d-flex align-items-center gap-2 text-white text-decoration-none position-relative" style="z-index:1;">
            <span class="brand-mark"><i class="bi bi-mortarboard-fill"></i></span>
            <span class="fw-semibold">Student Registration System</span>
        </a>
        <div class="position-relative" style="z-index:1;">
            <span class="hero-eyebrow mb-3"><i class="bi bi-shield-lock"></i> Administrator portal</span>
            <h1 class="display-6 fw-bold mt-3 mb-4">Manage student registrations securely.</h1>
            <ul class="list-unstyled auth-points mb-0">
                <li><i class="bi bi-speedometer2"></i><span>Live dashboard with registration statistics from MySQL.</span></li>
                <li><i class="bi bi-search"></i><span>Search, filter, edit and export student records.</span></li>
                <li><i class="bi bi-lock"></i><span>BCrypt-protected passwords and session-based access.</span></li>
            </ul>
        </div>
        <p class="small mb-0 position-relative" style="color: rgba(255,255,255,.55); z-index:1;">
            Project Based Learning Using Java &middot; Servlets, JSP, JDBC &amp; MySQL
        </p>
    </aside>

    <%-- Right: login form --%>
    <main class="auth-main">
        <div class="auth-card">
            <a href="${ctx}/" class="d-inline-flex d-lg-none align-items-center gap-2 text-decoration-none mb-4">
                <span class="brand-mark"><i class="bi bi-mortarboard-fill"></i></span>
                <span class="fw-semibold text-dark">Student Registration System</span>
            </a>
            <div class="card form-card">
                <div class="card-body">
                    <span class="stat-icon tone-primary mb-3"><i class="bi bi-person-lock"></i></span>
                    <h2 class="h4 mb-1">Welcome back</h2>
                    <p class="text-muted mb-4">Sign in to the administrator dashboard.</p>

                    <c:if test="${not empty error}">
                        <div class="alert alert-danger" role="alert">
                            <i class="bi bi-exclamation-circle-fill"></i><div><c:out value="${error}"/></div>
                        </div>
                    </c:if>
                    <c:if test="${param.logout == 'true' and empty error}">
                        <div class="alert alert-success" role="alert">
                            <i class="bi bi-check-circle-fill"></i><div>You have been logged out successfully.</div>
                        </div>
                    </c:if>
                    <c:if test="${param.required == 'true' and empty error}">
                        <div class="alert alert-info" role="alert">
                            <i class="bi bi-info-circle-fill"></i><div>Please log in to access the admin area.</div>
                        </div>
                    </c:if>

                    <form action="${ctx}/login" method="post" class="needs-validation" novalidate>
                        <div class="mb-3">
                            <label for="username" class="form-label">Username</label>
                            <div class="input-icon">
                                <i class="bi bi-person"></i>
                                <input type="text" class="form-control" id="username" name="username"
                                       value="${fn:escapeXml(username)}" required maxlength="50"
                                       autocomplete="username" autofocus placeholder="Enter your username">
                                <div class="invalid-feedback">Please enter your username.</div>
                            </div>
                        </div>
                        <div class="mb-4">
                            <label for="password" class="form-label">Password</label>
                            <div class="input-icon">
                                <i class="bi bi-key"></i>
                                <input type="password" class="form-control" id="password" name="password"
                                       required maxlength="72" autocomplete="current-password" placeholder="Enter your password">
                                <div class="invalid-feedback">Please enter your password.</div>
                            </div>
                        </div>
                        <button type="submit" class="btn btn-primary btn-lg w-100" data-loading-text="Signing in...">
                            <i class="bi bi-box-arrow-in-right me-2"></i>Sign In
                        </button>
                    </form>
                </div>
            </div>
            <p class="text-center text-muted small mt-4 mb-0">
                Are you a student? <a href="${ctx}/register" class="fw-medium">Register here</a>
            </p>
        </div>
    </main>
</div>
<%@ include file="/WEB-INF/views/common/scripts.jspf" %>
</body>
</html>
