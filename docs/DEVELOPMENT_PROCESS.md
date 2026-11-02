# Development Process

How the project was developed, step by step — useful for the "Methodology" and "Implementation"
chapters of the project report. The project followed an **incremental (iterative) model**: the
system was built in small working increments, each one tested and committed to GitHub before the
next began.

```
Problem → Requirements → Analysis → Design → Implement increment → Test → Commit → (next increment) → Documentation → Final verification
```

---

## 1. Problem identification
Paper and spreadsheet registration leads to handwriting errors, missing fields, duplicate entries,
slow searching and manual counting (see [PROJECT_OVERVIEW.md §2](PROJECT_OVERVIEW.md#2-the-problem-being-solved)).
Goal: an online registration form plus a secure admin panel backed by a relational database.

## 2. Requirement collection
Requirements came from:
- the project brief (subject: Project Based Learning Using Java),
- the typical workflow of a college office (register → verify → correct → report),
- the technology constraints of the course (Servlets, JSP, JDBC, MySQL, Tomcat 9, Maven).

## 3. Requirement analysis
Requirements were grouped into user classes (Student, Administrator) and numbered as functional
(FR-01 – FR-30) and non-functional (NFR-01 – NFR-12) requirements in [SRS.md](SRS.md). Use cases
were identified ([USE_CASES.md](USE_CASES.md)) and data flows drawn
([DFD level 0](diagrams/exports/dfd-level-0.png), [level 1](diagrams/exports/dfd-level-1.png)).

Feasibility:
- **Technical:** all tools are free and run on a normal laptop.
- **Economic:** no cost (open-source software, local execution).
- **Operational:** a browser-based interface needs no training.

## 4. System design
- **Architecture:** MVC + DAO on a three-tier (browser / Tomcat / MySQL) monolith — [ARCHITECTURE.md](ARCHITECTURE.md).
- **Classes:** [class diagram](diagrams/exports/class-diagram.png).
- **Interactions:** sequence diagrams for [registration](diagrams/exports/registration-sequence.png),
  [login](diagrams/exports/login-sequence.png) and [management](diagrams/exports/student-management-sequence.png).
- **Behaviour:** activity diagrams for [registration](diagrams/exports/activity-registration.png) and
  [admin management](diagrams/exports/activity-admin-management.png).
- **Security design:** PreparedStatement, server-side validation, BCrypt, session filter, CSRF
  token, output escaping, POST-only changes.
- **UI design:** navy/white theme, Bootstrap grid, sidebar admin layout, responsive breakpoints.

## 5. Database design
Two tables (`students`, `admin`) with primary keys, UNIQUE, NOT NULL, CHECK constraints and indexes
— [DATABASE_DESIGN.md](DATABASE_DESIGN.md). Normalised to 3NF; derived values (age, display ID) are
calculated, not stored.

## 6. Implementation (increments)

Each increment ended with a build, tests, a commit and a push to GitHub. The Git history shows them:

| # | Increment | Main files | Commit message |
|---|---|---|---|
| 0 | Environment and repository audit | — | — |
| 1 | Maven project, Tomcat 9 configuration, `.gitignore` | `pom.xml`, `web.xml`, `context.xml` | *Initial project structure with Maven and Tomcat 9 configuration* |
| 2 | Database connectivity + start-up check | `DBConnection`, `AppContextListener`, `db.properties.example` | *Add MySQL database connectivity* |
| 3 | Schema and sample data | `schema.sql`, `sample_data.sql` | *Add database schema and fictional sample data* |
| 4 | Models and DAOs | `model/*`, `dao/*` | *Add model classes and DAO layer* |
| 5 | Validation and security utilities | `util/*` | *Add validation, password, CSRF and CSV utilities* |
| 6 | Public registration | `RegisterServlet`, JSPs, CSS/JS | *Implement public student registration* |
| 7 | Authentication | `AdminSetup`, `LoginServlet`, `AuthFilter`, `LogoutServlet` | *Add admin authentication* |
| 8 | Dashboard | `DashboardServlet`, `dashboard.jsp` | *Build admin dashboard* |
| 9 | CRUD | profile, add, edit, delete, status servlets | *Implement student CRUD operations* |
| 10 | Search, filters, paging, CSV | `StudentListServlet`, `ExportStudentsServlet` | *Add search, filtering, pagination and CSV export* |
| 11 | Bug fixes from testing | CSS, JSPs, `Student` | *Fix issues found during testing* |
| 12 | Automated tests | `src/test`, `scripts/functional-test.sh` | *Add unit, integration and functional tests* |
| 13 | Diagrams and documentation | `docs/` | *Add UML, ER, DFD and architecture diagrams* and documentation commits |
| 14 | Final verification: fresh clone + fresh database, all tests, responsive fixes | `students.jsp`, `dashboard.jsp`, sidebar, `app.css`, test docs | *Improve responsive layout of admin tables*, *Add README screenshots and final verification results* |

Practices followed:
- Meaningful names, comments on important logic, short methods.
- No SQL in JSPs, no HTML in servlets.
- No secrets in the repository; the admin is created with a setup tool.
- Every change verified in a running Tomcat with a real MySQL database, not only compiled.

## 7. Testing
Four levels (details in [TEST_PLAN.md](TEST_PLAN.md)):
1. **Unit tests** (JUnit 5) — validation, hashing, CSV, model helpers.
2. **Integration tests** — DAO methods against MySQL, including constraints and SQL injection.
3. **Functional tests** — 67 automated HTTP checks of the deployed application.
4. **Manual/browser tests** — UI behaviour, mobile layout, print, database outage, performance.

## 8. Validation
Results were compared with requirements in the [traceability matrix](TRACEABILITY_MATRIX.md). Every
functional requirement is verified by at least one executed test, except the 30-minute timeout
(configuration verified, timed test pending). Ten defects were found and fixed during testing and final verification
([TEST_RESULTS.md §5](TEST_RESULTS.md#5-defects-found-and-fixed)).

## 9. Limitations
See [PROJECT_OVERVIEW.md §9](PROJECT_OVERVIEW.md#9-limitations): single admin role, no student
login, no email verification, no login lockout, no connection pool, fixed lists, hard delete,
HTTP on localhost only.

## 10. Future scope
See [PROJECT_OVERVIEW.md §10](PROJECT_OVERVIEW.md#10-future-enhancements): student portal, email
verification, documents upload, roles, lookup tables, audit log, lockout, connection pool, charts,
HTTPS deployment.
