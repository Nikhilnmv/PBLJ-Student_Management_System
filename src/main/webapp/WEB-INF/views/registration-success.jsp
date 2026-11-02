<%--
    VIEW: shown after a successful registration (GET /register/success).
    The "student" request attribute is the row just saved in MySQL.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Registration Successful"/>
<c:set var="activeNav" value="register"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/public-navbar.jspf" %>

<header class="hero hero-page">
    <div class="container text-center">
        <span class="hero-eyebrow"><i class="bi bi-patch-check"></i> Registration complete</span>
    </div>
</header>

<main class="container overlap-section pb-5">
    <div class="row justify-content-center">
        <div class="col-lg-7 col-xl-6">
            <div class="card form-card">
                <div class="card-body text-center">
                    <div class="print-only mb-3">
                        <h1 class="h4">Student Registration System</h1>
                        <p class="text-muted">Registration acknowledgement</p>
                    </div>

                    <div class="success-icon no-print"><i class="bi bi-check-lg"></i></div>
                    <h1 class="h3 mb-2">Registration Successful!</h1>
                    <p class="text-muted mb-4">
                        Thank you, <strong class="text-dark"><c:out value="${student.fullName}"/></strong>.
                        Your details have been saved.
                    </p>

                    <div class="registration-ticket mb-4">
                        <div class="small-caps text-muted mb-1">Your Registration ID</div>
                        <div class="ticket-id"><c:out value="${student.registrationId}"/></div>
                        <div class="small text-muted mt-1">Please note this ID for future reference.</div>
                    </div>

                    <dl class="detail-list text-start">
                        <div class="detail-row"><dt>Roll Number</dt><dd class="mono"><c:out value="${student.rollNumber}"/></dd></div>
                        <div class="detail-row"><dt>Email</dt><dd><c:out value="${student.email}"/></dd></div>
                        <div class="detail-row"><dt>Department</dt><dd><c:out value="${student.department}"/></dd></div>
                        <div class="detail-row"><dt>Course &amp; Semester</dt><dd><c:out value="${student.course}"/> &middot; Semester <c:out value="${student.semester}"/></dd></div>
                        <div class="detail-row"><dt>Registered On</dt><dd><c:out value="${student.createdAtFormatted}"/></dd></div>
                        <div class="detail-row"><dt>Status</dt><dd><span class="status-badge status-active"><c:out value="${student.status}"/></span></dd></div>
                    </dl>

                    <div class="d-flex flex-column flex-sm-row gap-2 justify-content-center mt-4 no-print">
                        <button type="button" class="btn btn-light-soft" data-action="print">
                            <i class="bi bi-printer me-2"></i>Print
                        </button>
                        <a href="${ctx}/register" class="btn btn-outline-primary">
                            <i class="bi bi-person-plus me-2"></i>Register Another Student
                        </a>
                        <a href="${ctx}/" class="btn btn-primary">
                            <i class="bi bi-house me-2"></i>Back to Home
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/common/public-footer.jspf" %>
<%@ include file="/WEB-INF/views/common/scripts.jspf" %>
</body>
</html>
