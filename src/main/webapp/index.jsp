<%--
    Home (landing) page: http://localhost:8080/student-registration/
    A purely presentational page with links to the registration form and
    the admin login. It contains no Java code and no database access.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Home"/>
<c:set var="activeNav" value="home"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/public-navbar.jspf" %>

<header class="hero hero-home">
    <div class="container">
        <div class="row align-items-center g-5">
            <div class="col-lg-7">
                <span class="hero-eyebrow"><i class="bi bi-stars"></i> Online student registration</span>
                <h1 class="mt-4 mb-3">Register once. Manage every student record with ease.</h1>
                <p class="lead mb-4">
                    Students submit their personal and academic details online. Administrators securely
                    review, search, update and track every registration from a single dashboard.
                </p>
                <div class="d-flex flex-wrap gap-3">
                    <a href="${ctx}/register" class="btn btn-lg btn-hero-light">
                        <i class="bi bi-pencil-square me-2"></i>Register as a Student
                    </a>
                    <a href="${ctx}/login" class="btn btn-lg btn-hero-outline">
                        <i class="bi bi-shield-lock me-2"></i>Administrator Login
                    </a>
                </div>
            </div>
            <div class="col-lg-5">
                <div class="hero-panel">
                    <p class="small-caps mb-3" style="color: rgba(255,255,255,.6);">How it works</p>
                    <div class="step">
                        <span class="step-number">1</span>
                        <div><strong>Fill in the form</strong><p>Personal, academic and contact details.</p></div>
                    </div>
                    <div class="step">
                        <span class="step-number">2</span>
                        <div><strong>Get your registration ID</strong><p>A unique ID is generated instantly.</p></div>
                    </div>
                    <div class="step">
                        <span class="step-number">3</span>
                        <div><strong>Admin verifies the record</strong><p>Records are managed securely by the college.</p></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</header>

<main class="container overlap-section pb-4">
    <div class="row g-4">
        <div class="col-sm-6 col-lg-3">
            <div class="card card-elevated feature-card">
                <span class="feature-icon tone-primary"><i class="bi bi-ui-checks"></i></span>
                <h3>Validated Registration</h3>
                <p>Every field is checked in the browser and again on the server before it is saved.</p>
            </div>
        </div>
        <div class="col-sm-6 col-lg-3">
            <div class="card card-elevated feature-card">
                <span class="feature-icon tone-success"><i class="bi bi-shield-check"></i></span>
                <h3>Secure Admin Access</h3>
                <p>BCrypt-hashed passwords, session-based login and protected admin pages.</p>
            </div>
        </div>
        <div class="col-sm-6 col-lg-3">
            <div class="card card-elevated feature-card">
                <span class="feature-icon tone-violet"><i class="bi bi-search"></i></span>
                <h3>Search &amp; Filter</h3>
                <p>Find students by name, roll number or email and filter by department or semester.</p>
            </div>
        </div>
        <div class="col-sm-6 col-lg-3">
            <div class="card card-elevated feature-card">
                <span class="feature-icon tone-warning"><i class="bi bi-bar-chart-line"></i></span>
                <h3>Live Dashboard</h3>
                <p>Registration statistics calculated directly from the MySQL database.</p>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/common/public-footer.jspf" %>
<%@ include file="/WEB-INF/views/common/scripts.jspf" %>
</body>
</html>
