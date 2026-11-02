<%--
    VIEW: admin dashboard. All values are request attributes set by
    DashboardServlet from live MySQL queries.
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Dashboard"/>
<c:set var="activePage" value="dashboard"/>
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

            <div class="page-header">
                <div>
                    <h1>Welcome, <c:out value="${sessionScope.loggedInAdmin.username}"/></h1>
                    <p>Here is an overview of student registrations.</p>
                </div>
                <div class="d-flex gap-2">
                    <a href="${ctx}/admin/students/export" class="btn btn-light-soft"><i class="bi bi-download me-2"></i>Export CSV</a>
                    <a href="${ctx}/admin/students" class="btn btn-primary"><i class="bi bi-people me-2"></i>View All Students</a>
                </div>
            </div>

            <%-- ===== Statistic cards ===== --%>
            <div class="row g-3 g-xl-4 mb-4">
                <div class="col-6 col-xl-3">
                    <div class="card stat-card">
                        <div class="stat-top">
                            <span class="stat-label">Total Students</span>
                            <span class="stat-icon tone-primary"><i class="bi bi-people"></i></span>
                        </div>
                        <div class="stat-value">${totalStudents}</div>
                        <div class="stat-note">All registrations</div>
                    </div>
                </div>
                <div class="col-6 col-xl-3">
                    <div class="card stat-card">
                        <div class="stat-top">
                            <span class="stat-label">Active Students</span>
                            <span class="stat-icon tone-success"><i class="bi bi-person-check"></i></span>
                        </div>
                        <div class="stat-value">${activeStudents}</div>
                        <div class="stat-note">${activePercent}% of all students</div>
                    </div>
                </div>
                <div class="col-6 col-xl-3">
                    <div class="card stat-card">
                        <div class="stat-top">
                            <span class="stat-label">Inactive Students</span>
                            <span class="stat-icon tone-danger"><i class="bi bi-person-dash"></i></span>
                        </div>
                        <div class="stat-value">${inactiveStudents}</div>
                        <div class="stat-note">${inactivePercent}% of all students</div>
                    </div>
                </div>
                <div class="col-6 col-xl-3">
                    <div class="card stat-card">
                        <div class="stat-top">
                            <span class="stat-label">Departments</span>
                            <span class="stat-icon tone-violet"><i class="bi bi-building"></i></span>
                        </div>
                        <div class="stat-value">${totalDepartments}</div>
                        <div class="stat-note">With at least one student</div>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <%-- ===== Recent registrations ===== --%>
                <div class="col-xl-7">
                    <div class="card h-100">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <h2>Recent Registrations</h2>
                            <a href="${ctx}/admin/students" class="small fw-medium text-decoration-none">View all <i class="bi bi-arrow-right"></i></a>
                        </div>
                        <c:choose>
                            <c:when test="${empty recentStudents}">
                                <div class="empty-state">
                                    <div class="empty-icon"><i class="bi bi-inbox"></i></div>
                                    <h3>No registrations yet</h3>
                                    <p>New student registrations will appear here.</p>
                                    <a href="${ctx}/admin/students/add" class="btn btn-primary"><i class="bi bi-person-plus me-2"></i>Add Student</a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-modern">
                                        <thead>
                                        <tr>
                                            <th>Student</th>
                                            <th class="d-none d-md-table-cell">Department</th>
                                            <th class="d-none d-sm-table-cell">Registered</th>
                                            <th>Status</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="s" items="${recentStudents}">
                                            <tr>
                                                <td>
                                                    <div class="student-cell">
                                                        <span class="avatar avatar-c${s.studentId % 6}"><c:out value="${s.initials}"/></span>
                                                        <div>
                                                            <a href="${ctx}/admin/students/view?id=${s.studentId}"><c:out value="${s.fullName}"/></a>
                                                            <small class="mono"><c:out value="${s.rollNumber}"/></small>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td class="d-none d-md-table-cell small"><c:out value="${s.department}"/></td>
                                                <td class="d-none d-sm-table-cell text-muted text-nowrap-cell"><c:out value="${s.createdDateFormatted}"/></td>
                                                <td>
                                                    <span class="status-badge ${s.active ? 'status-active' : 'status-inactive'}"><c:out value="${s.status}"/></span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <%-- ===== Department-wise statistics ===== --%>
                <div class="col-xl-5">
                    <div class="card h-100">
                        <div class="card-header">
                            <h2>Students by Department</h2>
                        </div>
                        <div class="card-body">
                            <c:choose>
                                <c:when test="${empty departmentCounts}">
                                    <p class="text-muted mb-0">No data yet. Statistics appear after the first registration.</p>
                                </c:when>
                                <c:otherwise>
                                    <%-- departmentCounts is a Map: entry.key = department, entry.value = count --%>
                                    <c:forEach var="entry" items="${departmentCounts}">
                                        <c:set var="percent" value="${totalStudents == 0 ? 0 : entry.value * 100 / totalStudents}"/>
                                        <div class="dept-row">
                                            <div class="d-flex justify-content-between gap-2 mb-1">
                                                <c:url var="departmentLink" value="/admin/students">
                                                    <c:param name="department" value="${entry.key}"/>
                                                </c:url>
                                                <a href="${departmentLink}" class="dept-name text-decoration-none text-reset"><c:out value="${entry.key}"/></a>
                                                <span class="dept-count">${entry.value}</span>
                                            </div>
                                            <div class="progress" role="progressbar" aria-label="${fn:escapeXml(entry.key)}"
                                                 aria-valuenow="${entry.value}" aria-valuemin="0" aria-valuemax="${totalStudents}">
                                                <div class="progress-bar" style="width: ${percent}%"></div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
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
