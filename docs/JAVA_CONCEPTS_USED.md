# Java Concepts Used in This Project

Every concept below is **actually used** in the source code. For each one you will find:
**(1)** a simple definition, **(2)** why it matters, **(3)** where it is used, **(4)** real code from
this repository, **(5)** an explanation of that code.

| # | Concept | # | Concept |
|---|---|---|---|
| 1 | [Classes and objects](#1-classes-and-objects) | 15 | [JDBC](#15-jdbc) |
| 2 | [Encapsulation](#2-encapsulation) | 16 | [PreparedStatement](#16-preparedstatement) |
| 3 | [Constructors](#3-constructors) | 17 | [ResultSet](#17-resultset) |
| 4 | [Getters and setters](#4-getters-and-setters) | 18 | [Try-with-resources](#18-try-with-resources) |
| 5 | [Packages and imports](#5-packages-and-imports) | 19 | [Servlets](#19-servlets) |
| 6 | [Inheritance](#6-inheritance) | 20 | [HTTP GET and POST](#20-http-get-and-post) |
| 7 | [Interfaces](#7-interfaces) | 21 | [HttpSession](#21-httpsession) |
| 8 | [Polymorphism: overriding and overloading](#8-polymorphism-overriding-and-overloading) | 22 | [Filters](#22-filters) |
| 9 | [Static members, final and utility classes](#9-static-members-final-and-utility-classes) | 23 | [Annotations](#23-annotations) |
| 10 | [Exception handling](#10-exception-handling) | 24 | [MVC architecture](#24-mvc-architecture) |
| 11 | [Custom exceptions](#11-custom-exceptions) | 25 | [DAO pattern](#25-dao-pattern) |
| 12 | [Collections and generics](#12-collections-and-generics) | 26 | [Wrapper classes, ternary operator, varargs](#26-wrapper-classes-ternary-operator-and-varargs) |
| 13 | [Strings, StringBuilder and regular expressions](#13-strings-stringbuilder-and-regular-expressions) | 27 | [Access modifiers](#27-access-modifiers) |
| 14 | [Date and time API (java.time)](#14-date-and-time-api-javatime) | 28 | [Unit testing with JUnit](#28-unit-testing-with-junit) |

---

## 1. Classes and objects
1. **Definition:** A *class* is a blueprint describing data (fields) and behaviour (methods). An *object* is one concrete thing created from that blueprint with `new`.
2. **Why:** It lets us model real things (a student, an admin) in code.
3. **Where:** `Student`, `Admin`, `StudentFilter`, every DAO and servlet.
4. **Code** (`StudentDAO.mapRowToStudent`):
   ```java
   Student student = new Student();
   student.setStudentId(resultSet.getInt("student_id"));
   student.setRollNumber(resultSet.getString("roll_number"));
   ```
5. **Explanation:** For every database row, a new `Student` *object* is created from the `Student` *class* and filled with that row's values. 17 rows → 17 objects.

## 2. Encapsulation
1. **Definition:** Keeping an object's fields `private` and allowing access only through methods.
2. **Why:** The class controls how its data is read and changed; other code cannot put it into an invalid state by accident.
3. **Where:** All model classes.
4. **Code** (`Student.java`):
   ```java
   private int studentId;
   private String rollNumber;
   ...
   public String getRollNumber() { return rollNumber; }
   public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
   ```
5. **Explanation:** No other class can write `student.rollNumber = ...`. It must call `setRollNumber(...)`. Derived values such as `getRegistrationId()` and `getAge()` are calculated inside the class, so the rule for building "SRS-00012" exists in exactly one place.

## 3. Constructors
1. **Definition:** A special method with the class name, run when an object is created.
2. **Why:** It puts a new object into a correct starting state.
3. **Where:** `Student`, `Admin`, `StudentFilter`, `DuplicateStudentException`.
4. **Code** (`Student.java`):
   ```java
   public Student() {
       this.status = STATUS_ACTIVE;
   }

   public Student(String rollNumber, String fullName, String email, String phone, String gender,
                  LocalDate dob, String department, String course, int semester, String address) {
       this();  // calls the no-argument constructor above, so status = Active
       this.rollNumber = rollNumber;
       ...
   }
   ```
5. **Explanation:** There are two **overloaded** constructors. The second one calls the first with `this()` (*constructor chaining*), so every new student is "Active" by default no matter which constructor is used. `this.rollNumber = rollNumber` uses `this` to distinguish the field from the parameter with the same name.

## 4. Getters and setters
1. **Definition:** Public methods `getX()` / `setX(value)` (or `isX()` for booleans) that read and write a private field.
2. **Why:** Needed for encapsulation, and JSP Expression Language relies on them: `${student.fullName}` calls `getFullName()`.
3. **Where:** Model classes; used heavily by DAOs and JSPs.
4. **Code** (`Student.java` and `students.jsp`):
   ```java
   public boolean isActive() { return STATUS_ACTIVE.equals(status); }
   ```
   ```jsp
   <span class="status-badge ${s.active ? 'status-active' : 'status-inactive'}">
   ```
5. **Explanation:** In the JSP, `s.active` is automatically translated to `s.isActive()`. The JSP never touches the field directly.

## 5. Packages and imports
1. **Definition:** A package groups related classes (like folders); `import` lets a class use classes from other packages.
2. **Why:** Organisation and avoiding name clashes.
3. **Where:** `com.student.model`, `.dao`, `.servlet`, `.filter`, `.listener`, `.util`.
4. **Code** (`StudentDAO.java`):
   ```java
   package com.student.dao;

   import com.student.model.Student;
   import com.student.util.DBConnection;
   import java.sql.PreparedStatement;
   ```
5. **Explanation:** The package names mirror the MVC + DAO layers, so you can tell a class's role from its package.

## 6. Inheritance
1. **Definition:** A class (*subclass*) `extends` another (*superclass*) and inherits its fields and methods.
2. **Why:** Reuse existing behaviour and specialise only what is different.
3. **Where:** (a) all 12 servlets `extends HttpServlet` (an abstract class from the Servlet API); (b) `DuplicateStudentException extends Exception`.
4. **Code:**
   ```java
   public class RegisterServlet extends HttpServlet { ... }
   public class DuplicateStudentException extends Exception { ... }
   ```
5. **Explanation:** `HttpServlet` already knows how to read an HTTP request and decide whether to call `doGet` or `doPost`. Our servlets inherit all that and only override `doGet`/`doPost`. If a servlet doesn't override `doGet` (like `DeleteStudentServlet`), the inherited version answers **405 Method Not Allowed** — that is how "POST only" is enforced.

## 7. Interfaces
1. **Definition:** A contract listing methods a class promises to implement (`implements`).
2. **Why:** Different code can work with any class that follows the contract.
3. **Where:**
   - `AuthFilter implements Filter` (methods `init`, `doFilter`, `destroy`),
   - `AppContextListener implements ServletContextListener` (`contextInitialized`, `contextDestroyed`),
   - `Student implements Serializable`, `Admin implements Serializable` (marker interface),
   - JDBC: `Connection`, `PreparedStatement`, `ResultSet` are interfaces implemented by the MySQL driver,
   - Collections: we declare `List<Student>` and `Map<String, Integer>` (interfaces) but create `ArrayList` / `LinkedHashMap`.
4. **Code** (`AppContextListener.java`):
   ```java
   @WebListener
   public class AppContextListener implements ServletContextListener {
       @Override
       public void contextInitialized(ServletContextEvent event) { ... }
       @Override
       public void contextDestroyed(ServletContextEvent event) { ... }
   }
   ```
5. **Explanation:** Tomcat doesn't know our class, but it knows the `ServletContextListener` interface, so it can call `contextInitialized` at start-up. Likewise, `DBConnection.getConnection()` returns a `Connection` — our code never mentions the MySQL driver's actual class name; we program to the interface.

## 8. Polymorphism: overriding and overloading
1. **Definition:** *Overriding* = a subclass provides its own version of an inherited method (run-time polymorphism). *Overloading* = several methods with the same name but different parameters (compile-time polymorphism).
2. **Why:** Overriding lets Tomcat call *our* `doGet`; overloading gives one meaningful name to related operations.
3. **Where:** `@Override doGet/doPost` in servlets, `@Override toString()` in `Student`; overloaded `searchStudents(...)` in `StudentDAO` and the two `Student` constructors.
4. **Code** (`StudentDAO.java`):
   ```java
   public List<Student> searchStudents(StudentFilter filter, int offset, int limit) throws SQLException { ... }
   public List<Student> searchStudents(StudentFilter filter) throws SQLException { ... }
   ```
5. **Explanation:** The list page calls the 3-parameter version (one page of 10); the CSV export calls the 1-parameter version (all rows). Java picks the right one from the arguments. Tomcat holds our servlet as an `HttpServlet` reference but calls our overridden `doGet` — that is run-time polymorphism.

## 9. Static members, final and utility classes
1. **Definition:** `static` members belong to the class, not to objects. `final` means "cannot be changed / reassigned". A *utility class* has only static methods and a private constructor.
2. **Why:** Helpers like validation don't need object state; constants should never change.
3. **Where:** `DBConnection`, `ValidationUtil`, `PasswordUtil`, `CsrfUtil`, `CsvUtil`, `AppConstants`; constants such as `Student.STATUS_ACTIVE`, `StudentListServlet.PAGE_SIZE`.
4. **Code** (`DBConnection.java`):
   ```java
   private static final String URL;
   static {
       Class.forName(DRIVER_CLASS);   // (inside try/catch)
       Properties fileSettings = loadPropertiesFile();
       URL = readSetting("DB_URL", "db.url", fileSettings);
       ...
   }
   private DBConnection() { }
   public static Connection getConnection() throws SQLException { ... }
   ```
5. **Explanation:** The **static initialisation block** runs once when the class is first used, loading the driver and the settings. Callers write `DBConnection.getConnection()` without creating an object; the private constructor prevents `new DBConnection()`.

## 10. Exception handling
1. **Definition:** A mechanism to deal with errors using `try`, `catch`, `finally`, `throw` and `throws`.
2. **Why:** The application must not crash or show stack traces when, for example, MySQL is down or the user types "five" as a semester.
3. **Where:** Everywhere; important examples below.
4. **Code:**
   ```java
   // ValidationUtil.parseInteger - turn bad input into null instead of an error
   try {
       return Integer.parseInt(value.trim());
   } catch (NumberFormatException e) {
       return null;
   }
   ```
   ```java
   // RegisterServlet.doPost - different handling for different problems
   } catch (DuplicateStudentException e) {
       showFormWithErrors(request, response, student, Map.of(e.getField(), e.getMessage()));
   } catch (SQLException e) {
       log("Registration failed for roll number " + student.getRollNumber(), e);
       throw new ServletException("Could not save the registration.", e);
   }
   ```
5. **Explanation:** DAO methods declare `throws SQLException` (a **checked** exception — the compiler forces callers to handle it). The servlet catches it, writes details to the Tomcat log, and re-throws it wrapped in a `ServletException`; `web.xml` then shows the friendly `error.jsp`. A duplicate is an expected situation, so it gets its own exception type and becomes a normal form error.

## 11. Custom exceptions
1. **Definition:** Your own exception class that extends `Exception` (checked) or `RuntimeException` (unchecked).
2. **Why:** To carry extra information and make the error meaning clear.
3. **Where:** `dao/DuplicateStudentException.java`.
4. **Code:**
   ```java
   public class DuplicateStudentException extends Exception {
       private final String field;
       public DuplicateStudentException(String field, String message, Throwable cause) {
           super(message, cause);
           this.field = field;
       }
       public String getField() { return field; }
   }
   ```
   ```java
   // StudentDAO.addStudent
   } catch (SQLIntegrityConstraintViolationException e) {
       throw toDuplicateException(e);
   }
   ```
5. **Explanation:** MySQL's error (`SQLIntegrityConstraintViolationException`) is *translated* into a project-specific exception that says *which field* is duplicated. `super(message, cause)` passes the message to `Exception` and keeps the original error as the *cause* for debugging.

## 12. Collections and generics
1. **Definition:** The Collections Framework provides data structures like `List` (ordered), `Map` (key → value). *Generics* (`List<Student>`) state the element type so the compiler can check it.
2. **Why:** Queries return a variable number of rows; validation produces a variable number of errors.
3. **Where:** `List<Student>` (search results), `Map<String, String>` (validation errors), `Map<String, Integer>` (department counts), `List<Object>` (SQL parameters), `List.of(...)` (constants).
4. **Code:**
   ```java
   // StudentDAO.getDepartmentCounts
   Map<String, Integer> counts = new LinkedHashMap<>();
   while (resultSet.next()) {
       counts.put(resultSet.getString("department"), resultSet.getInt("total"));
   }
   ```
   ```java
   // AppConstants
   public static final List<String> GENDERS = List.of("Male", "Female", "Other");
   ```
5. **Explanation:** `LinkedHashMap` (instead of `HashMap`) keeps insertion order, so departments stay sorted largest-first as returned by `ORDER BY total DESC`. `List.of` makes an **unmodifiable** list — code like `GENDERS.add("X")` would throw an exception. `AppConstants.GENDERS.contains(gender)` is used for validation. In JSP, `<c:forEach var="entry" items="${departmentCounts}">` loops over map entries (`entry.key`, `entry.value`).

## 13. Strings, StringBuilder and regular expressions
1. **Definition:** `String` is immutable text; `StringBuilder` builds text efficiently piece by piece; a *regular expression* (regex) is a pattern for matching text.
2. **Why:** Building dynamic SQL safely; validating email, phone, roll numbers and names.
3. **Where:** `StudentDAO.buildWhereClause`, `StudentRequestHelper.buildFilterQuery`, `ValidationUtil`.
4. **Code** (`ValidationUtil.java`):
   ```java
   private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");

   public static boolean isValidPhone(String phone) {
       return phone != null && PHONE_PATTERN.matcher(phone).matches();
   }
   ```
5. **Explanation:** `^` start, `[6-9]` one digit from 6 to 9, `[0-9]{9}` exactly nine more digits, `$` end. Compiling the pattern once (`static final`) is faster than compiling it on every call. Other String methods used: `trim()`, `isBlank()`, `toUpperCase(Locale.ROOT)`, `replaceAll("\\s+", " ")`, `String.format("%05d", id)`, `contains(...)`.

## 14. Date and time API (`java.time`)
1. **Definition:** Modern immutable date/time classes: `LocalDate` (date only), `LocalDateTime`, `Period`, `DateTimeFormatter`.
2. **Why:** Date of birth, age calculation, registration time.
3. **Where:** `Student`, `ValidationUtil`, `StudentDAO` (conversion to/from `java.sql.Date` / `Timestamp`).
4. **Code:**
   ```java
   // Student.getAge
   return dob == null ? 0 : Period.between(dob, LocalDate.now()).getYears();
   // StudentDAO: Java -> SQL and SQL -> Java
   statement.setDate(6, Date.valueOf(student.getDob()));
   student.setDob(resultSet.getDate("dob").toLocalDate());   // (null-checked in the code)
   ```
5. **Explanation:** `Period.between` handles leap years and month lengths correctly. JDBC uses `java.sql.Date`, so we convert with `Date.valueOf(...)` and `toLocalDate()`. `DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)` formats dates the same way on every computer.

## 15. JDBC
1. **Definition:** Java Database Connectivity — the standard API (`java.sql`) for connecting to relational databases and running SQL.
2. **Why:** It is how this Java application stores and reads data in MySQL.
3. **Where:** `DBConnection`, `StudentDAO`, `AdminDAO`, `AppContextListener`.
4. **Code** (`DBConnection.getConnection`):
   ```java
   return DriverManager.getConnection(URL, USER, PASSWORD == null ? "" : PASSWORD);
   ```
   with `URL = jdbc:mysql://localhost:3306/student_registration_db?useSSL=false&allowPublicKeyRetrieval=true`.
5. **Explanation:** Steps of JDBC: (1) load the driver (`Class.forName`), (2) get a `Connection`, (3) create a `PreparedStatement`, (4) execute it (`executeQuery` for SELECT, `executeUpdate` for INSERT/UPDATE/DELETE), (5) read the `ResultSet`, (6) close everything (done automatically by try-with-resources).

## 16. PreparedStatement
1. **Definition:** A pre-compiled SQL statement with `?` placeholders; values are bound with `setString`, `setInt`, `setDate`…
2. **Why:** **Prevents SQL injection** and handles quotes in data (e.g. "O'Brien") correctly.
3. **Where:** Every SQL statement in the project.
4. **Code** (`AdminDAO.findByUsername`):
   ```java
   private static final String FIND_BY_USERNAME_SQL =
           "SELECT admin_id, username, password_hash FROM admin WHERE username = ?";
   ...
   statement.setString(1, username);
   ```
5. **Explanation:** If someone types `admin' OR '1'='1` as the username, with string concatenation the SQL would become `... WHERE username = 'admin' OR '1'='1'` and match every row. With `PreparedStatement`, the SQL text is fixed and the value is sent separately; MySQL looks for a user literally named `admin' OR '1'='1` and finds none. This is tested in `StudentDAOIntegrationTest.sqlInjectionIsTreatedAsText` and `scripts/functional-test.sh` (TC-SEC-02, TC-SEC-03).

## 17. ResultSet
1. **Definition:** The table of rows returned by a SELECT, read with a cursor.
2. **Why:** It is how query results come back from the database.
3. **Where:** All read methods in the DAOs.
4. **Code** (`StudentDAO.queryStudents`):
   ```java
   try (ResultSet resultSet = statement.executeQuery()) {
       while (resultSet.next()) {
           students.add(mapRowToStudent(resultSet));
       }
   }
   ```
5. **Explanation:** The cursor starts *before* the first row. `next()` moves to the next row and returns `false` when there are no more. Inside the loop, `resultSet.getString("full_name")`, `getInt("semester")` etc. read the current row's columns.

## 18. Try-with-resources
1. **Definition:** `try (Resource r = ...) { ... }` automatically calls `r.close()` at the end, even if an exception happens. Works with any `AutoCloseable`.
2. **Why:** Unclosed connections would eventually exhaust MySQL's connection limit and crash the application.
3. **Where:** Every DAO method, `DBConnection.loadPropertiesFile`, `AppContextListener`.
4. **Code:**
   ```java
   try (Connection connection = DBConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE_STUDENT_SQL)) {
       statement.setInt(1, studentId);
       return statement.executeUpdate() > 0;
   }
   ```
5. **Explanation:** Resources are closed in reverse order (statement, then connection) as soon as the block ends — including when `return` runs or a `SQLException` is thrown. Without it we would need a long `finally` block with null checks.

## 19. Servlets
1. **Definition:** Java classes that run inside a servlet container (Tomcat) and respond to HTTP requests.
2. **Why:** They are the controllers of the web application.
3. **Where:** 12 classes in `com.student.servlet`.
4. **Code** (`LogoutServlet`):
   ```java
   @WebServlet("/admin/logout")
   public class LogoutServlet extends HttpServlet {
       @Override
       protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
           HttpSession session = request.getSession(false);
           if (session != null) {
               session.invalidate();
           }
           response.sendRedirect(request.getContextPath() + "/login?logout=true");
       }
   }
   ```
5. **Explanation:** **Life cycle:** Tomcat creates one instance, calls `init()` once, calls `service()` → `doGet`/`doPost` for every request (each on its own thread), and `destroy()` at shutdown. Because many threads share one instance, our servlets keep **no per-user data in fields** — only the stateless DAO object. Two ways to continue: `forward()` (server-side, same request, used to show a JSP) and `sendRedirect()` (tells the browser to make a new request, used after POST).

## 20. HTTP GET and POST
1. **Definition:** GET requests data (parameters in the URL); POST sends data to be processed (parameters in the request body).
2. **Why:** GET must be "safe" — it must not change data. POST is for changes.
3. **Where:** Search/list/profile/export = GET; register/login/add/edit/delete/status/logout = POST.
4. **Code** (`students.jsp` and `student-profile.jsp`):
   ```jsp
   <form action="${ctx}/admin/students" method="get">         <!-- search: bookmarkable URL -->
   <form action="${ctx}/admin/students/delete" method="post"> <!-- delete: changes data -->
       <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
       <input type="hidden" name="id" value="${student.studentId}">
   ```
5. **Explanation:** A search URL like `/admin/students?semester=3` can be bookmarked and shared. A delete must never happen just by opening a link, so `DeleteStudentServlet` only has `doPost`; a GET receives 405.

## 21. HttpSession
1. **Definition:** Server-side storage for one browser, identified by the `JSESSIONID` cookie.
2. **Why:** HTTP is stateless — without a session the server would forget the login after every page.
3. **Where:** `LoginServlet` (create), `AuthFilter` (check), `LogoutServlet` (invalidate), registration success data, flash messages, CSRF token.
4. **Code** (`LoginServlet.doPost`):
   ```java
   HttpSession session = request.getSession(true);
   session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
   session.setAttribute(AuthFilter.ADMIN_SESSION_ATTRIBUTE,
           new Admin(admin.getAdminId(), admin.getUsername(), null));
   ```
5. **Explanation:** `getSession(true)` creates a session (and cookie); `getSession(false)` returns `null` if none exists (used in the filter so visitors don't get sessions they don't need). `invalidate()` destroys it. The admin object is stored *without* the password hash.

## 22. Filters
1. **Definition:** A component that intercepts requests before they reach a servlet (and responses after).
2. **Why:** Cross-cutting concerns like security belong in one place.
3. **Where:** `AuthFilter` (`/admin/*`); Tomcat's `HttpHeaderSecurityFilter` configured in `web.xml` (`/*`).
4. **Code:**
   ```java
   if (!loggedIn) {
       response.sendRedirect(request.getContextPath() + "/login?required=true");
       return;
   }
   ...
   chain.doFilter(request, response);
   ```
5. **Explanation:** Not calling `chain.doFilter` stops the request; calling it passes control to the next filter or the servlet.

## 23. Annotations
1. **Definition:** Metadata in the code starting with `@`, read by the compiler or the server.
2. **Why:** Configure servlets without long XML; let the compiler catch mistakes.
3. **Where:** `@WebServlet`, `@WebFilter`, `@WebListener`, `@Override`; in tests `@Test`, `@DisplayName`, `@AfterEach`, `@EnabledIfSystemProperty`.
4. **Code:** `@WebServlet({"/admin/dashboard", "/admin"})` on `DashboardServlet`.
5. **Explanation:** Tomcat scans classes at start-up and maps both URLs to this servlet. `@Override` makes the compiler check that the method really overrides a superclass method (a typo like `doGEt` would be a compile error).

## 24. MVC architecture
1. **Definition:** Model–View–Controller separates data, presentation and request handling.
2. **Why:** Clean, testable, explainable code; teams can work on parts independently.
3. **Where:** Model = `com.student.model`; View = JSPs in `WEB-INF/views`; Controller = `com.student.servlet`.
4. **Code** (`DashboardServlet`, controller handing model data to the view):
   ```java
   request.setAttribute("recentStudents", recentStudents);
   request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
   ```
5. **Explanation:** The servlet never writes HTML; the JSP never runs SQL. See [ARCHITECTURE.md](ARCHITECTURE.md).

## 25. DAO pattern
1. **Definition:** A Data Access Object hides all database access behind methods with business-meaningful names.
2. **Why:** SQL in one place; servlets independent of the database details.
3. **Where:** `StudentDAO`, `AdminDAO`.
4. **Code:** `boolean deleted = studentDAO.deleteStudent(studentId);`
5. **Explanation:** The servlet doesn't know the table name or SQL syntax. If the database changed, only the DAO would change.

## 26. Wrapper classes, ternary operator and varargs
1. **Definition:** Wrapper classes (`Integer`) let primitives be `null` or stored in collections (autoboxing converts automatically). The ternary operator `condition ? a : b` is a compact if/else. Varargs (`String... values`) accept any number of arguments.
2. **Why:** "No semester chosen" must be representable (`null`); short conditional expressions; flexible helper methods.
3. **Where:** `StudentFilter.semester` is `Integer`; `parameters.add(limit)` autoboxes `int` → `Integer`; ternaries in many places; `CsvUtil.toCsvLine(String... values)`.
4. **Code:**
   ```java
   student.setSemester(semester == null ? 0 : semester);                // StudentRequestHelper
   public static String toCsvLine(String... values) { ... }             // CsvUtil
   if (value instanceof Integer) { statement.setInt(position, (Integer) value); } // StudentDAO
   ```
5. **Explanation:** `instanceof` checks the runtime type before casting, so integers are bound with `setInt` and text with `setString`.

## 27. Access modifiers
1. **Definition:** `public` (everyone), `protected` (subclasses and same package), *package-private* (no keyword: same package), `private` (same class).
2. **Why:** Hide internal details; expose only what is needed.
3. **Where:** Fields `private`; DAO API `public`; DAO helpers `private`; `doGet`/`doPost` `protected` (as in `HttpServlet`); `StudentRequestHelper` and its methods are package-private — only servlets in the same package use them.
4. **Code:** `final class StudentRequestHelper { static Student readStudentFromRequest(HttpServletRequest request) { ... } }`
5. **Explanation:** The `final` class cannot be extended, and without `public` it is invisible outside `com.student.servlet`.

## 28. Unit testing with JUnit
1. **Definition:** Automated tests that call methods and check results with assertions.
2. **Why:** Prove that validation, hashing and escaping work, and keep working after changes.
3. **Where:** `src/test/java` — `ValidationUtilTest`, `PasswordUtilTest`, `CsvUtilTest`, `StudentTest`, `StudentDAOIntegrationTest`.
4. **Code** (`PasswordUtilTest`):
   ```java
   @Test
   void samePasswordGivesDifferentHashesBecauseOfRandomSalt() {
       String first = PasswordUtil.hashPassword("Secret123");
       String second = PasswordUtil.hashPassword("Secret123");
       assertNotEquals(first, second);
       assertTrue(PasswordUtil.checkPassword("Secret123", first));
   }
   ```
5. **Explanation:** `mvn test` runs all methods annotated `@Test`. The integration test also uses a **lambda** in `assertThrows(DuplicateStudentException.class, () -> studentDAO.addStudent(duplicate))` — the lambda is the code that is expected to throw.

---

### Concepts deliberately **not** used (and why)
- **Abstract classes of our own / custom interfaces for DAOs** — not needed for two DAOs; would add abstraction without benefit. (We *use* the abstract class `HttpServlet` and many interfaces.)
- **Multithreading code** — Tomcat handles threads; our servlets are stateless so we don't need `synchronized`.
- **ORM (Hibernate/JPA), Spring** — excluded on purpose to show JDBC and SQL directly (see [DESIGN_DECISIONS.md](DESIGN_DECISIONS.md)).
