<%--
    VIEW: one student's complete profile (StudentProfileServlet, GET /admin/students/view?id=..).
    Also print-friendly: the browser's Print hides menus and buttons (see @media print in app.css).
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Student Profile"/>
<c:set var="activePage" value="students"/>
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
            <%@ include file="/WEB-INF/views/common/flash-messages.jspf" %>

            <div class="print-only mb-4">
                <h1 class="h3 mb-1">Student Profile</h1>
                <p class="text-muted mb-0">Student Registration System</p>
            </div>

            <nav aria-label="breadcrumb" class="no-print mb-3">
                <ol class="breadcrumb small mb-0">
                    <li class="breadcrumb-item"><a href="${ctx}/admin/dashboard">Dashboard</a></li>
                    <li class="breadcrumb-item"><a href="${ctx}/admin/students">All Students</a></li>
                    <li class="breadcrumb-item active" aria-current="page"><c:out value="${student.fullName}"/></li>
                </ol>
            </nav>

            <%-- ===== Header card: name, IDs, status and actions ===== --%>
            <div class="card mb-4">
                <div class="card-body p-4">
                    <div class="d-flex flex-column flex-lg-row justify-content-between gap-4">
                        <div class="profile-header">
                            <span class="avatar avatar-lg avatar-c${student.studentId % 6}"><c:out value="${student.initials}"/></span>
                            <div>
                                <h2><c:out value="${student.fullName}"/></h2>
                                <div class="profile-meta">
                                    <span class="chip"><i class="bi bi-hash"></i><c:out value="${student.registrationId}"/></span>
                                    <span class="chip mono"><i class="bi bi-person-badge"></i><c:out value="${student.rollNumber}"/></span>
                                    <span class="status-badge ${student.active ? 'status-active' : 'status-inactive'}"><c:out value="${student.status}"/></span>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex flex-wrap gap-2 align-items-start no-print">
                            <a href="${ctx}/admin/students/edit?id=${student.studentId}" class="btn btn-primary">
                                <i class="bi bi-pencil me-2"></i>Edit
                            </a>

                            <%-- Status toggle: a POST form that sends the OPPOSITE status --%>
                            <form action="${ctx}/admin/students/status" method="post" class="d-inline">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="id" value="${student.studentId}">
                                <c:choose>
                                    <c:when test="${student.active}">
                                        <input type="hidden" name="status" value="Inactive">
                                        <button type="submit" class="btn btn-soft-warning"><i class="bi bi-pause-circle me-2"></i>Mark Inactive</button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" name="status" value="Active">
                                        <button type="submit" class="btn btn-soft-success"><i class="bi bi-play-circle me-2"></i>Mark Active</button>
                                    </c:otherwise>
                                </c:choose>
                            </form>

                            <button type="button" class="btn btn-light-soft" data-action="print"><i class="bi bi-printer me-2"></i>Print</button>

                            <form action="${ctx}/admin/students/delete" method="post" class="d-inline"
                                  data-confirm="Delete ${fn:escapeXml(student.fullName)} (${fn:escapeXml(student.rollNumber)})? This permanently removes the record from the database."
                                  data-confirm-title="Delete student?" data-confirm-button="Delete">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="id" value="${student.studentId}">
                                <button type="submit" class="btn btn-soft-danger"><i class="bi bi-trash me-2"></i>Delete</button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-header"><h3><i class="bi bi-person text-primary me-2"></i>Personal Information</h3></div>
                        <div class="card-body">
                            <div class="info-grid">
                                <div class="info-item"><div class="info-label">Full Name</div><div class="info-value"><c:out value="${student.fullName}"/></div></div>
                                <div class="info-item"><div class="info-label">Gender</div><div class="info-value"><c:out value="${student.gender}"/></div></div>
                                <div class="info-item"><div class="info-label">Date of Birth</div><div class="info-value"><c:out value="${student.dobFormatted}"/></div></div>
                                <div class="info-item"><div class="info-label">Age</div><div class="info-value">${student.age} years</div></div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-header"><h3><i class="bi bi-mortarboard text-primary me-2"></i>Academic Information</h3></div>
                        <div class="card-body">
                            <div class="info-grid">
                                <div class="info-item"><div class="info-label">Roll Number</div><div class="info-value mono"><c:out value="${student.rollNumber}"/></div></div>
                                <div class="info-item"><div class="info-label">Department</div><div class="info-value"><c:out value="${student.department}"/></div></div>
                                <div class="info-item"><div class="info-label">Course</div><div class="info-value"><c:out value="${student.course}"/></div></div>
                                <div class="info-item"><div class="info-label">Current Semester</div><div class="info-value">Semester ${student.semester}</div></div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-header"><h3><i class="bi bi-envelope text-primary me-2"></i>Contact Information</h3></div>
                        <div class="card-body">
                            <div class="info-grid">
                                <div class="info-item"><div class="info-label">Email</div><div class="info-value"><a href="mailto:${fn:escapeXml(student.email)}"><c:out value="${student.email}"/></a></div></div>
                                <div class="info-item"><div class="info-label">Mobile Number</div><div class="info-value"><a href="tel:+91${fn:escapeXml(student.phone)}">+91 <c:out value="${student.phone}"/></a></div></div>
                            </div>
                            <div class="info-item mt-3"><div class="info-label">Address</div><div class="info-value"><c:out value="${student.address}"/></div></div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card h-100">
                        <div class="card-header"><h3><i class="bi bi-clock-history text-primary me-2"></i>Registration Record</h3></div>
                        <div class="card-body">
                            <div class="info-grid">
                                <div class="info-item"><div class="info-label">Registration ID</div><div class="info-value"><c:out value="${student.registrationId}"/></div></div>
                                <div class="info-item"><div class="info-label">Database ID (student_id)</div><div class="info-value">${student.studentId}</div></div>
                                <div class="info-item"><div class="info-label">Registered On</div><div class="info-value"><c:out value="${student.createdAtFormatted}"/></div></div>
                                <div class="info-item"><div class="info-label">Status</div><div class="info-value"><c:out value="${student.status}"/></div></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
        <%@ include file="/WEB-INF/views/common/admin-footer.jspf" %>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/confirm-modal.jspf" %>
<%@ include file="/WEB-INF/views/common/scripts.jspf" %>
</body>
</html>
