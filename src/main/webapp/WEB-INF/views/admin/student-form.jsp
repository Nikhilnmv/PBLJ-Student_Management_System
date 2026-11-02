<%--
    VIEW: one form used for BOTH "Add Student" and "Edit Student".
    The servlet sets ${mode} to "add" (AddStudentServlet) or "edit"
    (EditStudentServlet); the title, form action and buttons change accordingly.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="isEdit" value="${mode == 'edit'}"/>
<c:set var="pageTitle" value="${isEdit ? 'Edit Student' : 'Add Student'}"/>
<c:set var="activePage" value="${isEdit ? 'students' : 'add'}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body>
<div class="admin-layout">
    <%@ include file="/WEB-INF/views/common/admin-sidebar.jspf" %>
    <div class="admin-main">
        <%@ include file="/WEB-INF/views/common/admin-topbar.jspf" %>
        <main class="page-content">
            <nav aria-label="breadcrumb" class="mb-3">
                <ol class="breadcrumb small mb-0">
                    <li class="breadcrumb-item"><a href="${ctx}/admin/dashboard">Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="${ctx}/admin/students">All Students</a></li>
                    <li class="breadcrumb-item active" aria-current="page">${pageTitle}</li>
                </ol>
            </nav>

            <div class="page-header">
                <div>
                    <h1>${pageTitle}</h1>
                    <p>
                        <c:choose>
                            <c:when test="${isEdit}">Update the details of <strong><c:out value="${student.registrationId}"/></strong>. Changes are saved directly to the database.</c:when>
                            <c:otherwise>Register a student on their behalf. A registration ID is generated automatically.</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </div>

            <div class="row">
                <div class="col-xl-10 col-xxl-9">
                    <div class="card">
                        <div class="card-body p-4">
                            <c:if test="${not empty errors}">
                                <div class="alert alert-danger mb-4" role="alert">
                                    <i class="bi bi-exclamation-circle-fill"></i>
                                    <div>
                                        <strong>Please correct the highlighted fields.</strong>
                                        <c:if test="${not empty errors.general}"><div><c:out value="${errors.general}"/></div></c:if>
                                    </div>
                                </div>
                            </c:if>

                            <form action="${ctx}/admin/students/${isEdit ? 'edit' : 'add'}" method="post" class="needs-validation" novalidate>
                                <%-- CSRF token: AuthFilter rejects the POST without it --%>
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <c:if test="${isEdit}">
                                    <input type="hidden" name="id" value="${student.studentId}">
                                </c:if>

                                <%@ include file="/WEB-INF/views/common/student-form-fields.jspf" %>

                                <%-- Cancel goes back to the profile (edit) or to the list (add) --%>
                                <c:set var="cancelUrl" value="${ctx}/admin/students"/>
                                <c:if test="${isEdit}">
                                    <c:set var="cancelUrl" value="${ctx}/admin/students/view?id=${student.studentId}"/>
                                </c:if>
                                <div class="d-flex flex-column flex-sm-row justify-content-end gap-2 pt-2">
                                    <a href="${cancelUrl}" class="btn btn-light-soft">Cancel</a>
                                    <button type="submit" class="btn btn-primary" data-loading-text="Saving...">
                                        <i class="bi bi-check2 me-2"></i>${isEdit ? 'Save Changes' : 'Add Student'}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <%@ include file="/WEB-INF/views/common/admin-footer.jspf" %>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/scripts.jspf" %>
</body>
</html>
