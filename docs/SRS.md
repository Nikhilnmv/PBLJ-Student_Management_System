# Software Requirements Specification (SRS)

**Product:** Student Registration System
**Version:** 1.0
**Prepared for:** Project Based Learning Using Java (PBLJ) — Minor Project
**Status:** Requirements below describe the **implemented** system. Each requirement ID is traced
to code and tests in [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md).

---

## 1. Introduction

### 1.1 Purpose
This document specifies the functional and non-functional requirements of the Student Registration
System, a web application through which students register online and administrators manage the
registrations. It is intended for the project guide, examiners, and anyone who maintains or tests
the system.

### 1.2 Scope
The system provides:
- a public registration form with validation, duplicate prevention and an automatically generated
  registration ID;
- a protected administrator area with login/logout, a statistics dashboard, and student management
  (list, search, filter, view, add, edit, delete, change status, export to CSV, print).

It runs locally on one computer using Apache Tomcat 9 and MySQL 8. Online payment, student login,
notifications and internet deployment are outside the scope.

### 1.3 Definitions and abbreviations

| Term | Meaning |
|---|---|
| Admin | Administrator — college staff member with a login |
| Registration ID | Display ID derived from the database ID, e.g. `SRS-00017` |
| Roll number | The college's own student number, e.g. `23CSE001` |
| CRUD | Create, Read, Update, Delete |
| DAO | Data Access Object — class containing database code |
| MVC | Model–View–Controller architecture |
| JDBC | Java Database Connectivity |
| CSRF | Cross-Site Request Forgery |
| XSS | Cross-Site Scripting |
| BCrypt | Adaptive, salted password-hashing algorithm |
| PRG | Post/Redirect/Get pattern |

### 1.4 References
- [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md), [ARCHITECTURE.md](ARCHITECTURE.md),
  [DATABASE_DESIGN.md](DATABASE_DESIGN.md), [USE_CASES.md](USE_CASES.md)
- Java Servlet Specification 4.0; JSP 2.3; JSTL 1.2
- MySQL 8.0 Reference Manual

---

## 2. Overall description

### 2.1 Product perspective
A self-contained, monolithic web application (one WAR file) backed by a relational database.

```
Browser  ⇄  Apache Tomcat 9 (Servlets + JSP)  ⇄  JDBC  ⇄  MySQL 8
```

### 2.2 Product functions (summary)
1. Student registration with validation and registration ID.
2. Administrator authentication (login, logout, session timeout).
3. Dashboard statistics from live database queries.
4. Student list with search, filters and pagination.
5. Student profile, add, edit, delete, status change.
6. CSV export and printing.
7. One-time command-line setup of the first administrator.

### 2.3 User classes and characteristics

| User class | Description | Access | Frequency |
|---|---|---|---|
| Student (public visitor) | Registers once; no account | `/`, `/register`, `/register/success` | Occasional |
| Administrator | College office staff; trusted; basic computer skills | Everything under `/admin/*` after login | Daily |
| System installer | Student/developer who installs the system and creates the first admin | `AdminSetup` tool, database | Once |

### 2.4 Operating environment
- Server: Java 17, Apache Tomcat 9.0.x, MySQL 8.0 on Windows 10/11, macOS 12+ or Linux.
- Client: a current version of Chrome, Edge, Firefox or Safari, desktop or mobile.

### 2.5 Design and implementation constraints
- Must use Java Servlets/JSP with `javax.servlet` (Servlet 4.0) — therefore Tomcat 9, not 10+.
- Must use plain JDBC with `PreparedStatement`; no ORM, no Spring.
- Must use MySQL as the relational database (no mock data, no browser storage).
- Must be beginner-friendly and explainable in a viva.
- Credentials must not be stored in the source repository.
- Must run locally without paid services or internet access.

### 2.6 Assumptions and dependencies
- MySQL is installed and running on `localhost:3306` and `schema.sql` has been imported.
- The administrator creates the first admin account with `AdminSetup` before logging in.
- Departments and courses offered by the college are those listed in `AppConstants`.
- Students have a unique roll number and email address.
- Phone numbers are Indian 10-digit mobile numbers.
- Dependencies: MySQL Connector/J 8.4, JSTL 1.2, jBCrypt 0.4, Bootstrap 5.3 (bundled), JUnit 5 (tests).

---

## 3. Functional requirements

### 3.1 Student registration

| ID | Requirement |
|---|---|
| FR-01 | The system shall provide a public registration form, without login, with the fields: full name, roll number, email, phone, gender, date of birth, department, course, current semester and address. |
| FR-02 | The system shall reject the registration if any mandatory field is empty, and show a message next to each missing field. |
| FR-03 | The system shall validate the email format (`name@domain.tld`, max 100 characters). |
| FR-04 | The system shall validate the phone number as 10 digits starting with 6, 7, 8 or 9. |
| FR-05 | The system shall validate: semester 1–8; age 15–60 from the date of birth (no future dates); department and course from the predefined lists; gender Male/Female/Other; name 2–100 letters/spaces/`.'-`; roll number 3–20 letters/digits/`/`/`-`; address 5–500 characters. |
| FR-06 | The system shall prevent two students from having the same roll number (case-insensitive). |
| FR-07 | The system shall prevent two students from having the same email address (case-insensitive). |
| FR-08 | The system shall store a valid registration in MySQL with status *Active* and the registration date/time, regardless of any status value sent by the browser. |
| FR-09 | The system shall automatically generate a unique student ID and display it as a registration ID (`SRS-` + 5 digits) on a success page together with a summary of the registration. |
| FR-10 | The system shall not create a duplicate record when the success page is refreshed (Post/Redirect/Get). |

### 3.2 Administrator authentication

| ID | Requirement |
|---|---|
| FR-11 | The system shall allow an administrator to log in with a username and password verified against a BCrypt hash stored in the `admin` table. |
| FR-12 | The system shall show the same generic message for an unknown username and a wrong password. |
| FR-13 | The system shall protect every URL under `/admin/` and redirect users who are not logged in to the login page. |
| FR-14 | The system shall create a new session on successful login and end it on logout (session invalidation). |
| FR-15 | The system shall end an administrator session after 30 minutes of inactivity. |
| FR-16 | The system shall provide a command-line setup tool to create the first administrator (and reset a password) without storing a plain-text password in any file. |

### 3.3 Dashboard

| ID | Requirement |
|---|---|
| FR-17 | The dashboard shall display the total number of students, active students, inactive students and number of departments with students, all calculated by SQL queries at request time. |
| FR-18 | The dashboard shall list the five most recent registrations. |
| FR-19 | The dashboard shall display the number of students per department. |

### 3.4 Student management

| ID | Requirement |
|---|---|
| FR-20 | The system shall list all students, newest first, 10 per page with page navigation. |
| FR-21 | The system shall search students by name, roll number or email (partial match). |
| FR-22 | The system shall filter students by department, semester and status; filters can be combined with the search. |
| FR-23 | The system shall display the complete profile of a student. |
| FR-24 | The administrator shall be able to add a student (same validation as FR-02 to FR-07, plus status). |
| FR-25 | The administrator shall be able to edit a student; validation and duplicate checks shall ignore the student's own current values. |
| FR-26 | The administrator shall be able to delete a student after confirming in a dialog; deletion shall only be possible through a POST request. |
| FR-27 | The administrator shall be able to change a student's status between Active and Inactive. |
| FR-28 | The administrator shall be able to export the (filtered) student list as a CSV file. |
| FR-29 | The system shall provide a print-friendly student profile and registration slip. |
| FR-30 | The system shall show success/error notifications after actions and helpful empty-state messages when no data matches. |

---

## 4. Non-functional requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-01 | Security | All SQL statements shall use `PreparedStatement` with bound parameters (SQL-injection protection). |
| NFR-02 | Security | Administrator passwords shall be stored only as salted BCrypt hashes (cost 12). |
| NFR-03 | Security | Every data-changing admin request shall be a POST carrying a valid per-session CSRF token. |
| NFR-04 | Security | User-supplied data shall be HTML-escaped when displayed (XSS protection). |
| NFR-05 | Security | No passwords or other secrets shall be committed to the Git repository. |
| NFR-06 | Security | Session cookies shall be HttpOnly and SameSite=Lax; pages shall send `X-Frame-Options: DENY`; admin pages shall not be cached. |
| NFR-07 | Reliability | Users shall never see stack traces; errors shall show a friendly page and be logged on the server; the application shall recover automatically when the database becomes available again. |
| NFR-08 | Data integrity | The database shall enforce primary keys, UNIQUE (roll number, email, username), NOT NULL and CHECK (semester, status, gender) constraints. |
| NFR-09 | Usability | The interface shall be responsive (phone to desktop), use a consistent theme, label all fields, mark required fields, and give instant browser-side feedback. |
| NFR-10 | Performance | With 10,000 student records on a typical laptop, list, search, filter and dashboard pages shall respond in under 1 second. |
| NFR-11 | Maintainability | Code shall follow MVC + DAO, keep SQL only in DAO classes, and include comments and automated tests. |
| NFR-12 | Portability | The system shall run on Windows, macOS and Linux with Java 17, Tomcat 9 and MySQL 8, and work without internet access (no CDN). |

---

## 5. Hardware requirements

| Component | Minimum | Recommended |
|---|---|---|
| Processor | Dual-core 64-bit, 2 GHz | Quad-core |
| RAM | 4 GB | 8 GB |
| Free disk space | 2 GB (JDK, Maven repository, MySQL, Tomcat) | 5 GB |
| Display | 1280 × 720 | 1920 × 1080 |
| Network | Not required after installation (localhost) | — |

## 6. Software requirements

| Software | Version |
|---|---|
| Operating system | Windows 10/11, macOS 12+, or Ubuntu 22.04+ |
| Java Development Kit | 17 (LTS) |
| Apache Maven | 3.8 or newer |
| MySQL Community Server | 8.0.16 or newer (CHECK constraints) |
| Apache Tomcat | 9.0.x |
| Web browser | Current Chrome, Edge, Firefox or Safari |
| IDE (development) | IntelliJ IDEA or Eclipse IDE for Enterprise Java |
| Git | Any recent version |

## 7. System constraints
- Single administrator role; no role-based permissions.
- One application instance and one database on the same machine (no clustering).
- HTTP on localhost only; HTTPS would be required for real deployment.
- Department and course lists are fixed in code (`AppConstants`).

## 8. Assumptions and dependencies (summary)
See section 2.6.
