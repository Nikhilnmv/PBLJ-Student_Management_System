<%--
    VIEW: first-time setup - create the first administrator account.
    Shown by SetupServlet (GET /setup) only while the admin table is empty.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="First-Time Setup"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body>
<div class="auth-wrapper">
    <aside class="auth-aside hero">
        <a href="${ctx}/" class="d-flex align-items-center gap-2 text-white text-decoration-none position-relative" style="z-index:1;">
            <span class="brand-mark"><i class="bi bi-mortarboard-fill"></i></span>
            <span class="fw-semibold">Student Registration System</span>
        </a>
        <div class="position-relative" style="z-index:1;">
            <span class="hero-eyebrow mb-3"><i class="bi bi-gear"></i> First-time setup</span>
            <h1 class="display-6 fw-bold mt-3 mb-4">Create the administrator account.</h1>
            <ul class="list-unstyled auth-points mb-0">
                <li><i class="bi bi-1-circle"></i><span>Choose a username and a password you will remember.</span></li>
                <li><i class="bi bi-2-circle"></i><span>The password is stored only as a BCrypt hash in MySQL.</span></li>
                <li><i class="bi bi-3-circle"></i><span>This page disappears once the account exists.</span></li>
            </ul>
        </div>
        <p class="small mb-0 position-relative" style="color: rgba(255,255,255,.55); z-index:1;">
            Shown only because the <code style="color:#fff;">admin</code> table is empty.
        </p>
    </aside>

    <main class="auth-main">
        <div class="auth-card">
            <div class="card form-card">
                <div class="card-body">
                    <span class="stat-icon tone-primary mb-3"><i class="bi bi-person-gear"></i></span>
                    <h2 class="h4 mb-1">Create administrator</h2>
                    <p class="text-muted mb-4">No administrator exists yet. Create one to use the admin panel.</p>

                    <c:if test="${not empty dbError}">
                        <div class="alert alert-danger" role="alert">
                            <i class="bi bi-database-exclamation"></i>
                            <div><strong>Database problem.</strong> <c:out value="${dbError}"/>
                                <div class="small mt-1">After fixing it, reload this page.</div></div>
                        </div>
                    </c:if>

                    <form action="${ctx}/setup" method="post" class="needs-validation" novalidate>
                        <div class="mb-3">
                            <label for="username" class="form-label">Username</label>
                            <input type="text" class="form-control${not empty errors.username ? ' is-invalid' : ''}" id="username" name="username"
                                   value="${fn:escapeXml(username)}" required minlength="4" maxlength="50" pattern="[A-Za-z0-9._]{4,50}"
                                   autocomplete="username" autofocus placeholder="e.g. admin">
                            <div class="invalid-feedback">${not empty errors.username ? fn:escapeXml(errors.username) : 'Use 4-50 letters, digits, dots or underscores.'}</div>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Password</label>
                            <input type="password" class="form-control${not empty errors.password ? ' is-invalid' : ''}" id="password" name="password"
                                   required minlength="8" maxlength="72" autocomplete="new-password" placeholder="At least 8 characters">
                            <div class="form-text">8-72 characters, with at least one letter and one digit.</div>
                            <div class="invalid-feedback">${not empty errors.password ? fn:escapeXml(errors.password) : 'Use at least 8 characters.'}</div>
                        </div>
                        <div class="mb-4">
                            <label for="confirmPassword" class="form-label">Confirm password</label>
                            <input type="password" class="form-control${not empty errors.confirmPassword ? ' is-invalid' : ''}" id="confirmPassword" name="confirmPassword"
                                   required minlength="8" maxlength="72" autocomplete="new-password" placeholder="Type the password again">
                            <div class="invalid-feedback">${not empty errors.confirmPassword ? fn:escapeXml(errors.confirmPassword) : 'Please type the password again.'}</div>
                        </div>
                        <button type="submit" class="btn btn-primary btn-lg w-100" data-loading-text="Creating account...">
                            <i class="bi bi-check2-circle me-2"></i>Create Administrator
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </main>
</div>
<%@ include file="/WEB-INF/views/common/scripts.jspf" %>
</body>
</html>
