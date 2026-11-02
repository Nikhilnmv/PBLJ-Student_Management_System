# Requirements Traceability Matrix (RTM)

Links every requirement in [SRS.md](SRS.md) to the code that implements it and the tests that verify
it ([TEST_CASES.md](TEST_CASES.md)). Status reflects executed tests only.

## Functional requirements

| Req. | Requirement (short) | Implemented in | Verified by | Status |
|---|---|---|---|---|
| FR-01 | Public registration form with 10 fields | `RegisterServlet.doGet`, `register.jsp`, `student-form-fields.jspf` | TC-REG-01, TC-REG-13 | Verified |
| FR-02 | Mandatory field validation | `ValidationUtil.validateStudent`, `app.js` | TC-REG-02, TC-REG-13, TC-REG-15, TC-UNIT-01 | Verified |
| FR-03 | Email format validation | `ValidationUtil.isValidEmail` | TC-REG-03, TC-UNIT-01 | Verified |
| FR-04 | Phone validation | `ValidationUtil.isValidPhone` | TC-REG-04, TC-UNIT-01 | Verified |
| FR-05 | Semester, age, lists, name, roll, address rules | `ValidationUtil`, `AppConstants`, CHECK constraints | TC-REG-07, TC-REG-08, TC-REG-12, TC-UNIT-01, TC-INT-01 | Verified |
| FR-06 | No duplicate roll numbers | `StudentDAO.isRollNumberTaken`, `uk_students_roll_number`, `DuplicateStudentException` | TC-REG-05, TC-MGT-13, TC-SEC-08, TC-INT-01 | Verified |
| FR-07 | No duplicate emails | `StudentDAO.isEmailTaken`, `uk_students_email` | TC-REG-06, TC-MGT-13, TC-INT-01 | Verified |
| FR-08 | Store as Active with timestamp | `RegisterServlet.doPost` (`setStatus`), `StudentDAO.addStudent`, `created_at DEFAULT` | TC-REG-01, TC-REG-09, TC-REG-11 | Verified |
| FR-09 | Generate and show registration ID | `AUTO_INCREMENT`, `Student.getRegistrationId`, `RegistrationSuccessServlet`, `registration-success.jsp` | TC-REG-01, TC-UNIT-04 | Verified |
| FR-10 | No duplicate on refresh (PRG) | `RegisterServlet` redirect, `RegistrationSuccessServlet` removes the session attribute | TC-REG-10 | Verified |
| FR-11 | Admin login with BCrypt | `LoginServlet`, `AdminDAO.findByUsername`, `PasswordUtil.checkPassword` | TC-AUTH-01, TC-AUTH-02, TC-SEC-13, TC-UNIT-02 | Verified |
| FR-12 | Generic login error | `LoginServlet.doPost` | TC-AUTH-02, TC-AUTH-03 | Verified |
| FR-13 | Protect `/admin/*` | `AuthFilter` | TC-AUTH-05, TC-SEC-04 | Verified |
| FR-14 | New session at login, invalidate at logout | `LoginServlet`, `LogoutServlet` | TC-AUTH-06, TC-AUTH-08, TC-SEC-05 | Verified |
| FR-15 | 30-minute timeout | `web.xml` `<session-timeout>`, `setMaxInactiveInterval` | TC-AUTH-11 | **Pending** (configuration verified) |
| FR-16 | First admin via setup page / tool, no plain-text password | `SetupServlet`, `setup.jsp`, `AdminSetup`, `AdminDAO.countAdmins/createAdmin/updatePassword` | TC-AUTH-12 – TC-AUTH-16, TC-SEC-12 | Verified |
| FR-17 | Dashboard counts from SQL | `DashboardServlet`, `StudentDAO.countAllStudents/countStudentsByStatus/countDepartments` | TC-DASH-01, TC-DASH-02, TC-INT-01 | Verified |
| FR-18 | Five recent registrations | `StudentDAO.getRecentStudents`, `dashboard.jsp` | TC-DASH-04 | Verified |
| FR-19 | Students per department | `StudentDAO.getDepartmentCounts` | TC-DASH-03, TC-INT-01 | Verified |
| FR-20 | List with paging | `StudentListServlet`, `StudentDAO.searchStudents(filter, offset, limit)`, `countStudents` | TC-MGT-01, TC-MGT-19, TC-INT-01 | Verified |
| FR-21 | Search name/roll/email | `StudentDAO.buildWhereClause` (`LIKE ?`) | TC-MGT-02, TC-MGT-03, TC-MGT-04, TC-INT-01 | Verified |
| FR-22 | Filters department/semester/status | `StudentRequestHelper.readFilterFromRequest`, `buildWhereClause` | TC-MGT-05 – TC-MGT-08 | Verified |
| FR-23 | Student profile | `StudentProfileServlet`, `student-profile.jsp` | TC-MGT-09, TC-MGT-10 | Verified |
| FR-24 | Add student | `AddStudentServlet`, `student-form.jsp` | TC-MGT-11 | Verified |
| FR-25 | Edit student | `EditStudentServlet`, `StudentDAO.updateStudent` | TC-MGT-12, TC-MGT-13, TC-INT-01 | Verified |
| FR-26 | Delete with confirmation, POST only | `DeleteStudentServlet`, `confirm-modal.jspf`, `app.js` | TC-MGT-15, TC-MGT-16, TC-MGT-20 | Verified |
| FR-27 | Change status | `StudentStatusServlet`, `StudentDAO.updateStatus` | TC-MGT-14, TC-INT-01 | Verified |
| FR-28 | CSV export | `ExportStudentsServlet`, `CsvUtil` | TC-MGT-17, TC-UNIT-03 | Verified |
| FR-29 | Print-friendly profile / slip | `@media print` in `app.css`, `data-action="print"` | TC-MGT-21 | Verified (paper print pending) |
| FR-30 | Notifications and empty states | `flash-messages.jspf`, `StudentRequestHelper.setFlash…`, empty states in JSPs | TC-MGT-18, TC-MGT-22, TC-MGT-15 | Verified |

## Non-functional requirements

| Req. | Requirement (short) | Implemented in | Verified by | Status |
|---|---|---|---|---|
| NFR-01 | PreparedStatement for all SQL | `StudentDAO`, `AdminDAO` | TC-SEC-01, TC-SEC-02, TC-SEC-03, TC-INT-01 | Verified |
| NFR-02 | BCrypt password storage | `PasswordUtil`, `admin.password_hash` | TC-SEC-13, TC-UNIT-02 | Verified |
| NFR-03 | CSRF token + POST for changes | `CsrfUtil`, `AuthFilter`, forms with hidden token | TC-SEC-06, TC-MGT-16 | Verified |
| NFR-04 | Output escaping (XSS) | `<c:out>`, `fn:escapeXml` in JSPs | TC-SEC-07 | Verified |
| NFR-05 | No secrets in Git | `.gitignore`, `db.properties.example`, `AdminSetup` | TC-SEC-12 | Verified |
| NFR-06 | Cookie flags, headers, no cache | `web.xml`, `context.xml`, `AuthFilter` | TC-AUTH-07, TC-AUTH-09, TC-SEC-09 | Verified |
| NFR-07 | Friendly errors, recovery | `error.jsp`, `web.xml` error pages, `AppContextListener` | TC-SEC-10, TC-SEC-11, TC-MGT-10 | Verified |
| NFR-08 | Database constraints | `schema.sql` | TC-INT-01 (UNIQUE, CHECK), TC-REG-05/06 | Verified |
| NFR-09 | Responsive, consistent UI | `app.css`, Bootstrap | TC-UI-01 | Verified (other browsers pending: TC-UI-03) |
| NFR-10 | < 1 s with 10,000 students | indexes, paging | TC-PERF-01 | Verified |
| NFR-11 | MVC + DAO, tests | package structure, `src/test` | Code review, TC-UNIT, TC-INT | Verified |
| NFR-12 | Portable, offline | Java 17/Tomcat 9/MySQL 8, local assets | TC-UI-02, TC-ENV-01 – 04 | Offline verified; Windows/macOS **pending** |

## Use case → requirement map

| Use case | Requirements |
|---|---|
| UC-01 Register | FR-01 – FR-08, FR-10 |
| UC-02 View registration ID | FR-09, FR-29 |
| UC-03 Log in | FR-11, FR-12, FR-14 |
| UC-04 Log out | FR-14 |
| UC-05 Dashboard | FR-17 – FR-19 |
| UC-06 List/search/filter | FR-20 – FR-22, FR-30 |
| UC-07 Profile | FR-23 |
| UC-08 Add | FR-24 |
| UC-09 Edit | FR-25 |
| UC-10 Delete | FR-26 |
| UC-11 Status | FR-27 |
| UC-12 Export | FR-28 |
| UC-13 Print | FR-29 |
