<%--
    VIEW: "All Students" - search box, filters, results table and paging.
    Data comes from StudentListServlet (request attributes: students, filter,
    filterQuery, totalMatches, currentPage, totalPages, firstRow, lastRow ...).
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="All Students"/>
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

            <div class="page-header">
                <div>
                    <h1>All Students</h1>
                    <p>Search, filter and manage registered students.</p>
                </div>
                <div class="d-flex gap-2">
                    <a href="${ctx}/admin/students/export?${fn:escapeXml(filterQuery)}" class="btn btn-light-soft">
                        <i class="bi bi-download me-2"></i>Export CSV
                    </a>
                    <a href="${ctx}/admin/students/add" class="btn btn-primary">
                        <i class="bi bi-person-plus me-2"></i>Add Student
                    </a>
                </div>
            </div>

            <%-- ===== Search and filters (GET form: the choices appear in the URL) ===== --%>
            <div class="card filter-card mb-4">
                <div class="card-body">
                    <form action="${ctx}/admin/students" method="get" class="row g-2 g-lg-3 align-items-end">
                        <div class="col-12 col-lg-3">
                            <label for="keyword" class="form-label small">Search</label>
                            <div class="input-icon">
                                <i class="bi bi-search"></i>
                                <input type="search" class="form-control" id="keyword" name="keyword" maxlength="100"
                                       value="${fn:escapeXml(filter.keyword)}" placeholder="Name, roll no. or email">
                            </div>
                        </div>
                        <div class="col-12 col-sm-6 col-lg-3">
                            <label for="department" class="form-label small">Department</label>
                            <select class="form-select" id="department" name="department">
                                <option value="">All departments</option>
                                <c:forEach var="d" items="${departments}">
                                    <option value="${fn:escapeXml(d)}" ${filter.department == d ? 'selected' : ''}><c:out value="${d}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-6 col-sm-3 col-lg-2">
                            <label for="semester" class="form-label small">Semester</label>
                            <select class="form-select" id="semester" name="semester">
                                <option value="">All</option>
                                <c:forEach var="sem" items="${semesters}">
                                    <option value="${sem}" ${filter.semester == sem ? 'selected' : ''}>Semester ${sem}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-6 col-sm-3 col-lg-2">
                            <label for="status" class="form-label small">Status</label>
                            <select class="form-select" id="status" name="status">
                                <option value="">All</option>
                                <c:forEach var="st" items="${statuses}">
                                    <option value="${st}" ${filter.status == st ? 'selected' : ''}>${st}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-12 col-lg-2 d-flex gap-2">
                            <button type="submit" class="btn btn-primary flex-grow-1 text-nowrap"><i class="bi bi-funnel me-1"></i>Apply</button>
                            <c:if test="${filter.active}">
                                <a href="${ctx}/admin/students" class="btn btn-light-soft px-3" title="Clear all filters" aria-label="Clear all filters">
                                    <i class="bi bi-x-lg"></i>
                                </a>
                            </c:if>
                        </div>
                    </form>
                </div>
            </div>

            <%-- ===== Results ===== --%>
            <div class="card">
                <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                    <h2>
                        <c:choose>
                            <c:when test="${filter.active}">Search Results</c:when>
                            <c:otherwise>Registered Students</c:otherwise>
                        </c:choose>
                    </h2>
                    <span class="text-muted small">
                        Showing <strong class="text-dark">${firstRow}&ndash;${lastRow}</strong> of
                        <strong class="text-dark">${totalMatches}</strong> student${totalMatches == 1 ? '' : 's'}
                    </span>
                </div>

                <c:choose>
                    <c:when test="${empty students}">
                        <%-- Empty state: different message for "no data" and "no matches" --%>
                        <div class="empty-state">
                            <c:choose>
                                <c:when test="${filter.active}">
                                    <div class="empty-icon"><i class="bi bi-search"></i></div>
                                    <h3>No students match your search</h3>
                                    <p>Try a different name, roll number or email, or remove some filters.</p>
                                    <a href="${ctx}/admin/students" class="btn btn-light-soft"><i class="bi bi-x-circle me-2"></i>Clear filters</a>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-icon"><i class="bi bi-people"></i></div>
                                    <h3>No students registered yet</h3>
                                    <p>Share the registration form with students, or add a student yourself.</p>
                                    <div class="d-flex gap-2 justify-content-center">
                                        <a href="${ctx}/register" class="btn btn-light-soft" target="_blank" rel="noopener">Open Registration Form</a>
                                        <a href="${ctx}/admin/students/add" class="btn btn-primary"><i class="bi bi-person-plus me-2"></i>Add Student</a>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-modern">
                                <thead>
                                <tr>
                                    <th>Student</th>
                                    <th>Roll Number</th>
                                    <th class="d-none d-lg-table-cell">Department / Course</th>
                                    <th class="d-none d-md-table-cell text-center">Semester</th>
                                    <th>Status</th>
                                    <th class="d-none d-xl-table-cell">Registered</th>
                                    <th class="text-end">Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="s" items="${students}">
                                    <tr>
                                        <td>
                                            <div class="student-cell">
                                                <span class="avatar avatar-c${s.studentId % 6}"><c:out value="${s.initials}"/></span>
                                                <div class="text-truncate">
                                                    <a href="${ctx}/admin/students/view?id=${s.studentId}"><c:out value="${s.fullName}"/></a>
                                                    <small class="text-truncate"><c:out value="${s.email}"/></small>
                                                </div>
                                            </div>
                                        </td>
                                        <td class="mono text-nowrap-cell"><c:out value="${s.rollNumber}"/></td>
                                        <td class="d-none d-lg-table-cell">
                                            <div class="small fw-medium"><c:out value="${s.department}"/></div>
                                            <div class="small text-muted"><c:out value="${s.course}"/></div>
                                        </td>
                                        <td class="d-none d-md-table-cell text-center">${s.semester}</td>
                                        <td><span class="status-badge ${s.active ? 'status-active' : 'status-inactive'}"><c:out value="${s.status}"/></span></td>
                                        <td class="d-none d-xl-table-cell text-muted small text-nowrap-cell"><c:out value="${s.createdDateFormatted}"/></td>
                                        <td class="text-end text-nowrap-cell">
                                            <a href="${ctx}/admin/students/view?id=${s.studentId}" class="btn btn-light-soft btn-icon" title="View profile" aria-label="View profile of ${fn:escapeXml(s.fullName)}"><i class="bi bi-eye"></i></a>
                                            <a href="${ctx}/admin/students/edit?id=${s.studentId}" class="btn btn-light-soft btn-icon" title="Edit" aria-label="Edit ${fn:escapeXml(s.fullName)}"><i class="bi bi-pencil"></i></a>
                                            <form action="${ctx}/admin/students/delete" method="post" class="d-inline"
                                                  data-confirm="Delete ${fn:escapeXml(s.fullName)} (${fn:escapeXml(s.rollNumber)})? This permanently removes the record from the database."
                                                  data-confirm-title="Delete student?" data-confirm-button="Delete">
                                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                                <input type="hidden" name="id" value="${s.studentId}">
                                                <button type="submit" class="btn btn-soft-danger btn-icon" title="Delete" aria-label="Delete ${fn:escapeXml(s.fullName)}"><i class="bi bi-trash"></i></button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <%-- ===== Paging (keeps the current filters in every link) ===== --%>
                        <c:if test="${totalPages > 1}">
                            <c:set var="pageLinkBase" value="${ctx}/admin/students?${fn:escapeXml(filterQuery)}page="/>
                            <c:set var="windowStart" value="${currentPage - 2 < 1 ? 1 : currentPage - 2}"/>
                            <c:set var="windowEnd" value="${currentPage + 2 > totalPages ? totalPages : currentPage + 2}"/>
                            <nav class="card-footer bg-transparent d-flex justify-content-between align-items-center flex-wrap gap-2 py-3" aria-label="Student list pages">
                                <span class="small text-muted">Page ${currentPage} of ${totalPages}</span>
                                <ul class="pagination pagination-sm">
                                    <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageLinkBase}${currentPage - 1}" aria-label="Previous page"><i class="bi bi-chevron-left"></i></a>
                                    </li>
                                    <c:forEach var="p" begin="${windowStart}" end="${windowEnd}">
                                        <li class="page-item ${p == currentPage ? 'active' : ''}">
                                            <a class="page-link" href="${pageLinkBase}${p}" ${p == currentPage ? 'aria-current="page"' : ''}>${p}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageLinkBase}${currentPage + 1}" aria-label="Next page"><i class="bi bi-chevron-right"></i></a>
                                    </li>
                                </ul>
                            </nav>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
        <%@ include file="/WEB-INF/views/common/admin-footer.jspf" %>
    </div>
</div>
<%@ include file="/WEB-INF/views/common/confirm-modal.jspf" %>
<%@ include file="/WEB-INF/views/common/scripts.jspf" %>
</body>
</html>
