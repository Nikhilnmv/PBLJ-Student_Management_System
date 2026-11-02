# Code Walkthrough

A file-by-file explanation of the source code, followed by step-by-step traces of every important
flow. Keep the code open while reading. Paths are relative to `src/main/java/com/student/` for Java
and `src/main/webapp/` for web files.

**Contents**
1. [Model](#1-model-package-comstudentmodel)
2. [DAO](#2-dao-package-comstudentdao)
3. [Utilities](#3-utility-package-comstudentutil)
4. [Filter and listener](#4-filter-and-listener)
5. [Servlets (controllers)](#5-servlets-package-comstudentservlet)
6. [Views (JSP)](#6-views-jsp)
7. [Static assets](#7-static-assets)
8. [Configuration](#8-configuration-files)
9. [Flow traces](#9-flow-traces)

---

## 1. Model (package `com.student.model`)

### `Student.java`
- **What:** one student = one row of the `students` table.
- **Why:** gives the rest of the program a single object to pass around instead of 13 separate values.
- **Fields:** `studentId, rollNumber, fullName, email, phone, gender, dob (LocalDate), department,
  course, semester (int), address, status, createdAt (LocalDateTime)` — all `private`.
- **Constants:** `STATUS_ACTIVE = "Active"`, `STATUS_INACTIVE = "Inactive"`, `REGISTRATION_ID_PREFIX = "SRS-"`.
- **Constructors:** `Student()` sets `status = "Active"`; `Student(rollNumber, …, address)` calls
  `this()` first and then fills the form fields (constructor chaining).
- **Helper methods used by JSPs:**
  - `getRegistrationId()` → `"SRS-" + String.format("%05d", studentId)`.
  - `getDobFormatted()`, `getCreatedAtFormatted()`, `getCreatedDateFormatted()` → readable dates (English month names).
  - `getDobIso()` → `yyyy-MM-dd`, the format an `<input type="date">` needs.
  - `getAge()` → `Period.between(dob, LocalDate.now()).getYears()`.
  - `isActive()` → used in JSP as `${s.active}` to pick the badge colour.
  - `getInitials()` → "Aarav Mehta" → "AM" for the round avatar.
- **Used by:** every servlet, `StudentDAO` (creates Student objects from rows), JSPs (read with EL).
- **Concepts:** class, object, encapsulation, constructors, `this()`, `Serializable` interface,
  `java.time` API, `static final` constants, `toString()` override.

### `Admin.java`
- One row of the `admin` table: `adminId`, `username`, `passwordHash`. Implements `Serializable`
  because it is stored in the `HttpSession`. `LoginServlet` stores a copy **without** the hash.

### `StudentFilter.java`
- Holds the search box and drop-down values of the student list: `keyword`, `department`,
  `semester` (`Integer`, so it can be `null` = "all"), `status`.
- `isActive()` returns `true` if any option is chosen (the JSP then shows "Search Results" and a
  "clear filters" button).
- **Why:** passing one object is clearer than passing four parameters to every DAO method.

---

## 2. DAO (package `com.student.dao`)

### `StudentDAO.java` — the only class with SQL for students
- **SQL constants** at the top (`INSERT_STUDENT_SQL`, `SELECT_BY_ID_SQL`, `UPDATE_STUDENT_SQL`,
  `DELETE_STUDENT_SQL`, `UPDATE_STATUS_SQL`, the `COUNT…` queries …). Keeping SQL as named
  constants makes it easy to read and review.
- **Public methods** (what servlets call):

| Method | SQL | Returns |
|---|---|---|
| `addStudent(Student)` | `INSERT … VALUES (?, …)` with generated keys | new `student_id` |
| `getStudentById(int)` | `SELECT … WHERE student_id = ?` | `Student` or `null` |
| `searchStudents(filter, offset, limit)` | `SELECT … WHERE … ORDER BY … LIMIT ? OFFSET ?` | one page |
| `searchStudents(filter)` | same without `LIMIT` (CSV export) | all matches |
| `countStudents(filter)` | `SELECT COUNT(*) … WHERE …` | number of matches |
| `isRollNumberTaken(roll, excludeId)` | `SELECT COUNT(*) … WHERE roll_number = ? AND student_id <> ?` | boolean |
| `isEmailTaken(email, excludeId)` | same for email | boolean |
| `updateStudent(Student)` | `UPDATE students SET … WHERE student_id = ?` | `true` if a row changed |
| `updateStatus(id, status)` | `UPDATE students SET status = ? WHERE student_id = ?` | boolean |
| `deleteStudent(id)` | `DELETE FROM students WHERE student_id = ?` | boolean |
| `countAllStudents()` | `SELECT COUNT(*) FROM students` | int |
| `countStudentsByStatus(status)` | `… WHERE status = ?` | int |
| `countDepartments()` | `SELECT COUNT(DISTINCT department) …` | int |
| `getRecentStudents(limit)` | `… ORDER BY created_at DESC, student_id DESC LIMIT ?` | list |
| `getDepartmentCounts()` | `SELECT department, COUNT(*) AS total … GROUP BY department …` | `Map<String,Integer>` |

- **Private helpers:**
  - `buildWhereClause(filter, parameters)` — builds `WHERE 1 = 1 AND …` with `?` and collects the values.
  - `escapeLikeWildcards(text)` — makes `%` and `_` in the search text literal.
  - `queryStudents(sql, parameters)` — runs a SELECT and turns every row into a `Student`.
  - `setParameters(statement, parameters)` — binds `Integer` with `setInt`, everything else with `setString`.
  - `setStudentParameters(statement, student)` — fills placeholders 1–11 (shared by INSERT and UPDATE).
  - `mapRowToStudent(resultSet)` — reads each column of the current row into a new `Student`.
  - `countMatches(sql, value, excludeId)` — shared code for the two "is taken" checks.
  - `toDuplicateException(e)` — reads the constraint name in MySQL's error message
    (`uk_students_roll_number` or `uk_students_email`) and creates a `DuplicateStudentException`.
- **Pattern used in every method:**
  ```java
  try (Connection connection = DBConnection.getConnection();
       PreparedStatement statement = connection.prepareStatement(SQL)) {
      statement.setInt(1, studentId);           // bind values
      try (ResultSet resultSet = statement.executeQuery()) { ... }
  }                                             // everything closed automatically
  ```
- **Concepts:** DAO pattern, JDBC, PreparedStatement, ResultSet, try-with-resources, method
  overloading (`searchStudents`), collections (`List`, `ArrayList`, `Map`, `LinkedHashMap`),
  exception translation.

### `AdminDAO.java`
- `findByUsername(username)` → `SELECT admin_id, username, password_hash FROM admin WHERE username = ?`
- `createAdmin(username, passwordHash)` → `INSERT INTO admin (username, password_hash) VALUES (?, ?)`
- `updatePassword(username, passwordHash)` → `UPDATE admin SET password_hash = ? WHERE username = ?`
- Used by `LoginServlet` (find) and `AdminSetup` (create / reset).

### `DuplicateStudentException.java`
- A **custom checked exception**: `extends Exception`, adds `field` ("rollNumber" or "email").
- Thrown by `addStudent` / `updateStudent`; caught by the servlets, which show the message under that field.

---

## 3. Utility package (`com.student.util`)

### `DBConnection.java`
- **What:** opens JDBC connections. **Why:** URL, user and password are configured once.
- **Static block** (runs once when the class is loaded): `Class.forName("com.mysql.cj.jdbc.Driver")`
  registers the MySQL driver; then the settings are read with `readSetting(envName, propertyName, file)`:
  environment variable first, otherwise `db.properties` from the classpath.
- `getConnection()` → `DriverManager.getConnection(URL, USER, PASSWORD)`; throws a clear
  `SQLException("Database is not configured…")` if no settings were found.
- `explain(SQLException e)` → turns a database error into a beginner-friendly hint using MySQL's
  error code (1045 wrong password, 1049 no database, 1146 no tables) or SQL state `08…` (MySQL not
  running). Shown on the login and setup pages.
- Private constructor: nobody needs a `DBConnection` object; all members are `static`.

### `ValidationUtil.java`
- `validateStudent(Student)` returns `Map<String, String>` (field → message), empty if valid.
  Order is kept with `LinkedHashMap`.
- Individual checks (public so they can be unit tested): `isValidName`, `isValidRollNumber`,
  `isValidEmail`, `isValidPhone`, `isValidGender`, `isValidDateOfBirth`, `isValidDepartment`,
  `isValidCourse`, `isValidSemester`, `isValidAddress`, `isValidStatus`.
- **Regular expressions** are compiled once as `static final Pattern`:
  - name `^\p{L}[\p{L} .'-]{1,99}$` (letters of any language),
  - roll number `^[A-Z0-9][A-Z0-9/-]{2,19}$`,
  - email `^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$`,
  - phone `^[6-9][0-9]{9}$`.
- Safe converters: `parseInteger("five")` → `null` (catches `NumberFormatException`),
  `parseDate("2005-02-30")` → `null` (catches `DateTimeParseException`), `normalizeSpaces(...)`.

### `AppConstants.java`
- `DEPARTMENTS` (7), `COURSES` (6), `GENDERS`, `STATUSES`, `SEMESTERS` (1–8), age limits 15–60.
  Created with `List.of(...)` (unmodifiable). The same lists feed the drop-downs **and** validation.

### `PasswordUtil.java`
- `hashPassword(plain)` → `BCrypt.hashpw(plain, BCrypt.gensalt(12))` (random salt, cost 12).
- `checkPassword(plain, storedHash)` → `BCrypt.checkpw(...)`; returns `false` (never throws) for
  `null` or invalid hashes.
- `isStrongPassword(pw)` → 8–72 characters, at least one letter and one digit.

### `CsrfUtil.java`
- `generateToken()` → 32 bytes from `SecureRandom`, Base64 → 43-character token.
- `isValid(session, submittedToken)` → compares with the session's token using
  `MessageDigest.isEqual` (constant-time comparison).

### `CsvUtil.java`
- `escape(value)` → prefixes `'` if the value starts with `= + - @` (formula injection), and wraps in
  quotes if it contains a comma, quote or line break (doubling inner quotes).
- `toCsvLine(String... values)` → one line, values separated by commas (varargs).

### `AdminSetup.java`
- A standalone program with `public static void main(String[] args)` — **not** part of the website.
- Asks for a username, checks if it exists (`adminDAO.findByUsername`), asks for the password twice,
  checks strength, hashes it, then `createAdmin` or `updatePassword`.
- Uses `System.console().readPassword()` so the password is hidden in a real terminal; falls back to
  a `BufferedReader` in IDE consoles. Clears the password array with `Arrays.fill(password, ' ')`.
- Run with `./mvnw compile exec:java` (macOS) or `mvnw.cmd compile exec:java` (Windows).
- The browser-based `/setup` page (`SetupServlet`) does the same for the **first** admin; this tool is
  still useful to **reset a forgotten password**.

---

## 4. Filter and listener

### `filter/AuthFilter.java`
- `@WebFilter("/admin/*")` + `implements Filter` → Tomcat runs `doFilter` **before** every admin servlet.
- Steps in `doFilter`:
  1. `request.getSession(false)` — get the existing session, don't create one.
  2. No `loggedInAdmin` attribute → `response.sendRedirect(contextPath + "/login?required=true")` and stop.
  3. POST without a valid CSRF token → `response.sendError(403)` and stop.
  4. Add `Cache-Control: no-cache, no-store, must-revalidate`.
  5. `chain.doFilter(request, response)` → continue to the servlet.
- **Why a filter?** One security check for 9 admin URLs instead of copying it into each servlet.

### `listener/AppContextListener.java`
- `@WebListener` + `implements ServletContextListener`.
- `contextInitialized` → opens a test connection and logs
  `connected to MySQL (8.0.xx)` or a WARNING.
- `contextDestroyed` → deregisters the JDBC driver and stops Connector/J's cleanup thread
  (avoids memory-leak warnings when Tomcat stops or redeploys).

---

## 5. Servlets (package `com.student.servlet`)

All servlets `extends HttpServlet`, declare `serialVersionUID`, keep a DAO object as a field, and
catch `SQLException` → log it → `throw new ServletException(...)` so `web.xml` shows the friendly
error page.

### `StudentRequestHelper.java` (package-private helper)
Shared by Register, Add, Edit, List and Export servlets:
- `readStudentFromRequest(request)` — form fields → `Student` (trim, upper-case roll, lower-case email,
  parse date/semester; semester `0` if missing so validation fails).
- `validateStudent(student, dao, excludeId)` — `ValidationUtil` rules + duplicate checks.
- `setFormOptions(request)` — puts `departments, courses, genders, semesters, statuses, minDob, maxDob`
  into the request for the form drop-downs.
- `readFilterFromRequest(request)` — builds a `StudentFilter`, ignoring unknown values.
- `buildFilterQuery(filter)` — `"keyword=mehta&department=Civil+Engineering&"` (URL-encoded) for paging/export links.
- `setFlashSuccess` / `setFlashError` — one-time messages in the session.
- `readPositiveInt(request, name)` — safe ID reading; `-1` if missing/invalid.

### `RegisterServlet.java` — `/register`
- `doGet`: empty `Student` + form options → `register.jsp`.
- `doPost`: read → force Active → validate → (errors? back to form) → `addStudent` →
  `getStudentById` → store in session → redirect `/register/success`.
  Catches `DuplicateStudentException` (shows field error) and `SQLException` (error page).

### `RegistrationSuccessServlet.java` — `/register/success`
- Takes `registeredStudent` from the session, removes it, forwards to `registration-success.jsp`.
  If missing (direct visit or refresh), redirects to `/register`.

### `SetupServlet.java` — `/setup` (first-time setup)
- Works **only while the `admin` table is empty** (`adminDAO.countAdmins() == 0`); otherwise every
  GET or POST is redirected to `/login`, so nobody can use it to add extra admins.
- `doGet` shows `setup.jsp`; if the database can't be reached it shows `DBConnection.explain(e)`.
- `doPost` checks the username (`ValidationUtil.isValidUsername`), password strength and that both
  passwords match, then `createAdmin(username, PasswordUtil.hashPassword(password))` and redirects to
  `/login?setup=done`.

### `LoginServlet.java` — `/login`
- `doGet`: already logged in → dashboard; no admin exists yet → redirect to `/setup`; otherwise `login.jsp`.
- `doPost`: empty check → `findByUsername` → `PasswordUtil.checkPassword` → same error message for
  wrong user or wrong password → new session, timeout, admin + CSRF token → redirect dashboard.
  A `SQLException` shows "Cannot reach the MySQL server on localhost:3306. Make sure MySQL is running …" (DBConnection.explain gives a specific hint)

### `LogoutServlet.java` — `/admin/logout`
- `doPost`: `session.invalidate()` → redirect `/login?logout=true`. `doGet`: redirect to dashboard.

### `DashboardServlet.java` — `/admin/dashboard` and `/admin`
- Six DAO calls → request attributes `totalStudents, activeStudents, inactiveStudents,
  totalDepartments, activePercent, inactivePercent, recentStudents, departmentCounts` → `dashboard.jsp`.

### `StudentListServlet.java` — `/admin/students`
- `PAGE_SIZE = 10`. Reads the filter and `page`, counts matches, calculates
  `totalPages = max(1, ceil(total / 10))`, clamps the page number, loads one page, sets
  `firstRow`/`lastRow` for "Showing 1–10 of 17", forwards to `students.jsp`.

### `StudentProfileServlet.java` — `/admin/students/view?id=`
- Invalid or unknown ID → `sendError(404)`; otherwise `student-profile.jsp`.

### `AddStudentServlet.java` — `/admin/students/add`
- Like registration, but the admin chooses the status (`showStatusField = true`, `mode = "add"`).
  Success → flash message with the new registration ID → redirect to the profile.

### `EditStudentServlet.java` — `/admin/students/edit?id=`
- `doGet` loads the student into the form (`mode = "edit"`).
- `doPost` validates with `excludeStudentId = id`, calls `updateStudent`; `false` means the row was
  deleted meanwhile → flash error.

### `DeleteStudentServlet.java` — `/admin/students/delete` (POST only)
- Loads the student (for the message), deletes, sets a flash message, redirects to the list.

### `StudentStatusServlet.java` — `/admin/students/status` (POST only)
- Validates `status` is Active/Inactive, `updateStatus`, redirects to the profile.

### `ExportStudentsServlet.java` — `/admin/students/export`
- Writes `text/csv` with a UTF-8 BOM, a header line and one line per student (CRLF line endings).

---

## 6. Views (JSP)

| File | Shown by | Main content |
|---|---|---|
| `index.jsp` | welcome file | hero, "Register" / "Admin Login" buttons, feature cards |
| `WEB-INF/views/register.jsp` | RegisterServlet | registration form (includes `student-form-fields.jspf`), error summary, info cards |
| `WEB-INF/views/registration-success.jsp` | RegistrationSuccessServlet | registration ID "ticket", summary, Print button |
| `WEB-INF/views/setup.jsp` | SetupServlet | first-time "Create administrator" form; database problem hint |
| `WEB-INF/views/login.jsp` | LoginServlet | split screen login; messages for error, logout, login required, account created |
| `WEB-INF/views/error.jsp` | `web.xml` error pages | friendly text for 403/404/405/500 — no technical details |
| `WEB-INF/views/admin/dashboard.jsp` | DashboardServlet | 4 statistic cards, recent table, department bars |
| `WEB-INF/views/admin/students.jsp` | StudentListServlet | filter bar, table, delete forms, paging, empty states |
| `WEB-INF/views/admin/student-profile.jsp` | StudentProfileServlet | header with actions, 4 information cards, print layout |
| `WEB-INF/views/admin/student-form.jsp` | Add/EditStudentServlet | one form for both modes, hidden `csrfToken` and `id` |

Fragments in `WEB-INF/views/common/`: `head.jspf` (meta, title, CSS, defines `${ctx}`),
`scripts.jspf`, `public-navbar.jspf`, `public-footer.jspf`, `admin-sidebar.jspf` (menu + logout form),
`admin-topbar.jspf`, `admin-footer.jspf`, `flash-messages.jspf`, `confirm-modal.jspf`,
`student-form-fields.jspf` (all 10–11 form fields with server error messages).

**Escaping rule used everywhere:** text from users is printed with `<c:out value="${...}"/>` in the
page body and `${fn:escapeXml(...)}` inside attributes, e.g.
`value="${fn:escapeXml(student.fullName)}"`.

---

## 7. Static assets

- `assets/css/app.css` — theme variables (navy/blue), buttons, forms, cards, sidebar layout,
  tables, badges, empty states, responsive rules, `@media print` rules.
- `assets/js/app.js` — (1) `needs-validation` forms: `checkValidity()`, red highlights, focus first
  error, disable the submit button after the first click; remove server error highlight when typing;
  upper-case roll numbers while typing. (2) `form[data-confirm]` → Bootstrap modal confirmation.
  (3) `[data-action="print"]` → `window.print()`.
- `assets/vendor/` — Bootstrap 5.3.3, Bootstrap Icons 1.11.3, Inter font (all MIT/OFL licensed).
- `assets/img/favicon.svg` — the tab icon.

---

## 8. Configuration files

- `pom.xml` — see [COMPLETE_PROJECT_GUIDE §2.2](COMPLETE_PROJECT_GUIDE.md#22-maven--the-build-tool).
- `WEB-INF/web.xml` — encoding, `jsp-config`, `HttpHeaderSecurityFilter`, welcome file, session
  config (30 min, HttpOnly, COOKIE tracking), error pages.
- `META-INF/context.xml` — `<CookieProcessor sameSiteCookies="lax" />`.
- `src/main/resources/db.properties.example` — template; copy to `db.properties`.
- `database/schema.sql`, `database/sample_data.sql` — see [DATABASE_EXPLANATION.md](DATABASE_EXPLANATION.md).

---

## 9. Flow traces

### 9.1 Registration flow
`register.jsp → RegisterServlet → StudentRequestHelper → ValidationUtil → StudentDAO → DBConnection → MySQL`

1. Browser: `GET /register` → `RegisterServlet.doGet` → `request.setAttribute("student", new Student())`
   → `StudentRequestHelper.setFormOptions` → forward `register.jsp`.
2. Student fills the form; `app.js` runs `form.checkValidity()`; if OK the button is disabled
   ("Submitting…") and the browser sends `POST /register`.
3. `RegisterServlet.doPost` → `readStudentFromRequest` builds the `Student` → `setStatus("Active")`.
4. `StudentRequestHelper.validateStudent(student, studentDAO, 0)`:
   `ValidationUtil.validateStudent` → then `studentDAO.isRollNumberTaken(...)`, `isEmailTaken(...)`
   (each opens a connection and runs a `SELECT COUNT(*)`).
5. Errors → `showFormWithErrors` → `register.jsp` shows red fields. **Stop.**
6. No errors → `studentDAO.addStudent(student)` → `DBConnection.getConnection()` →
   `prepareStatement(INSERT_STUDENT_SQL, RETURN_GENERATED_KEYS)` → `setStudentParameters` →
   `executeUpdate()` → `getGeneratedKeys()` → returns the new ID.
7. `studentDAO.getStudentById(newId)` reads the saved row (with `created_at`).
8. `session.setAttribute("registeredStudent", saved)` → `sendRedirect("/register/success")`.
9. Browser: `GET /register/success` → `RegistrationSuccessServlet` takes and removes the student →
   `registration-success.jsp` shows `SRS-000NN`.

### 9.2 Admin login flow
0. Very first time only: `GET /login` finds no admin → redirect `/setup` → the admin account is created
   (`SetupServlet`) → redirect `/login?setup=done`.
1. `GET /login` → `LoginServlet.doGet` → `login.jsp`.
2. `POST /login` → `doPost` → `adminDAO.findByUsername` → `PasswordUtil.checkPassword`.
3. Wrong → `login.jsp` with "Invalid username or password." **Stop.**
4. Right → invalidate old session → create new session → `setMaxInactiveInterval(1800)` →
   attributes `loggedInAdmin`, `csrfToken` → redirect `/admin/dashboard`.

### 9.3 Dashboard loading flow
1. `GET /admin/dashboard` → `AuthFilter.doFilter` (session OK) → `DashboardServlet.doGet`.
2. Six DAO calls (six short SQL queries) → attributes → `dashboard.jsp`.
3. The JSP loops over `recentStudents` (`<c:forEach>`) and over the `departmentCounts` map
   (`entry.key`, `entry.value`) and calculates bar widths.

### 9.4 Student listing flow
1. `GET /admin/students` → `AuthFilter` → `StudentListServlet.doGet`.
2. Empty filter → `countStudents` (`WHERE 1 = 1`) → e.g. 17 → `totalPages = 2`.
3. `searchStudents(filter, 0, 10)` → newest 10 students → `students.jsp`.
4. Paging links: `/admin/students?<filterQuery>page=2`.

### 9.5 Search and filtering flow
1. Admin types "mehta", selects semester 3, clicks **Apply** → the GET form produces
   `/admin/students?keyword=mehta&department=&semester=3&status=`.
2. `readFilterFromRequest` → `StudentFilter(keyword="mehta", semester=3)` (empty values ignored,
   unknown departments ignored).
3. `buildWhereClause` → `WHERE 1 = 1 AND (full_name LIKE ? OR roll_number LIKE ? OR email LIKE ?) AND semester = ?`
   with parameters `["%mehta%", "%mehta%", "%mehta%", 3]`.
4. Results (or the "No students match your search" empty state) are shown; the inputs keep their values.

### 9.6 Edit operation
1. Profile → **Edit** → `GET /admin/students/edit?id=5` → `EditStudentServlet.doGet` →
   `getStudentById(5)` → `student-form.jsp` (mode edit, values filled, hidden `id` and `csrfToken`).
2. **Save Changes** → `POST /admin/students/edit` → `AuthFilter` checks session **and** token →
   `doPost` → `readStudentFromRequest` + `setStudentId(5)` → `validateStudent(..., 5)` →
   `updateStudent` → `UPDATE … WHERE student_id = ?` → flash "Changes to … were saved." → redirect to the profile.

### 9.7 Delete operation
1. Trash icon → `app.js` intercepts the submit of `form[data-confirm]` → modal "Delete student?".
2. **Delete** → `POST /admin/students/delete` with `id` and `csrfToken` → `AuthFilter` →
   `DeleteStudentServlet.doPost` → `getStudentById` → `deleteStudent` → `DELETE … WHERE student_id = ?`
   → flash "Student … was deleted." → redirect `/admin/students`.

### 9.8 Logout flow
1. Sidebar **Log out** → `POST /admin/logout` with `csrfToken` → `AuthFilter` → `LogoutServlet.doPost`
   → `session.invalidate()` → redirect `/login?logout=true` → "You have been logged out successfully."
2. Pressing Back or opening `/admin/dashboard` now → `AuthFilter` finds no session → redirect to login.
   Because of `Cache-Control: no-store`, the browser does not show a cached copy of the dashboard.
