# Project Report Material

Raw material for the final project report. **This is not the final report.** When the college's
prescribed format is available, the final report will be written in exactly that format, using the
content collected here. Fill in the bracketed placeholders (`[ … ]`) with your own details.

---

## 1. Title page information
- **Project title:** Student Registration System
- **Subject:** Project Based Learning Using Java (PBLJ) — Minor Project
- **Domain:** Education
- **Submitted by:** [Your name, roll number, class/section]
- **Guide:** [Guide's name, designation]
- **Department / Institute:** [Department, college name]
- **Academic year:** [2026–27]

## 2. Abstract (draft, ~200 words)
> Student registration in many colleges is still carried out on paper forms or shared spreadsheets,
> which leads to incomplete and inconsistent data, duplicate entries and time-consuming searches and
> reports. This project presents a web-based Student Registration System developed with Java Servlets,
> JavaServer Pages, JDBC and MySQL, deployed on Apache Tomcat 9. Students register through a public,
> validated online form and instantly receive a unique registration ID. Administrators authenticate
> with BCrypt-hashed passwords and use a protected dashboard that shows live statistics computed by SQL
> queries, and a management module to search, filter, view, add, edit, delete, activate/deactivate and
> export student records. The system follows the Model–View–Controller architecture with a Data Access
> Object layer, separating presentation, control logic and database access. Data integrity is enforced
> both by server-side validation and by database constraints (primary key, unique, check). Security
> measures include parameterised SQL queries against injection, output escaping against cross-site
> scripting, session-based access control with a servlet filter, CSRF tokens and POST-only data changes.
> The system was verified with 28 unit tests, 15 database integration tests and 67 automated HTTP
> functional checks, all passing, and responded in under 50 ms per page with 10,000 student records.
> The result is a simple, secure and maintainable application suitable for a college office.

## 3. Problem statement
Manual registration using paper or spreadsheets is error-prone, allows duplicate registrations, offers
no access control, and makes searching, correcting and reporting student data slow. A centralised,
validated and secure electronic registration system is required.

## 4. Objectives
1. Develop an online student registration form with complete validation.
2. Store registrations in a relational database with integrity constraints.
3. Generate a unique registration ID automatically.
4. Provide secure administrator authentication and session management.
5. Provide student management: list, search, filter, view, add, edit, delete, status change, export.
6. Provide a dashboard with real-time statistics.
7. Apply the MVC and DAO design patterns and essential web security practices.
8. Test and document the system.

## 5. Scope
In scope: public registration; single admin role; dashboard; CRUD with search/filter/paging; CSV
export; print; local deployment. Out of scope: student login, payments, notifications, document
upload, multiple roles, internet deployment. (Details: [PROJECT_OVERVIEW.md §5](PROJECT_OVERVIEW.md#5-scope).)

## 6. Existing system
- Paper forms filled by hand and typed into spreadsheets later.
- Spreadsheets e-mailed between staff; several versions exist.
- No validation: wrong emails/phones, missing fields.
- Duplicates detected only by manual checking.
- Counting students per department or semester requires manual filtering.
- No access control or audit; anyone with the file can change it.

## 7. Proposed system
- Online form with browser and server validation.
- Central MySQL database; constraints prevent invalid and duplicate data.
- Instant registration ID and printable acknowledgement.
- Secure admin panel with search, filters, editing, status, deletion with confirmation, CSV export.
- Live dashboard statistics.
- Responsive interface for phones and computers.

## 8. Feasibility study
| Type | Assessment |
|---|---|
| Technical | Uses mature, free technologies (Java 17, Tomcat 9, MySQL 8) that run on an ordinary laptop. The team has Java and SQL knowledge. Feasible. |
| Economic | All software is free and open source; no hosting cost for local use. Development effort only. Feasible. |
| Operational | Browser interface familiar to students and staff; minimal training; reduces manual work. Feasible. |
| Schedule | Built incrementally in milestones (see DEVELOPMENT_PROCESS.md). Feasible within the semester. |
| Legal/ethical | Only fictional sample data; passwords hashed; no personal data in the repository. |

## 9. Functional requirements
FR-01 – FR-30 in [SRS.md §3](SRS.md#3-functional-requirements). Summary: registration form and
validation (FR-01–05), duplicate prevention (FR-06–07), storage and ID (FR-08–10), authentication
(FR-11–16), dashboard (FR-17–19), management (FR-20–30).

## 10. Non-functional requirements
NFR-01 – NFR-12 in [SRS.md §4](SRS.md#4-non-functional-requirements): security (SQL injection, BCrypt,
CSRF, XSS, secrets, cookies/headers), reliability, data integrity, usability, performance,
maintainability, portability.

## 11. Hardware and software requirements
[SRS.md §5–6](SRS.md#5-hardware-requirements): dual-core CPU, 4 GB RAM (8 GB recommended), 2 GB disk;
Windows/macOS/Linux, JDK 17, Maven 3.8+, MySQL 8.0, Tomcat 9, modern browser.

## 12. Technology stack
| Layer | Technology | Purpose |
|---|---|---|
| Language | Java 17 | Server-side logic |
| Web | Servlets 4.0, JSP 2.3, JSTL 1.2 | Controllers and views |
| UI | HTML5, CSS3, JavaScript, Bootstrap 5.3 | Responsive interface |
| Database | MySQL 8.0 | Persistent storage |
| Connectivity | JDBC, MySQL Connector/J 8.4 | Java–MySQL communication |
| Security | jBCrypt | Password hashing |
| Server | Apache Tomcat 9 | Servlet container |
| Build/Test | Maven, JUnit 5 | Build, dependencies, tests |
| Version control | Git, GitHub | Source code management |

## 13. System architecture (for the report)
The system is a three-tier web application: the browser (presentation tier), Apache Tomcat running the
Java application (application tier) and MySQL (data tier). Inside the application tier the MVC pattern
separates **models** (`Student`, `Admin`, `StudentFilter`), **views** (JSP pages) and **controllers**
(twelve servlets). A **DAO layer** (`StudentDAO`, `AdminDAO`) encapsulates all SQL. A **servlet filter**
(`AuthFilter`) protects administrative URLs, and utility classes provide database connections,
validation, password hashing, CSRF tokens and CSV formatting.
Figures: `diagrams/exports/architecture.png`, `mvc-architecture.png`, `component-diagram.png`,
`deployment-diagram.png`, `request-lifecycle.png`. Text: [ARCHITECTURE.md](ARCHITECTURE.md).

## 14. Database design (for the report)
Database `student_registration_db` (utf8mb4) with two InnoDB tables. `students` (13 columns) uses an
auto-increment primary key, UNIQUE constraints on roll number and email, CHECK constraints on semester
(1–8), status and gender, defaults for status and registration time, and indexes on department,
semester, status and registration time. `admin` stores usernames (UNIQUE) and BCrypt password hashes.
The schema is in third normal form; derived values (age, display ID) are computed rather than stored.
Figure: `er-diagram.png`. Tables: data dictionary in [DATABASE_DESIGN.md](DATABASE_DESIGN.md).

## 15. UML diagram descriptions
| Figure | File | Description for the report |
|---|---|---|
| Use case diagram | `use-case-diagram.png` | Two actors. Student: register, view registration ID. Administrator: log in/out, dashboard, view/search/filter, profile, add, edit, delete, change status, export, print. Register/Add/Edit include "Validate student data". |
| Class diagram | `class-diagram.png` | Model classes, DAOs, utilities, `AuthFilter` (implements `Filter`), servlets (extend `HttpServlet`), `DuplicateStudentException` (extends `Exception`) and their dependencies. |
| Sequence – registration | `registration-sequence.png` | Interaction from the GET of the form through validation, duplicate checks, INSERT, redirect and success page, including the error and race-condition alternatives. |
| Sequence – login | `login-sequence.png` | Credential lookup, BCrypt verification, session creation with CSRF token, filter check, logout and invalidation. |
| Sequence – management | `student-management-sequence.png` | Search with filters, edit with validation and UPDATE, delete with confirmation and DELETE. |
| Activity – registration | `activity-registration.png` | Decision points: browser validation, server validation, duplicate check, constraint check. |
| Activity – admin | `activity-admin-management.png` | Login loop, dashboard, search, profile actions, add, export, logout. |
| Component diagram | `component-diagram.png` | Web components, data/utility components, external driver, configuration and database. |
| Deployment diagram | `deployment-diagram.png` | Browser, JVM with Tomcat and WAR, MySQL server on one laptop; ports 8080 and 3306. |

## 16. DFD descriptions
- **Level 0 (context):** the whole system as one process exchanging registration details and IDs with
  the Student, and credentials, criteria, updates, statistics, lists and CSV files with the Administrator.
- **Level 1:** six processes — 1.0 Register Student, 2.0 Authenticate Admin, 3.0 Search and View
  Students, 4.0 Manage Student Records, 5.0 Generate Statistics, 6.0 Export CSV — and three data stores
  (D1 students, D2 admin, D3 HTTP session).
Figures: `dfd-level-0.png`, `dfd-level-1.png`.

## 17. Implementation — module descriptions
| Module | Classes / files | Description |
|---|---|---|
| Registration | `RegisterServlet`, `RegistrationSuccessServlet`, `register.jsp`, `registration-success.jsp` | Form display, normalisation, validation, duplicate checks, insertion, PRG redirect, ID display |
| Validation | `ValidationUtil`, `AppConstants`, `StudentRequestHelper`, `app.js` | Field rules (regex, ranges, lists) on server; HTML5 checks in browser |
| Database access | `DBConnection`, `StudentDAO`, `AdminDAO` | Configuration, connections, PreparedStatement CRUD, search, statistics |
| Authentication | `LoginServlet`, `LogoutServlet`, `AuthFilter`, `PasswordUtil`, `CsrfUtil`, `AdminSetup` | BCrypt login, session lifecycle, route protection, CSRF tokens, first-admin setup |
| Dashboard | `DashboardServlet`, `dashboard.jsp` | Statistics cards, recent registrations, per-department bars |
| Student management | `StudentListServlet`, `StudentProfileServlet`, `AddStudentServlet`, `EditStudentServlet`, `DeleteStudentServlet`, `StudentStatusServlet` + JSPs | List/search/filter/paging, profile, add, edit, delete, status |
| Export & print | `ExportStudentsServlet`, `CsvUtil`, print CSS | CSV download and print layout |
| Infrastructure | `AppContextListener`, `web.xml`, `context.xml` | Start-up DB check, error pages, session and cookie settings, security headers |

## 18. Testing methodology
Unit testing (JUnit 5) for validation, hashing, CSV and model logic; integration testing of DAO classes
against MySQL; automated black-box functional testing over HTTP with a curl script; manual and browser
testing for UI behaviour, responsiveness, printing, database outage and performance. Test cases have IDs,
preconditions, inputs, expected and actual results ([TEST_CASES.md](TEST_CASES.md)), traced to
requirements ([TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md)).

## 19. Actual testing results
From [TEST_RESULTS.md](TEST_RESULTS.md) (executed 02 Nov 2026 on Ubuntu 24.04, Java 17, Tomcat 9.0.111,
MySQL 8.0.46):
- Unit tests: 28/28 passed. Integration tests: 15/15 passed. HTTP functional checks: 67/67 passed.
- Documented test cases: 80 — 73 executed and passed, 0 failed, 7 pending (own-laptop setup, IDE run,
  other browsers, Workbench view, 30-minute timeout, empty-database dashboard).
- Performance with 10,017 students: dashboard 38 ms, list 18 ms, search ≈ 41 ms, filters 17 ms; CSV
  export of all rows 0.35 s.
- Ten defects found and fixed during testing and final verification.
> Add your own laptop's results for the pending cases before including them in the report.

## 20. Advantages
Accurate data (validation + constraints) · no duplicates · instant ID · fast search and reports · secure
admin access · responsive and offline-capable UI · free software · maintainable MVC + DAO code · tested.

## 21. Limitations
Single admin role · no student self-service · no email verification · no login lockout · no connection
pool · fixed department/course lists · hard delete without audit log · local HTTP only.

## 22. Future scope
Student portal · email/SMS notifications and verification · document upload · role-based access ·
lookup tables with foreign keys · audit log and soft delete · rate limiting · connection pooling ·
charts/PDF reports · cloud deployment with HTTPS.

## 23. Conclusion (draft)
> The Student Registration System meets its objectives: students can register online with validated
> data and receive a unique ID, and administrators can securely manage, analyse and export the records.
> Building it with Servlets, JSP and JDBC provided practical experience of the HTTP request–response
> cycle, session management, MVC and DAO design, SQL and database constraints, and web security
> practices such as parameterised queries, password hashing, CSRF protection and output escaping.
> Systematic testing at unit, integration and functional levels confirmed correct behaviour and good
> performance. The modular design allows the future enhancements listed above to be added with
> limited changes.

## 24. References and learning resources
1. Oracle, *The Java Tutorials* — JDBC Database Access. <https://docs.oracle.com/javase/tutorial/jdbc/>
2. Oracle, *Java SE 17 API Documentation*. <https://docs.oracle.com/en/java/javase/17/docs/api/>
3. Jakarta EE / JCP, *Java Servlet Specification 4.0* (JSR 369).
4. Apache Software Foundation, *Apache Tomcat 9 Documentation*. <https://tomcat.apache.org/tomcat-9.0-doc/>
5. Oracle, *MySQL 8.0 Reference Manual*. <https://dev.mysql.com/doc/refman/8.0/en/>
6. Oracle, *MySQL Connector/J 8 Developer Guide*. <https://dev.mysql.com/doc/connector-j/en/>
7. Apache Software Foundation, *Apache Maven Documentation*. <https://maven.apache.org/guides/>
8. Bootstrap Team, *Bootstrap 5.3 Documentation*. <https://getbootstrap.com/docs/5.3/>
9. OWASP Foundation, *SQL Injection Prevention Cheat Sheet*; *Cross-Site Request Forgery Prevention Cheat Sheet*; *Password Storage Cheat Sheet*. <https://cheatsheetseries.owasp.org/>
10. JUnit Team, *JUnit 5 User Guide*. <https://junit.org/junit5/docs/current/user-guide/>
11. Provos, N. and Mazières, D., "A Future-Adaptable Password Scheme" (bcrypt), USENIX 1999.
12. Fowler, M., *Patterns of Enterprise Application Architecture* (Data Access/Table Data Gateway, MVC), Addison-Wesley, 2002.

## 25. Figures and tables available for the report
- Diagrams (PNG and SVG): `docs/diagrams/exports/` — 15 figures.
- Tables: data dictionary (DATABASE_DESIGN.md), requirements (SRS.md), test cases (TEST_CASES.md),
  traceability (TRACEABILITY_MATRIX.md), technology stack (above).
- Screenshots: capture from your own running application using
  [REPORT_SCREENSHOTS_CHECKLIST.md](REPORT_SCREENSHOTS_CHECKLIST.md).
