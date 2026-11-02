<%--
    VIEW: public student registration form.
    Shown by RegisterServlet (GET /register), and shown again with error
    messages when the submitted data is invalid (POST /register).

    This file is inside WEB-INF, so a browser cannot open it directly;
    it is only displayed when a servlet forwards to it.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Student Registration"/>
<c:set var="activeNav" value="register"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/public-navbar.jspf" %>

<header class="hero hero-page">
    <div class="container">
        <span class="hero-eyebrow"><i class="bi bi-pencil-square"></i> New student registration</span>
        <h1 class="mt-3 mb-2">Student Registration Form</h1>
        <p class="lead mb-0">Fill in your details below. Fields marked <strong class="text-white">*</strong> are required.</p>
    </div>
</header>

<main class="container overlap-section pb-5">
    <div class="row g-4">
        <div class="col-lg-8">
            <div class="card form-card">
                <div class="card-body">
                    <c:if test="${not empty errors}">
                        <div class="alert alert-danger mb-4" role="alert">
                            <i class="bi bi-exclamation-circle-fill"></i>
                            <div>
                                <strong>Please correct the highlighted fields.</strong>
                                <c:if test="${not empty errors.general}">
                                    <div><c:out value="${errors.general}"/></div>
                                </c:if>
                            </div>
                        </div>
                    </c:if>

                    <%-- method="post": the data goes in the request body, not in the URL --%>
                    <form action="${ctx}/register" method="post" class="needs-validation" novalidate>
                        <%@ include file="/WEB-INF/views/common/student-form-fields.jspf" %>

                        <div class="d-flex flex-column flex-sm-row gap-2 justify-content-between align-items-sm-center pt-2">
                            <p class="text-muted small mb-0">
                                <i class="bi bi-lock me-1"></i>Your details are stored securely and visible only to administrators.
                            </p>
                            <button type="submit" class="btn btn-primary btn-lg" data-loading-text="Submitting...">
                                <i class="bi bi-send me-2"></i>Submit Registration
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="card info-card mb-4">
                <div class="card-body">
                    <h2 class="h6 mb-3"><i class="bi bi-info-circle text-primary me-2"></i>Before you begin</h2>
                    <ul class="info-list">
                        <li><i class="bi bi-check2-circle"></i><span>Keep your college roll number ready.</span></li>
                        <li><i class="bi bi-check2-circle"></i><span>Use an email address you check regularly.</span></li>
                        <li><i class="bi bi-check2-circle"></i><span>Mobile number must be 10 digits.</span></li>
                        <li><i class="bi bi-check2-circle"></i><span>Each roll number and email can be registered only once.</span></li>
                    </ul>
                </div>
            </div>
            <div class="card info-card">
                <div class="card-body">
                    <h2 class="h6 mb-3"><i class="bi bi-signpost-split text-primary me-2"></i>What happens next?</h2>
                    <ul class="info-list">
                        <li><i class="bi bi-1-circle"></i><span>You receive a unique registration ID instantly.</span></li>
                        <li><i class="bi bi-2-circle"></i><span>Your record appears on the administrator dashboard.</span></li>
                        <li><i class="bi bi-3-circle"></i><span>Contact the administrator to correct any details later.</span></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/common/public-footer.jspf" %>
<%@ include file="/WEB-INF/views/common/scripts.jspf" %>
</body>
</html>
