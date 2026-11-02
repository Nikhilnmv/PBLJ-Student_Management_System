<%--
    VIEW: friendly error page (configured in web.xml for 403, 404, 405, 500
    and any uncaught exception). It deliberately shows NO technical details
    such as stack traces; those are written to the Tomcat log instead.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="statusCode" value="${requestScope['javax.servlet.error.status_code']}"/>
<c:choose>
    <c:when test="${statusCode == 404}">
        <c:set var="errorTitle" value="Page not found"/>
        <c:set var="errorMessage" value="The page you are looking for does not exist or may have been removed."/>
    </c:when>
    <c:when test="${statusCode == 403}">
        <c:set var="errorTitle" value="Access denied"/>
        <c:set var="errorMessage" value="This request was blocked for security reasons. Please reload the page and try again."/>
    </c:when>
    <c:when test="${statusCode == 405}">
        <c:set var="errorTitle" value="Action not allowed"/>
        <c:set var="errorMessage" value="This action cannot be performed this way. Please use the buttons in the application."/>
    </c:when>
    <c:otherwise>
        <c:set var="statusCode" value="500"/>
        <c:set var="errorTitle" value="Something went wrong"/>
        <c:set var="errorMessage" value="We could not complete your request. The database may be unavailable. Please try again in a moment."/>
    </c:otherwise>
</c:choose>
<c:set var="pageTitle" value="${errorTitle}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/public-navbar.jspf" %>
<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card card-elevated">
                <div class="card-body text-center p-5">
                    <div class="error-code mb-3"><c:out value="${statusCode}"/></div>
                    <h1 class="h4 mb-2"><c:out value="${errorTitle}"/></h1>
                    <p class="text-muted mb-4"><c:out value="${errorMessage}"/></p>
                    <div class="d-flex flex-column flex-sm-row gap-2 justify-content-center">
                        <a href="${ctx}/" class="btn btn-primary"><i class="bi bi-house me-2"></i>Go to Home</a>
                        <a href="${ctx}/admin/dashboard" class="btn btn-light-soft"><i class="bi bi-speedometer2 me-2"></i>Admin Dashboard</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
<%@ include file="/WEB-INF/views/common/scripts.jspf" %>
</body>
</html>
