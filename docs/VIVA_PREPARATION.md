# Viva Preparation Guide

A complete manual for presenting and defending the Student Registration System. Read it several
times, practise the introductions aloud, and rehearse the demonstration at least twice on your own
laptop.

**Sections**
- [A. Project introduction](#section-a-project-introduction)
- [B. Architecture explanation](#section-b-architecture-explanation)
- [C. Java questions](#section-c-java-questions)
- [D. DBMS and SQL questions](#section-d-dbms-and-sql-questions)
- [E. Code-based questions](#section-e-code-based-viva-questions)
- [F. Practical demonstration](#section-f-practical-demonstration)
- [G. Difficult questions](#section-g-difficult-viva-questions)
- [H. Quick revision](#section-h-quick-revision)

---

## Section A: Project introduction

### A.1 30-second introduction
> "Good morning. My project is a **Student Registration System**. It is a web application built with
> **Java Servlets, JSP, JDBC and MySQL**, running on **Apache Tomcat 9**. Students fill in an online
> form and immediately get a registration ID. Administrators log in securely to see live statistics
> and to search, edit, delete and export student records. I followed the **MVC architecture with a DAO
> layer**, and I used PreparedStatements and BCrypt password hashing for security."

### A.2 One-minute introduction
> "My project is a Student Registration System for a college. The problem it solves is that paper
> or spreadsheet registration causes mistakes, duplicate entries and slow searching.
>
> There are two kinds of users. **Students** use a public registration form with ten fields — name,
> roll number, email, phone, gender, date of birth, department, course, semester and address. Every
> field is validated in the browser and again on the server, duplicates are rejected, and the student
> gets a unique registration ID like SRS-00017.
>
> **Administrators** log in with a BCrypt-hashed password. They see a dashboard with total, active and
> inactive students and department-wise counts, all calculated by SQL queries. They can search by
> name, roll number or email, filter by department, semester and status, view, add, edit, delete,
> activate or deactivate students, print profiles and export a CSV file.
>
> Technically it uses Java 17, Servlets and JSP on Tomcat 9, MySQL 8 through JDBC, Maven to build,
> and Bootstrap for a responsive design. It follows MVC with a DAO layer."

### A.3 Detailed two-minute introduction
> "My project is a **web-based Student Registration System** developed for Project Based Learning
> Using Java.
>
> **Problem.** In many colleges registration is still on paper or in Excel sheets. That leads to
> missing or wrong data, the same student registered twice, and a lot of manual work to find a student
> or count students per department.
>
> **Solution.** I built a web application with two parts. The **public part** is a registration form.
> When a student submits it, a servlet called `RegisterServlet` reads the data, validates every field
> on the server using `ValidationUtil`, checks that the roll number and email are not already
> registered, and then `StudentDAO` inserts the record into MySQL using a PreparedStatement. MySQL
> generates an auto-increment ID which I show as a registration ID like SRS-00017. I use the
> Post-Redirect-Get pattern so refreshing the page can't create a duplicate.
>
> The **admin part** is protected. The admin logs in; the password is checked against a **BCrypt hash**
> — the real password is never stored. A new HTTP session is created, and a **Servlet Filter** called
> `AuthFilter` checks that session for every URL under `/admin`. The dashboard shows total, active,
> inactive and department statistics, all from live SQL queries like COUNT and GROUP BY. The admin can
> search, filter, view profiles, add, edit, delete with confirmation, change status, export CSV and print.
>
> **Architecture.** I used **MVC with DAO**: model classes like `Student`, JSP views, servlets as
> controllers, and DAO classes containing all SQL. Utilities handle database connections, validation,
> hashing and CSRF tokens.
>
> **Security.** PreparedStatement against SQL injection, server-side validation, BCrypt, session
> filter, CSRF tokens on every admin form, output escaping against XSS, and no passwords in GitHub.
>
> **Testing.** I wrote 34 JUnit unit tests, 15 integration tests against MySQL and a script with 69
> automated HTTP checks; all pass. I also tested it with 10,000 students — every page responded in
> under 50 milliseconds.
>
> I can now demonstrate it."

### A.4 Problem statement
Manual registration with paper forms or spreadsheets is error-prone, allows duplicate entries, makes
searching and reporting slow, and offers no access control. The college needs a centralised,
validated, secure system for collecting and managing student registrations.

### A.5 Proposed solution
A Java web application where students self-register through a validated form and administrators
manage records through a protected panel; data is stored in a MySQL database with integrity
constraints; the code follows MVC + DAO.

### A.6 Objectives
1. Online registration with validation and an automatic unique ID.
2. Prevent duplicate roll numbers and emails.
3. Secure admin login (BCrypt, sessions, protected pages).
4. Search, filter, view, add, edit, delete and change status of students.
5. Live dashboard statistics from the database.
6. Clean MVC + DAO code and essential web security.
7. Tested and documented system.

### A.7 Features
Registration form · registration ID · admin login/logout · 30-min session timeout · dashboard (4
cards, recent registrations, department bars) · list with paging · search by name/roll/email ·
filters by department/semester/status · profile · add · edit · delete with confirmation ·
Active/Inactive status · CSV export · print · flash messages · empty states · responsive design ·
friendly error pages · admin setup tool.

### A.8 Technology stack
Java 17 · Servlets 4.0 (`javax.servlet`) · JSP 2.3 + JSTL 1.2 · HTML/CSS/JavaScript · Bootstrap 5.3 ·
MySQL 8.0 · JDBC (MySQL Connector/J 8.4) · jBCrypt · Apache Tomcat 9 · Maven · JUnit 5 · Git/GitHub.

### A.9 Advantages
- Correct data: validation + database constraints.
- No duplicates even under simultaneous submissions.
- Fast search and live statistics.
- Secure: hashed passwords, protected admin area, SQL-injection and XSS protection.
- Works on phones and desktops, and offline (no CDN).
- Free software only; runs on any laptop.

### A.10 Limitations (say them honestly)
Single admin role · students cannot log in to edit their data · no email verification · no lockout
after failed logins · no connection pool · department list fixed in code · hard delete without audit
trail · HTTP on localhost only.

---

## Section B: Architecture explanation

### B.1 Explain your project architecture.
**Short:** "Three tiers — browser, Tomcat, MySQL — and inside Tomcat I used MVC with a DAO layer."

**Detailed:** "The browser sends HTTP requests to Apache Tomcat. Requests to `/admin/*` first go
through `AuthFilter`, which checks the session. Then a **servlet** — the controller — handles the
request. It reads parameters, validates them with `ValidationUtil`, and calls a **DAO** such as
`StudentDAO`. The DAO gets a connection from `DBConnection` and runs SQL with **PreparedStatement**
through **JDBC** on **MySQL**. Results come back as a `ResultSet`, which the DAO converts into
**model** objects like `Student`. The servlet puts those objects into the request with `setAttribute`
and **forwards** to a **JSP**, the view, which uses JSTL and EL to generate HTML. Tomcat sends that
HTML back to the browser."
(Show [architecture diagram](diagrams/exports/architecture.png).)

### B.2 Why did you use MVC?
**Short:** "To separate data, presentation and request handling, so each part has one job."

**Detailed:** "Without MVC, JSP pages would mix HTML, Java and SQL — hard to read and to change. With
MVC my JSPs only display data, my servlets only control the flow, and my model classes only hold
data. I can change the design of a page without touching Java, or change a query without touching
any page. It also makes testing easier — for example `ValidationUtil` is tested without a server."

### B.3 What is DAO?
**Short:** "Data Access Object — a class that contains all the database code for one table."

**Detailed:** "`StudentDAO` has methods like `addStudent`, `getStudentById`, `searchStudents`,
`updateStudent`, `deleteStudent` and the statistics queries. Servlets call these methods and never
write SQL. So all SQL is in one place, which is easier to maintain and to check for SQL injection. If I
moved to another database, only the DAO would change."

### B.4 What is the role of a Servlet?
**Short:** "A servlet is the controller: a Java class that receives an HTTP request and produces a response."

**Detailed:** "Each of my servlets extends `HttpServlet` and is mapped to a URL with `@WebServlet`.
`doGet` handles opening a page; `doPost` handles form submission. For example `RegisterServlet.doPost`
reads the form with `request.getParameter`, validates it, calls `studentDAO.addStudent`, then calls
`response.sendRedirect` to the success page. Tomcat creates one servlet object and calls it on a
separate thread for each request."

### B.5 What is the role of JSP?
**Short:** "JSP is the view — HTML pages that display data provided by the servlet."

**Detailed:** "My JSPs contain no Java code. They use **Expression Language** like `${student.fullName}`
and **JSTL** tags like `<c:forEach>` and `<c:if>`. I print user data with `<c:out>` which escapes HTML
to prevent XSS. The JSPs are inside `WEB-INF` so they cannot be opened directly — always through a
servlet. Common parts like the navigation bar and form fields are reusable `.jspf` fragments."

### B.6 How does JDBC work?
**Short:** "JDBC is Java's standard API to talk to databases; the MySQL driver implements it."

**Detailed:** "Six steps: load the driver (`Class.forName("com.mysql.cj.jdbc.Driver")`), open a
`Connection` with `DriverManager.getConnection(url, user, password)`, create a `PreparedStatement` with
`?` placeholders, set the values with `setString`/`setInt`, execute with `executeQuery` or
`executeUpdate`, read the `ResultSet` with `next()`, and close everything — I use try-with-resources
so closing is automatic."

### B.7 How does MySQL connect to Java?
"Through the MySQL Connector/J JDBC driver, which is a Maven dependency packaged in the WAR. The
connection URL is `jdbc:mysql://localhost:3306/student_registration_db?...` — protocol, host, port,
database. The username and password come from `db.properties` or environment variables, read once by
`DBConnection`. The app connects as a restricted MySQL user `srs_app` that only has SELECT, INSERT,
UPDATE and DELETE on this database."

### B.8 What happens when a student submits the registration form?
1. The browser checks required fields; `app.js` disables the button to stop double clicks.
2. A **POST** to `/register` reaches `RegisterServlet.doPost`.
3. `StudentRequestHelper.readStudentFromRequest` builds a `Student`, trimming, upper-casing the roll
   number and lower-casing the email. Status is forced to Active.
4. `ValidationUtil.validateStudent` checks every field; `StudentDAO.isRollNumberTaken` and
   `isEmailTaken` check duplicates.
5. If there are errors, the form is shown again with messages.
6. Otherwise `StudentDAO.addStudent` runs the INSERT with a PreparedStatement and gets the
   auto-generated ID.
7. The saved student is put in the session and the servlet **redirects** to `/register/success`.
8. `RegistrationSuccessServlet` shows the registration ID once and removes it from the session.

### B.9 Explain the complete request–response lifecycle.
"The browser opens a TCP connection to port 8080 and sends an HTTP request: method, URL, headers
including the session cookie, and for POST a body with form data. Tomcat parses it into an
`HttpServletRequest`, selects my application by the context path `/student-registration`, runs the
filters — the security headers filter and, for `/admin/*`, `AuthFilter` — then calls the servlet's
`service` method, which dispatches to `doGet` or `doPost`. The servlet calls the DAO, which queries
MySQL. The servlet then either **forwards** to a JSP — which renders HTML into the
`HttpServletResponse` — or **redirects**, which sends status 302 with a Location header so the browser
makes a new GET request. Tomcat sends the status line, headers and body back, and the browser renders
the page." (Show [request-lifecycle](diagrams/exports/request-lifecycle.png).)

---

## Section C: Java questions

**Q1. What is a class and an object? Example from your project.**
A class is a blueprint; an object is an instance. `Student` is a class; every row read from MySQL
becomes a `Student` object via `new Student()` in `mapRowToStudent`.

**Q2. What is encapsulation? Where did you use it?**
Hiding fields with `private` and exposing getters/setters. All fields of `Student`, `Admin` and
`StudentFilter` are private. Rules like building the registration ID are inside the class (`getRegistrationId()`).

**Q3. What is a constructor? Do you have constructor overloading?**
A special method that initialises an object. `Student` has two constructors: a no-argument one that
sets status Active, and one with ten parameters that calls `this()` first — that is overloading and
constructor chaining.

**Q4. Why getters and setters if fields could be public?**
Control and flexibility — I can validate or calculate values, and JSP EL requires getters
(`${student.fullName}` calls `getFullName()`).

**Q5. What is inheritance? Where is it used?**
A class extending another to reuse its behaviour. All my servlets extend `HttpServlet`;
`DuplicateStudentException` extends `Exception`.

**Q6. What is an interface? Where did you use one?**
A contract of methods. `AuthFilter implements Filter`, `AppContextListener implements
ServletContextListener`, `Student implements Serializable`. JDBC's `Connection`, `PreparedStatement`
and `ResultSet` are interfaces implemented by the MySQL driver.

**Q7. What is polymorphism here?**
Overriding: my servlets override `doGet`/`doPost`, and Tomcat calls them through an `HttpServlet`
reference. Overloading: `StudentDAO.searchStudents(filter)` and `searchStudents(filter, offset, limit)`.

**Q8. Abstract class vs interface?**
An abstract class can have state and implemented methods and you extend only one; an interface is a
contract and a class can implement many. `HttpServlet` is an abstract class I extend; `Filter` is an
interface I implement.

**Q9. What is a package? Which packages do you have?**
A namespace for related classes: `com.student.model`, `.dao`, `.servlet`, `.filter`, `.listener`, `.util`.

**Q10. Checked vs unchecked exceptions?**
Checked exceptions (like `SQLException`, `IOException`) must be caught or declared with `throws`;
unchecked (`RuntimeException`, e.g. `NumberFormatException`) need not be. My DAO methods declare
`throws SQLException`; `parseInteger` catches `NumberFormatException`.

**Q11. How do you handle exceptions in servlets?**
I catch `SQLException`, log it with `log(...)`, and throw `ServletException`; `web.xml` maps errors to
`error.jsp`, so users see a friendly page while details go to the Tomcat log. Duplicates are caught as
`DuplicateStudentException` and shown as form errors.

**Q12. Why did you create a custom exception?**
`DuplicateStudentException` carries which field (`rollNumber` or `email`) is duplicated, so the servlet
can show the message beside the correct input.

**Q13. What is try-with-resources?**
A try statement that automatically closes resources implementing `AutoCloseable`. I use it for every
Connection, PreparedStatement and ResultSet so connections are never leaked, even when an exception occurs.

**Q14. Which collections did you use and why?**
`List<Student>`/`ArrayList` for query results; `Map<String,String>`/`LinkedHashMap` for validation
errors (keeps form order); `Map<String,Integer>`/`LinkedHashMap` for department counts (keeps SQL
order); `List.of(...)` for unmodifiable constant lists.

**Q15. HashMap vs LinkedHashMap?**
LinkedHashMap keeps insertion order; HashMap doesn't. I need the department order from `ORDER BY total DESC`.

**Q16. What is `static`? Why are your utility methods static?**
Belongs to the class, not objects. Validation and hashing need no object state, so
`ValidationUtil.isValidEmail(...)` is called without creating an object. Constructors are private.

**Q17. What is a static block? Where is it?**
Code that runs once when the class loads. `DBConnection`'s static block loads the driver and reads
the configuration.

**Q18. What does `final` mean in your code?**
Constants (`static final String INSERT_STUDENT_SQL`), a field that cannot be reassigned
(`private final String field`), and classes that cannot be extended (`final class StudentRequestHelper`).

**Q19. What is a servlet's life cycle?**
Load class → create one instance → `init()` once → `service()` (→ `doGet`/`doPost`) for every request
on separate threads → `destroy()` at shutdown.

**Q20. GET vs POST?**
GET sends parameters in the URL, is bookmarkable and must not change data — I use it for list,
search, profile and export. POST sends data in the body — I use it for register, login, add, edit,
delete, status and logout.

**Q21. forward vs sendRedirect?**
`forward` passes the same request to a JSP inside the server (URL unchanged, attributes kept).
`sendRedirect` tells the browser (302) to make a new request (URL changes). I forward to show pages and
redirect after successful POSTs.

**Q22. What is HttpSession? How do you use it?**
Server-side storage for one browser identified by the `JSESSIONID` cookie. I store the logged-in admin,
the CSRF token, flash messages and the just-registered student.

**Q23. `getSession(true)` vs `getSession(false)`?**
`true` creates a session if none exists; `false` returns `null`. `AuthFilter` uses `false` so it doesn't
create sessions for anonymous visitors.

**Q24. What is a Filter?**
A component that runs before servlets for matching URLs. `AuthFilter` (`@WebFilter("/admin/*")`)
checks login and CSRF tokens and then calls `chain.doFilter`.

**Q25. What are annotations you used?**
`@WebServlet`, `@WebFilter`, `@WebListener`, `@Override`; in tests `@Test`, `@DisplayName`,
`@AfterEach`, `@EnabledIfSystemProperty`.

**Q26. What is JSTL and EL?**
EL `${...}` reads attributes and properties; JSTL provides tags like `<c:forEach>`, `<c:if>`,
`<c:choose>`, `<c:out>`, `<c:set>`, `<c:url>`, `<c:remove>` so JSPs need no Java code.

**Q27. What is Maven and what is `provided` scope?**
A build tool that downloads dependencies and builds the WAR. `provided` means Tomcat already supplies
the library (Servlet/JSP API), so it is used for compiling but not packaged.

**Q28. What is a WAR file?**
Web Application Archive — a ZIP containing compiled classes (`WEB-INF/classes`), libraries
(`WEB-INF/lib`), `web.xml`, JSPs and static files, deployable on Tomcat.

**Q29. Why Java 17?**
Long-Term Support version, stable and widely installed; offers modern APIs used here like `java.time`,
`List.of`, `String.isBlank`.

**Q30. Is your servlet thread-safe?**
Yes. Servlets have no per-request instance fields; the only field is a stateless DAO. Request data
lives in local variables and the request/session objects.

---

## Section D: DBMS and SQL questions

**Q1. What is a primary key? Which ones do you have?**
A column that uniquely identifies each row and cannot be NULL. `students.student_id` and
`admin.admin_id`, both `AUTO_INCREMENT`.

**Q2. What is a foreign key? Do you have one?**
A column referencing another table's primary key to enforce a relationship. **No** — my two tables are
independent; admins manage students in the application but the database doesn't record who changed
what. An audit table with foreign keys would be a future enhancement.

**Q3. What is a unique constraint? Why use it if Java already checks?**
It forbids duplicate values in a column. `roll_number` and `email` are UNIQUE. The Java check gives
friendly messages, but only the database can guarantee uniqueness if two requests arrive at the same
moment.

**Q4. What are CHECK constraints in your schema?**
`semester BETWEEN 1 AND 8`, `status IN ('Active','Inactive')`, `gender IN ('Male','Female','Other')`.
MySQL 8.0.16+ enforces them — a second line of defence.

**Q5. What is an index? Which did you create and why?**
A data structure (B-tree) that speeds up searches. Automatic indexes for the primary key and UNIQUE
columns, plus `department`, `semester`, `status` (filters) and `created_at` (sorting newest first).

**Q6. Can your search use an index?**
Partly: filters use indexes. The keyword search `LIKE '%text%'` starts with a wildcard, so it scans the
table — still about 40 ms with 10,000 rows. For millions of rows I'd use a FULLTEXT index.

**Q7. What is normalisation? Is your database normalised?**
Organising data to remove redundancy and anomalies. It's in 3NF: atomic values, single-column keys,
no transitive dependencies — for example age is not stored but calculated from `dob`. Department and
course are stored as text from a fixed list, a conscious simplification.

**Q8. Explain joins. Did you use any?**
A JOIN combines rows from tables using a related column — INNER, LEFT, RIGHT. I didn't need joins
because there is no relationship between my tables. Example if I had a `departments` table:
`SELECT s.full_name, d.name FROM students s JOIN departments d ON s.department_id = d.id;`

**Q9. What is CRUD? Show me each in your project.**
Create `INSERT INTO students …` (`addStudent`), Read `SELECT … WHERE student_id = ?` (`getStudentById`),
Update `UPDATE students SET … WHERE student_id = ?` (`updateStudent`), Delete
`DELETE FROM students WHERE student_id = ?` (`deleteStudent`).

**Q10. Write the query for total active students.**
`SELECT COUNT(*) FROM students WHERE status = 'Active';` (in code with `?`).

**Q11. Write the department-wise count query.**
`SELECT department, COUNT(*) AS total FROM students GROUP BY department ORDER BY total DESC;`

**Q12. COUNT(*) vs COUNT(DISTINCT department)?**
`COUNT(*)` counts rows; `COUNT(DISTINCT department)` counts different department values — used for the
"Departments" card.

**Q13. What is LIMIT and OFFSET?**
`LIMIT 10 OFFSET 20` returns 10 rows starting after the first 20 — page 3. Used for pagination.

**Q14. What is PreparedStatement and why use it?**
A pre-compiled SQL statement with `?` placeholders; values are sent separately, so they can never
change the SQL — this prevents SQL injection. It also handles quotes and data types.

**Q15. What is SQL injection? Demonstrate.**
An attack where input changes the SQL command. With concatenation, username `' OR '1'='1` would make
`WHERE username = '' OR '1'='1'` — true for every row. With PreparedStatement MySQL searches for a user
literally named `' OR '1'='1`. My functional tests TC-SEC-02 and TC-SEC-03 prove it.

**Q16. What is a ResultSet?**
The rows returned by a SELECT, read with a cursor: `while (resultSet.next()) { resultSet.getString("full_name"); }`.

**Q17. What is a transaction? Did you use transactions?**
A group of operations that succeed or fail together (ACID). Each of my DAO methods runs a single
statement in auto-commit mode, and InnoDB makes each statement atomic, so I didn't need manual
transactions. If deleting a student also had to write an audit row, I'd use `setAutoCommit(false)`,
`commit()` and `rollback()`.

**Q18. What are ACID properties?**
Atomicity (all or nothing), Consistency (constraints always hold), Isolation (concurrent transactions
don't interfere), Durability (committed data survives crashes). InnoDB provides them.

**Q19. Why VARCHAR for phone, not INT?**
Phone numbers are identifiers, not quantities; a number type would lose leading zeros/`+`, and
10-digit numbers can exceed INT's range.

**Q20. DELETE vs TRUNCATE vs DROP?**
DELETE removes chosen rows (with WHERE, can roll back); TRUNCATE removes all rows quickly and resets
AUTO_INCREMENT; DROP removes the whole table. I only use `DELETE … WHERE student_id = ?`.

**Q21. What does AUTO_INCREMENT do? Why might IDs skip numbers?**
MySQL assigns the next number automatically. InnoDB reserves values even for failed or ignored
inserts, so gaps are normal; IDs only need to be unique.

**Q22. What is the difference between DATE, DATETIME and TIMESTAMP?**
DATE stores only the date (used for `dob`). DATETIME and TIMESTAMP store date and time; TIMESTAMP is
stored in UTC and converted to the session time zone, and supports `DEFAULT CURRENT_TIMESTAMP` (used
for `created_at`).

**Q23. What is utf8mb4 and collation?**
utf8mb4 stores every Unicode character. The collation `utf8mb4_unicode_ci` defines comparison; `ci` =
case-insensitive, which makes the unique email check case-insensitive.

---

## Section E: Code-based viva questions

**Why did you create `Student.java`?**
To represent one student as an object that moves between layers — the servlet fills it from the form,
the DAO saves it and creates it from rows, and the JSP displays it. It also holds helper methods like
`getRegistrationId()` and `getAge()`. (`src/main/java/com/student/model/Student.java`)

**Why is `DBConnection.java` required?**
So connection details are defined once. Every DAO calls `DBConnection.getConnection()`. It reads
settings from environment variables or `db.properties`, so no password is in the code, and it gives a
clear error if the database isn't configured.

**What does `StudentDAO.java` do?**
All SQL for the `students` table: insert, select by ID, search with filters and paging, count,
duplicate checks, update, status change, delete and dashboard statistics — each with PreparedStatement
and try-with-resources.

**Why did you use PreparedStatement?**
SQL-injection safety, correct handling of quotes and types, and readable SQL constants.

**How does `LoginServlet` authenticate an administrator?**
It reads username and password, calls `adminDAO.findByUsername`, then
`PasswordUtil.checkPassword(password, admin.getPasswordHash())` — BCrypt re-hashes the typed password
with the stored salt and compares. On success it invalidates any old session, creates a new one with a
30-minute timeout, stores the admin (without hash) and a random CSRF token, and redirects to the
dashboard. Wrong user and wrong password give the same message.

**How does the dashboard calculate total registrations?**
`DashboardServlet` calls `studentDAO.countAllStudents()`, which runs `SELECT COUNT(*) FROM students`.
Active/inactive use `WHERE status = ?`, departments `COUNT(DISTINCT department)`, the bars `GROUP BY department`.

**How do you prevent duplicate registrations?**
Three layers: (1) the Java check `isRollNumberTaken`/`isEmailTaken` before insert; (2) the UNIQUE
constraints in MySQL; (3) Post-Redirect-Get plus a disabled submit button, so refresh or double-click
can't resubmit.

**How does your application validate an email?**
In the browser with `type="email"`, and on the server in `ValidationUtil.isValidEmail` with the regex
`^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$` and a 100-character limit. The
email is also converted to lower case.

**What happens if MySQL is unavailable?**
At start-up `AppContextListener` logs a warning. Pages that don't need the database still open. A
registration or dashboard request throws `SQLException`, which is logged and turned into the friendly
"Something went wrong" page — no stack trace. Login explains the likely cause, e.g. "Cannot reach the MySQL server … Make sure MySQL is running."
When MySQL starts again, the app works without restarting Tomcat, because every request opens a new
connection. (Tested: TC-SEC-11.)

**What happens if two users try registering with the same roll number at the same time?**
Both may pass the Java check, but MySQL's UNIQUE constraint lets only one INSERT succeed. The other gets
`SQLIntegrityConstraintViolationException`; `StudentDAO.toDuplicateException` turns it into
`DuplicateStudentException("rollNumber")`, and the second user sees "This roll number is already registered."

**How is the registration ID generated?**
`student_id` is AUTO_INCREMENT; `Student.getRegistrationId()` returns `"SRS-" + String.format("%05d", studentId)`.

**How does `AuthFilter` protect admin pages?**
It is mapped to `/admin/*`. It checks `session.getAttribute("loggedInAdmin")`; if missing it redirects
to login and does **not** call `chain.doFilter`, so the servlet never runs.

**What is the CSRF token and where is it checked?**
A random 32-byte value created at login (`CsrfUtil.generateToken`), stored in the session and put in
a hidden field of every admin form. `AuthFilter` rejects any POST whose token doesn't match with 403.

**How do you prevent XSS?**
All user data is printed with `<c:out>` or `fn:escapeXml`, which turn `<` into `&lt;`, so a `<script>`
typed by a user is shown as text. Tested in TC-SEC-07.

**Why are JSPs inside WEB-INF?**
So they can't be opened directly; every page goes through its servlet (and the filter for admin pages).

**How does search with optional filters work safely?**
`buildWhereClause` appends `AND department = ?` etc. only for chosen filters and collects values in a
list; `setParameters` binds them. The SQL text never contains user input.

**How does pagination work?**
`countStudents` gives the total; `totalPages = ceil(total / 10)`; the page number is clamped; the query
uses `LIMIT 10 OFFSET (page-1)*10`.

**Why does delete use POST and not a link?**
GET requests can be triggered by links, prefetching or crawlers and must not change data.
`DeleteStudentServlet` only implements `doPost`, so GET gets 405. A confirmation dialog appears first.

**Where is the admin password stored?**
Only as a BCrypt hash in `admin.password_hash`. The first admin is created on the first-time setup
page (`/setup`, which closes itself once an admin exists) or with the `AdminSetup` command-line tool;
there is no password in any file in the repository.

**What does `AppContextListener` do?**
On start-up it tests the database connection and logs the result; on shutdown it deregisters the JDBC
driver and stops MySQL's cleanup thread.

**Why `Locale.ENGLISH` in date formatting?**
So month names and AM/PM are the same on every computer regardless of its language settings.

---
## Section F: Practical demonstration

### F.1 Before the viva (checklist — do it 15 minutes early)
- [ ] MySQL is running (Windows: Services → MySQL80 → Running; macOS: `brew services list`).
- [ ] Tomcat is started and `<TOMCAT>/logs/localhost.<date>.log` shows "connected to MySQL".
- [ ] Browser tab 1: <http://localhost:8080/student-registration/> · tab 2: `/login`.
- [ ] MySQL Workbench is connected and has a query tab with:
      `USE student_registration_db; SELECT * FROM students ORDER BY student_id DESC LIMIT 5;`
- [ ] You know the admin username and password (test it once).
- [ ] IDE open with `RegisterServlet`, `StudentDAO`, `DBConnection`, `AuthFilter`, `schema.sql`.
- [ ] `docs/diagrams/exports/architecture.png` and `mvc-architecture.png` ready to show.
- [ ] Choose a **new** roll number and email for the live registration (not used before), e.g.
      roll `26CSE050`, email `riya.sen@example.com`. If you rehearsed with them, delete that record first.
- [ ] Browser zoom at 100–110 %, notifications off, other tabs closed.

### F.2 Demonstration script (about 10 minutes)

| Step | What you do | What you say |
|---|---|---|
| 1. Introduce | Show the home page | Give the 30-second introduction (A.1). "There are two users: students and administrators." |
| 2. Registration form | Click **Register as a Student** | "This is the public form with ten fields in three sections. Required fields are marked." |
| 3. Validation | Click **Submit** on the empty form; then type `abc` as email and `12345` as phone | "The browser highlights errors instantly — but this is only for convenience; the same rules run again on the server in `ValidationUtil`." |
| 4. Duplicate check (optional) | Enter roll number `23CSE001` (already in sample data) with valid other fields, submit | "The server says the roll number is already registered — duplicates are rejected by a Java check and by a UNIQUE constraint in MySQL." |
| 5. Register | Fill in: Riya Sen · DOB 2005-04-12 · Female · `26cse050` · Semester 1 · Computer Science and Engineering · B.E. · `riya.sen@example.com` · 9876512345 · "21 Lake Road, Kolkata" → Submit | "Notice the roll number becomes capital letters." |
| 6. Registration ID | Point at `SRS-000NN` | "MySQL generated the auto-increment ID; Java formats it as SRS plus five digits. If I refresh, it doesn't register again — Post-Redirect-Get." (Press F5 → back to the form.) |
| 7. Database | Switch to Workbench, run the SELECT | "Here is the new row: roll number in capitals, email in lower case, status Active and `created_at` filled in by MySQL." |
| 8. Login | Tab 2 → enter a wrong password first, then the right one | "Wrong password gives a generic message. The password is checked against a BCrypt hash." Optionally run `SELECT username, password_hash FROM admin;` to show the hash. |
| 9. Dashboard | Show the four cards, recent registrations, department bars | "Every number is a live SQL query — COUNT, COUNT DISTINCT, GROUP BY. Riya is at the top of recent registrations and the total increased by one." |
| 10. Student management | Click **All Students**; search `Riya` | "Search looks in name, roll number and email with LIKE and PreparedStatement." |
| 11. Edit | Open Riya's profile → **Edit** → change semester to 2 → Save | "The form is pre-filled; on save the servlet validates again and runs an UPDATE. The green message is a one-time flash message." Show the row in Workbench again. |
| 12. Status | Click **Mark Inactive** | "Status is changed with a POST request; the Inactive card on the dashboard will increase." |
| 13. Filters | All Students → Department = Computer Science and Engineering, Semester = 3 → Apply; then Status = Inactive | "Filters can be combined; the URL contains the filter, so it can be bookmarked. Ten students per page." |
| 14. Delete | Delete Riya (the test record) → show the confirmation dialog → Cancel → Delete again → confirm | "Deleting needs confirmation and is only possible with POST plus a security token. The row is gone from MySQL." Refresh Workbench. |
| 15. Extras (if time) | Export CSV; open a profile and click Print; resize the browser to phone width | "CSV opens in Excel; print hides the menus; the layout is responsive." |
| 16. Security (if asked) | Logout → press Back / open `/admin/dashboard` | "After logout the session is invalidated; admin pages redirect to login and aren't cached." |
| 17. Architecture | Show `architecture.png` / `mvc-architecture.png` | Explain B.1 in your own words. |
| 18. Source code | Open in this order: `DBConnection.getConnection` → `RegisterServlet.doPost` → `StudentDAO.addStudent` → `AuthFilter.doFilter` → `schema.sql` | Follow the registration flow: "form → servlet → validation → DAO → PreparedStatement → MySQL". |

### F.3 If something goes wrong during the demo
| Problem | Quick fix |
|---|---|
| Page says "Something went wrong" | MySQL stopped — start it; no need to restart Tomcat. Turn it into a feature: "this shows graceful error handling". |
| 404 on every page | Tomcat not running or WAR not deployed — run `startup.bat`/`startup.sh`, wait 10 s. |
| "Invalid username or password" with the right password | Caps Lock; or reset with `mvnw.cmd compile exec:java` / `./mvnw compile exec:java` (answer `yes`). |
| "Roll number already registered" for your demo student | You registered it in rehearsal — use another roll number or delete it first. |
| Workbench doesn't show the new row | Click the refresh icon or re-run the SELECT. |

---

## Section G: Difficult viva questions

**What would happen if the database connection failed?**
`DriverManager.getConnection` throws `SQLException`. The DAO doesn't catch it; the servlet logs it and
throws `ServletException`; `web.xml` shows the friendly 500 page. No stack trace reaches the user, no
partial data is saved because nothing was executed, and when MySQL is back the next request works.
I tested this by stopping MySQL (TC-SEC-11).

**How do you prevent SQL injection?**
Every query uses PreparedStatement with `?` placeholders. Even my dynamic search builds only the SQL
*structure* with placeholders and binds values separately. Additionally, inputs are validated (e.g. roll
numbers allow only letters, digits, `/` and `-`), and the app's MySQL user cannot drop tables. Tests
TC-SEC-01/02/03 try injection strings and they are treated as plain text.

**How do you handle duplicate submissions?**
Post/Redirect/Get (refresh can't resubmit), the submit button is disabled after the first click
(tested: two quick clicks send one request), and the UNIQUE constraints make even a resubmission fail
with "already registered".

**How would the system behave with 10,000 students?**
I measured it: with 10,017 rows the dashboard took 38 ms, the list 18 ms, page 500 34 ms, keyword search
about 41 ms, combined filters 17 ms and the CSV export of all rows 0.35 s. Pages show only 10 rows
(LIMIT/OFFSET), the filtered and sorted columns are indexed, and `EXPLAIN` confirmed index use. The
weakest point is `LIKE '%text%'` which scans the table; with millions of rows I would add a FULLTEXT
index and a connection pool.

**Why did you not use Spring Boot?**
The goal was to learn and show core Java web development: how HTTP requests reach servlets, sessions,
filters, JDBC and SQL. Spring Boot automates these, which is great in industry but would hide what
this subject is assessing. Having built it with servlets, I understand what Spring does underneath.

**Why JSP and Servlets?**
They are the foundation of Java web development (Spring MVC itself runs on the Servlet API), they need
only Tomcat, and the MVC separation is very clear: servlets control, JSPs display.

**Why is MySQL suitable here?**
The data is structured and relational, it needs uniqueness and validity guarantees, and reports are
simple SQL aggregates. MySQL is free, reliable (InnoDB, ACID), widely used and supported by an official
JDBC driver.

**Why not store the password encrypted instead of hashed?**
Encryption can be reversed with a key; if the key leaks, all passwords leak. A hash is one-way — even I
can't recover a password. BCrypt adds a random salt and is slow on purpose, so brute-forcing is impractical.

**Why do you validate in both JavaScript and Java?**
JavaScript gives instant feedback, but users can disable it or send requests with tools like curl. The
server is the only place I fully control, so Java validation is the real protection — my functional
tests send invalid data directly with curl and the server rejects it.

**What are the limitations?**
Single admin role, no student login, no email verification, no lockout after failed logins, no
connection pool, departments fixed in code, hard delete without audit log, HTTP on localhost only.

**What would you improve in the future?**
Student portal, email verification, role-based access, departments/courses tables with foreign keys,
audit log and soft delete, login rate limiting, connection pooling, charts and PDF reports, HTTPS
deployment.

**How would you scale the application?**
Step by step: connection pool; FULLTEXT search index; caching dashboard counts for a few seconds;
running several Tomcat instances behind a load balancer (sessions would need to be shared — sticky
sessions or a session store); a MySQL read replica for reports; HTTPS via a reverse proxy.

**Is your application safe against CSRF? Explain.**
Yes: each session gets a random 32-byte token; every admin form includes it; `AuthFilter` rejects POSTs
without the matching token (403). The session cookie is also `SameSite=Lax`, so browsers don't send it
on cross-site POSTs. Tested in TC-SEC-06.

**What if an admin opens the delete URL in the address bar?**
That's a GET request; `DeleteStudentServlet` has no `doGet`, so the inherited `HttpServlet.doGet`
returns 405 and nothing is deleted.

**Why is there no foreign key between `admin` and `students`?**
The requirements don't need to record which admin created or changed a student. Adding a fake
relationship would complicate the schema without purpose. If auditing were required, I'd add an audit
table with foreign keys to both.

**Why is the roll number not the primary key?**
Roll numbers are typed by people and may need correcting; primary keys should never change. A generated
`student_id` stays stable, while UNIQUE still prevents duplicate roll numbers.

**Why did you open a new database connection for every operation?**
It is simple and safe with try-with-resources, and fast enough (single-digit milliseconds locally). A
connection pool would reuse connections — it's the first optimisation for high traffic.

**What happens if the session expires while the admin is editing?**
On submit, `AuthFilter` finds no session and redirects to the login page; nothing is saved. The admin
logs in again and repeats the edit.

**How did you test your project?**
34 JUnit unit tests, 15 integration tests against real MySQL (constraints, CRUD, SQL injection), a script
with 69 automated HTTP checks, and manual/browser tests for UI, database outage and performance. 81 of
89 documented test cases are executed and passed; 8 that need my own laptop or a 30-minute wait are
marked pending.

---

## Section H: Quick revision

### H.1 The 30 most important questions (one-line answers)
1. **What is your project?** A Java web app for online student registration with a secure admin panel.
2. **Tech stack?** Java 17, Servlets, JSP/JSTL, JDBC, MySQL 8, Tomcat 9, Maven, Bootstrap 5.
3. **Architecture?** MVC + DAO, three-tier (browser, Tomcat, MySQL).
4. **Model classes?** `Student`, `Admin`, `StudentFilter`.
5. **Controllers?** 13 servlets, e.g. `RegisterServlet`, `LoginServlet`, `StudentListServlet`.
6. **Views?** JSPs in `WEB-INF/views` with JSTL and EL.
7. **DAO?** `StudentDAO`, `AdminDAO` — all SQL lives there.
8. **How is the DB connected?** `DBConnection` → `DriverManager.getConnection(url, user, password)`.
9. **Where is the password for MySQL?** In `db.properties` (git-ignored) or environment variables.
10. **How is SQL injection prevented?** PreparedStatement with `?` placeholders.
11. **How are admin passwords stored?** BCrypt hash (cost 12) in `password_hash`.
12. **How are admin pages protected?** `AuthFilter` on `/admin/*` checks the session.
13. **How is logout done?** `session.invalidate()` in `LogoutServlet` (POST + CSRF token).
14. **Session timeout?** 30 minutes (`web.xml` + `setMaxInactiveInterval`).
15. **How is the registration ID made?** AUTO_INCREMENT `student_id` → `SRS-%05d`.
16. **How are duplicates prevented?** Java pre-check + UNIQUE constraints + PRG + disabled button.
17. **What is PRG?** Post/Redirect/Get — redirect after POST so refresh doesn't resubmit.
18. **Validation where?** Browser (HTML5 + `app.js`) for convenience; server (`ValidationUtil`) for security.
19. **How is XSS prevented?** `<c:out>` / `fn:escapeXml` escape user data.
20. **What is CSRF and your protection?** Forged cross-site requests; per-session token + SameSite cookie.
21. **Dashboard queries?** COUNT, COUNT with WHERE, COUNT DISTINCT, GROUP BY, ORDER BY … LIMIT.
22. **Search query?** `LIKE ?` on name/roll/email + optional `department = ?`, `semester = ?`, `status = ?`.
23. **Pagination?** `LIMIT 10 OFFSET ?`, page count from `COUNT(*)`.
24. **Delete safety?** POST only, CSRF token, confirmation dialog, `WHERE student_id = ?`.
25. **CSV export?** `ExportStudentsServlet` writes `text/csv` with `Content-Disposition: attachment`.
26. **Error handling?** Log + `ServletException` → `error.jsp`; no stack traces.
27. **First admin?** First-time setup page `/setup` (only while no admin exists) or the `AdminSetup` tool.
28. **Tests?** 34 unit, 15 integration, 69 HTTP checks — all pass.
29. **Performance?** < 50 ms per page with 10,000 students.
30. **Limitations?** One admin role, no student login, no email verification, no lockout, no pool.

### H.2 One-line definitions
| Term | Definition |
|---|---|
| Servlet | Java class that handles HTTP requests inside a servlet container |
| JSP | HTML page with tags, compiled into a servlet, used as the view |
| JSTL | Standard tag library for JSP (`c:forEach`, `c:if`, `c:out`) |
| EL | Expression Language `${…}` to read data in JSP |
| Tomcat | Servlet container / web server that runs the WAR |
| WAR | Web Application Archive — the deployable package |
| Maven | Build and dependency management tool |
| JDBC | Java API for relational databases |
| Driver | Library implementing JDBC for a specific database |
| PreparedStatement | Pre-compiled SQL with bound parameters |
| ResultSet | Cursor over the rows returned by a query |
| DAO | Class that encapsulates database access |
| MVC | Model–View–Controller separation |
| HttpSession | Server-side per-user storage identified by a cookie |
| Cookie | Small data stored by the browser and sent with each request |
| Filter | Component that intercepts requests before servlets |
| Listener | Component notified of application events (start/stop) |
| BCrypt | Slow, salted, one-way password hashing |
| Salt | Random data added to a password before hashing |
| CSRF | Attack making a logged-in user's browser send unwanted requests |
| XSS | Attack injecting scripts into pages viewed by others |
| SQL injection | Attack altering SQL through user input |
| Primary key | Unique, non-null row identifier |
| Unique constraint | Rule forbidding duplicate values in a column |
| Index | Structure that speeds up searching and sorting |
| Normalisation | Organising tables to avoid redundancy |
| Transaction | Group of operations that commit or roll back together |
| CRUD | Create, Read, Update, Delete |
| PRG | Post/Redirect/Get pattern |
| Context path | URL prefix of the app (`/student-registration`) |

### H.3 SQL revision
```sql
-- create
INSERT INTO students (roll_number, full_name, email, phone, gender, dob, department, course, semester, address, status)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
-- read one
SELECT * FROM students WHERE student_id = ?;
-- search + filters + paging
SELECT * FROM students WHERE (full_name LIKE ? OR roll_number LIKE ? OR email LIKE ?) AND department = ?
ORDER BY created_at DESC LIMIT 10 OFFSET 0;
-- update / delete
UPDATE students SET semester = ?, status = ? WHERE student_id = ?;
DELETE FROM students WHERE student_id = ?;
-- statistics
SELECT COUNT(*) FROM students;
SELECT COUNT(*) FROM students WHERE status = 'Active';
SELECT COUNT(DISTINCT department) FROM students;
SELECT department, COUNT(*) AS total FROM students GROUP BY department ORDER BY total DESC;
-- duplicates
SELECT COUNT(*) FROM students WHERE roll_number = ? AND student_id <> ?;
```

### H.4 Architecture revision (draw this on the board)
```
Browser ──HTTP──> Tomcat 9
                   ├─ AuthFilter (/admin/*)  : session + CSRF check
                   ├─ Servlet (Controller)   : read → validate → call DAO → forward/redirect
                   │     └─ ValidationUtil, PasswordUtil, CsrfUtil
                   ├─ DAO (StudentDAO/AdminDAO) : PreparedStatement SQL
                   │     └─ DBConnection ──JDBC──> MySQL 8 (students, admin)
                   ├─ Model (Student, Admin, StudentFilter)
                   └─ View (JSP + JSTL) ──HTML──> Browser
```

### H.5 Common mistakes to avoid during the demonstration
- Don't start the viva without checking that MySQL and Tomcat are running.
- Don't register a student with a roll number you already used in practice.
- Don't say "JavaScript validation makes it secure" — server validation does.
- Don't call the password "encrypted" — it is **hashed** with BCrypt.
- Don't say there is a foreign key — there isn't; explain why.
- Don't claim features that don't exist (no email sending, no student login, no roles).
- Don't open JSPs directly by URL (they're in WEB-INF) — use the app's links.
- Don't delete sample students during the demo — delete only your own test record.
- Don't panic on an error page — explain it as graceful error handling, fix, continue.
- Keep answers short first; give details when the examiner asks "how?" or "why?".
