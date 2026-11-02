# Project Overview — Student Registration System

**Subject:** Project Based Learning Using Java (PBLJ) — Minor Project
**Domain:** Education
**Type:** Web-based student registration and management system
**Technologies:** Java 17, Java Servlets 4.0, JSP + JSTL, JDBC, MySQL 8, Apache Tomcat 9, Maven, Bootstrap 5

---

## 1. What is the Student Registration System?

It is a website that a college can run to collect and manage student registrations.

- **Students** open a public registration form, enter their personal, academic and contact
  details, and immediately receive a unique registration ID (for example `SRS-00017`).
- **Administrators** log in to a protected admin panel where they can see live statistics,
  search and filter all registrations, view complete profiles, edit records, mark students
  Active or Inactive, delete records, print profiles and export the list to a CSV file.

All data is stored permanently in a **MySQL** relational database.

## 2. The problem being solved

Many colleges still register students on paper forms or in spreadsheets that are emailed around.
This causes typical problems:

| Problem with paper / spreadsheets | How this system solves it |
|---|---|
| Handwriting mistakes, missing fields | Every field is validated in the browser **and** on the server before it is saved |
| The same student registered twice | The database itself rejects duplicate roll numbers and email addresses (UNIQUE constraints) |
| Slow to find one student among hundreds | Search by name, roll number or email; filter by department, semester and status |
| Counting students per department is manual work | The dashboard calculates totals with SQL queries in real time |
| Anyone with the file can change it | Only logged-in administrators can change data; passwords are stored as BCrypt hashes |
| Copies get out of date | One central database is the single source of truth |

## 3. Why the application is useful

- Students can register from any device with a browser (the layout is responsive).
- Staff save time: no re-typing, instant search, one-click CSV export for Excel.
- Data is consistent: constraints in MySQL guarantee valid semesters, statuses and unique IDs.
- It demonstrates, in a small and understandable codebase, how real web applications are built:
  HTTP, servlets, JSP, sessions, JDBC, SQL and the MVC/DAO design.

## 4. Objectives

1. Provide a public, validated online registration form.
2. Store registrations in a normalised MySQL table with proper keys and constraints.
3. Generate a unique registration ID automatically.
4. Provide secure administrator login with hashed passwords and session management.
5. Allow administrators to view, search, filter, add, edit, delete and change the status of students.
6. Show a dashboard with statistics calculated directly from the database.
7. Follow the MVC architecture with a DAO layer so the code is organised and easy to explain.
8. Apply essential security practices: PreparedStatement, server-side validation, output escaping,
   CSRF tokens, POST-only modifications, and no secrets in the repository.
9. Test the system with unit, integration and functional tests and document the results.

## 5. Scope

**In scope**

- Public student registration with validation and duplicate prevention.
- Registration success page showing the generated ID.
- Single role of administrator: login, logout, session timeout (30 minutes).
- Dashboard: total, active, inactive, departments, recent registrations, per-department counts.
- Student management: list with paging, search, filters, profile, add, edit, delete, status change.
- CSV export and print-friendly profile.
- Local execution on one computer (Tomcat + MySQL on `localhost`).

**Out of scope** (see [Future enhancements](#10-future-enhancements))

- Students logging in to edit their own data.
- Fee payment, course allocation, attendance, results.
- Email/SMS notifications, document upload.
- Multiple admin roles with different permissions.
- Internet deployment, HTTPS certificates, cloud hosting.

## 6. Target users

| User | Needs | Technical skill |
|---|---|---|
| Student (public visitor) | Register quickly, get a confirmation ID | Basic — uses a browser |
| Administrator (college office staff) | Find, verify, correct and export student records; see statistics | Basic to moderate |
| Developer / examiner | Understand and verify the implementation | Java and SQL knowledge |

## 7. Major functionalities

| # | Function | Who | Where in the code |
|---|---|---|---|
| 1 | Register with validation | Student | `RegisterServlet`, `ValidationUtil`, `StudentDAO.addStudent` |
| 2 | Show registration ID | Student | `RegistrationSuccessServlet`, `registration-success.jsp` |
| 3 | Admin login / logout | Admin | `LoginServlet`, `LogoutServlet`, `PasswordUtil` |
| 4 | Protect admin pages | System | `AuthFilter` |
| 5 | Dashboard statistics | Admin | `DashboardServlet`, `StudentDAO.count…` methods |
| 6 | List, search, filter, paging | Admin | `StudentListServlet`, `StudentDAO.searchStudents` |
| 7 | View profile, print | Admin | `StudentProfileServlet`, `student-profile.jsp` |
| 8 | Add / edit student | Admin | `AddStudentServlet`, `EditStudentServlet` |
| 9 | Delete student | Admin | `DeleteStudentServlet` |
| 10 | Active / Inactive status | Admin | `StudentStatusServlet` |
| 11 | CSV export | Admin | `ExportStudentsServlet`, `CsvUtil` |
| 12 | Create first admin | Developer | `AdminSetup` (command-line tool) |

## 8. Technology stack

| Component | Technology | Role in this project |
|---|---|---|
| Language | Java 17 | All server-side code |
| Web layer | Java Servlets 4.0 (`javax.servlet`) | Controllers that receive HTTP requests |
| Views | JSP 2.3 + JSTL 1.2 | HTML pages filled with data (no Java code inside) |
| Frontend | HTML5, CSS3, JavaScript, Bootstrap 5.3, Bootstrap Icons | Layout, styling, form checks, confirm dialog |
| Database | MySQL 8.0 | Permanent storage (`students`, `admin` tables) |
| Connectivity | JDBC with MySQL Connector/J 8.4 | Java ⇄ MySQL communication |
| Password hashing | jBCrypt 0.4 | One-way salted hashes for admin passwords |
| Server | Apache Tomcat 9 | Runs the servlets and JSPs |
| Build | Maven 3 (WAR packaging) | Dependencies, compilation, tests, packaging |
| Testing | JUnit 5, Bash + curl functional test script, manual browser testing | Verification of every feature |
| Architecture | MVC + DAO | Separation of concerns |

## 9. Limitations

These are honest limitations of the current version:

1. **Single administrator role** — every admin can do everything; there is no "view-only" role.
2. **No self-service for students** — after registering, a student cannot log in to see or correct
   their own record; they must contact an administrator.
3. **No email verification** — the system checks the email *format*, not that the mailbox exists.
4. **No login rate-limiting** — repeated wrong passwords are not locked out (BCrypt's slowness helps,
   but a lockout would be better).
5. **No connection pool** — each database operation opens a new JDBC connection. This is simple and
   fine for a college project, but slower under heavy load.
6. **Fixed lists** — departments and courses are defined in `AppConstants.java`; adding one needs a
   code change and rebuild.
7. **Hard delete** — deleting a student removes the row permanently; there is no recycle bin or audit log.
8. **HTTP only on localhost** — for real deployment HTTPS would be required.
9. **Search uses `LIKE '%text%'`** — fine for thousands of rows, but cannot use an index for the
   leading wildcard; full-text search would be needed for very large data.

## 10. Future enhancements

- Student login to view registration status and update contact details.
- Email confirmation with a verification link; SMS notifications.
- Upload of photo and documents (marksheets, ID proof).
- Multiple admin roles (super admin, department admin, read-only).
- `departments` and `courses` tables managed from the admin panel (with foreign keys).
- Soft delete and an audit log of who changed what and when.
- Login attempt limiting and account lockout; "forgot password" by email.
- Connection pooling (Tomcat JDBC pool / HikariCP) for better performance.
- Charts (e.g. registrations per month) and PDF reports.
- Deployment to a cloud server with HTTPS.
