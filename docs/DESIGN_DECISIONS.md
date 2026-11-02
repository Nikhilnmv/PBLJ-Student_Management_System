# Design Decisions

Each important decision with its **reason**, its **advantages** and its **trade-offs** (what we give
up). Being able to explain trade-offs is often what examiners look for.

---

## DD-01 Java Servlets and JSP (instead of Spring Boot)
- **Decision:** Build the web layer directly on the Servlet API and JSP.
- **Why:** The project's purpose is to demonstrate core Java web development. With servlets you can
  see exactly how a request arrives (`doGet`/`doPost`), how parameters are read, how sessions and
  redirects work. Spring Boot hides these behind annotations and auto-configuration.
- **Advantages:** No "magic"; every line is explainable; small WAR (≈2.8 MB); fast to start; the
  concepts transfer to every Java web framework.
- **Trade-offs:** More manual code (e.g. reading every form field, a hand-written filter), no
  dependency injection, no built-in validation framework. Acceptable for this size.

## DD-02 MySQL (relational database)
- **Decision:** Store all data in MySQL 8.
- **Why:** Student records are structured and must be unique and consistent — a perfect fit for
  tables with constraints. MySQL is free, widely taught, and has Workbench for demonstrations.
- **Advantages:** UNIQUE/CHECK constraints protect data even if application code has a bug; SQL
  aggregate functions compute dashboard statistics; transactions and durability.
- **Trade-offs:** Requires installing and running a database server (versus a file or H2 embedded DB).

## DD-03 MVC architecture
- **Decision:** Separate Model (`model`), View (JSP) and Controller (servlets).
- **Why:** Each part has one job, which makes the code easier to read, test, change and explain.
- **Advantages:** JSPs contain no Java/SQL; servlets contain no HTML; models are plain objects.
- **Trade-offs:** More files than putting everything in a few JSPs — but each file is small.

## DD-04 DAO pattern
- **Decision:** All SQL lives in `StudentDAO` and `AdminDAO`.
- **Why:** Servlets ask for *what* they need (`deleteStudent(id)`), not *how*.
- **Advantages:** One place to review SQL for injection; reuse (the same `searchStudents` serves the
  list and the CSV export); DAOs can be integration-tested on their own.
- **Trade-offs:** None significant. We did **not** add DAO interfaces (`StudentDAO` + `StudentDAOImpl`)
  because there is only one implementation — that would be abstraction without benefit.

## DD-05 Apache Tomcat 9
- **Decision:** Target Tomcat 9 (Servlet 4.0, `javax.servlet`).
- **Why:** The requirement is `javax.servlet`. Tomcat 10+ uses `jakarta.servlet` and would not load
  these classes. Tomcat 9 is still maintained, lightweight and standard in colleges.
- **Trade-offs:** Not the newest Jakarta EE namespace; migrating later means renaming imports
  (`javax.servlet` → `jakarta.servlet`) and moving to Tomcat 10.

## DD-06 Plain JDBC instead of an ORM (Hibernate/JPA)
- **Decision:** Write SQL by hand with `PreparedStatement`.
- **Why:** The project must demonstrate JDBC and SQL directly. An ORM generates SQL automatically,
  hiding exactly the concepts the examiner wants to see.
- **Advantages:** Full control and visibility of every query; no extra configuration; easy to explain.
- **Trade-offs:** Mapping rows to objects (`mapRowToStudent`) is manual; more boilerplate
  (connections, statements). Try-with-resources keeps it safe and short.

## DD-07 Monolithic application
- **Decision:** One WAR, one database, one server.
- **Why:** Simplest architecture that meets all requirements; easy to run on one laptop.
- **Advantages:** Simple build, deployment, debugging and demonstration.
- **Trade-offs:** Cannot scale parts independently. For a college registration system with
  thousands of students this is not a problem (see performance results in TEST_RESULTS.md).

## DD-08 One servlet per action (small controllers)
- **Decision:** 12 small servlets (`AddStudentServlet`, `DeleteStudentServlet` …) instead of one big
  servlet with `?action=add|edit|delete`.
- **Why:** Each class does one thing and is short; the URL tells you which class to open; "POST only"
  is enforced simply by not writing `doGet`.
- **Trade-offs:** More classes. Shared logic is kept in `StudentRequestHelper` to avoid duplication.

## DD-09 One JSP for Add and Edit; one fragment for all student forms
- **Decision:** `student-form.jsp` handles both modes; `student-form-fields.jspf` is shared with the
  public registration page.
- **Why:** The fields and validation messages are identical — one place to change.

## DD-10 Two-layer duplicate protection
- **Decision:** Check duplicates in Java first, **and** rely on UNIQUE constraints in MySQL.
- **Why:** The Java check gives friendly, field-specific messages for both fields at once; the
  database constraint is the only thing that is safe when two requests arrive at the same moment.

## DD-11 Surrogate key + derived registration ID
- **Decision:** `student_id INT AUTO_INCREMENT` as primary key; display `SRS-` + 5 digits.
- **Why:** Roll numbers may be corrected by an admin; a generated key never changes, so links stay
  valid. The display ID is calculated, not stored, so it can never be inconsistent.

## DD-12 BCrypt for passwords
- **Decision:** jBCrypt with cost 12.
- **Why:** BCrypt is salted and deliberately slow — the industry-standard choice for passwords.
  A plain hash like SHA-256 is too fast and vulnerable to brute force.
- **Trade-offs:** Login takes ~0.2–0.3 s of CPU time — invisible to users, expensive for attackers.

## DD-13 Session authentication with a Filter + CSRF token
- **Decision:** `HttpSession` holds the logged-in admin; `AuthFilter` guards `/admin/*`; every admin
  POST carries a random token; cookie is HttpOnly and SameSite=Lax.
- **Why:** Standard, simple and explainable protection without a security framework.
- **Trade-offs:** One role only; no "remember me"; no lockout after failed logins (documented limitation).

## DD-14 Command-line admin setup
- **Decision:** `AdminSetup` creates the first admin; no default admin in SQL.
- **Why:** A default password in `schema.sql` would be public on GitHub and often never changed.
- **Trade-off:** One extra setup step (documented in SETUP_AND_EXECUTION.md §5).

## DD-15 Configuration outside the code
- **Decision:** Database settings from environment variables or an ignored `db.properties`.
- **Why:** Different laptops have different passwords; secrets must not be committed.

## DD-16 No connection pool
- **Decision:** `DriverManager.getConnection()` for each DAO call.
- **Why:** Simplest correct approach; each operation is short; measured response times are 7–42 ms
  even with 10,000 students.
- **Trade-off:** Opening connections costs a few milliseconds; a pool (Tomcat JDBC / HikariCP) would
  be the first step for many simultaneous users.

## DD-17 Fixed department and course lists in code
- **Decision:** `AppConstants.DEPARTMENTS` / `COURSES` instead of database tables.
- **Why:** Keeps the schema at the two required tables and the code simple; the same list feeds the
  drop-downs and the validation.
- **Trade-off:** Adding a department needs a code change. Future scope: `departments` and
  `courses` tables with foreign keys and an admin screen.

## DD-18 Post/Redirect/Get and flash messages
- **Decision:** Every successful POST ends with a redirect; messages travel in the session once.
- **Why:** Refreshing the page never repeats an insert/delete; no "resubmit form?" warnings.

## DD-19 Bootstrap bundled locally
- **Decision:** Bootstrap, Bootstrap Icons and the Inter font are stored in `assets/vendor`.
- **Why:** The application must work during a viva without internet access.
- **Trade-off:** About 0.8 MB of static files in the repository.

## DD-20 Testing strategy
- **Decision:** JUnit for pure Java logic, opt-in JUnit integration tests against real MySQL, and a
  curl script for end-to-end HTTP tests. No mocking framework, no Selenium in the repository.
- **Why:** Tests real behaviour with the fewest tools; easy to run and explain.
- **Trade-off:** Browser-only behaviour (JavaScript dialogs, layout) is checked manually.
