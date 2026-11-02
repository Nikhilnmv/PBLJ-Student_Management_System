# Diagrams

All diagrams are written in **Mermaid**, a text format that GitHub renders automatically. The `.mmd` files in this
folder are the editable sources; `exports/` contains PNG (for Word/PDF reports) and SVG (scalable) versions.
Every diagram uses the real class, method, URL and table names from the code.

## How to edit and re-export

1. Edit the `.mmd` file (any text editor). Preview it at <https://mermaid.live> (paste the text) or with the
   Mermaid plugin of IntelliJ IDEA / VS Code.
2. Re-export (needs Node.js; run from the project folder):
   ```bash
   npx -p @mermaid-js/mermaid-cli mmdc -i docs/diagrams/er-diagram.mmd -o docs/diagrams/exports/er-diagram.png -b white -s 2
   ```
   Or click **Actions → PNG/SVG** on mermaid.live and save the file into `exports/`.
3. If the diagram is also shown inside another document (e.g. ARCHITECTURE.md), copy the new text there too.

## Index

| # | Diagram | Source | PNG | SVG |
|---|---|---|---|---|
| 1 | [High-level system architecture](#1-high-level-system-architecture) | [`architecture.mmd`](architecture.mmd) | [png](exports/architecture.png) | [svg](exports/architecture.svg) |
| 2 | [MVC + DAO architecture](#2-mvc--dao-architecture) | [`mvc-architecture.mmd`](mvc-architecture.mmd) | [png](exports/mvc-architecture.png) | [svg](exports/mvc-architecture.svg) |
| 3 | [Component diagram](#3-component-diagram) | [`component-diagram.mmd`](component-diagram.mmd) | [png](exports/component-diagram.png) | [svg](exports/component-diagram.svg) |
| 4 | [Deployment diagram](#4-deployment-diagram) | [`deployment-diagram.mmd`](deployment-diagram.mmd) | [png](exports/deployment-diagram.png) | [svg](exports/deployment-diagram.svg) |
| 5 | [Request–response lifecycle](#5-requestresponse-lifecycle) | [`request-lifecycle.mmd`](request-lifecycle.mmd) | [png](exports/request-lifecycle.png) | [svg](exports/request-lifecycle.svg) |
| 6 | [Use case diagram](#6-use-case-diagram) | [`use-case-diagram.mmd`](use-case-diagram.mmd) | [png](exports/use-case-diagram.png) | [svg](exports/use-case-diagram.svg) |
| 7 | [Class diagram](#7-class-diagram) | [`class-diagram.mmd`](class-diagram.mmd) | [png](exports/class-diagram.png) | [svg](exports/class-diagram.svg) |
| 8 | [Sequence diagram – Student registration](#8-sequence-diagram--student-registration) | [`registration-sequence.mmd`](registration-sequence.mmd) | [png](exports/registration-sequence.png) | [svg](exports/registration-sequence.svg) |
| 9 | [Sequence diagram – Admin login and logout](#9-sequence-diagram--admin-login-and-logout) | [`login-sequence.mmd`](login-sequence.mmd) | [png](exports/login-sequence.png) | [svg](exports/login-sequence.svg) |
| 10 | [Sequence diagram – Student management](#10-sequence-diagram--student-management) | [`student-management-sequence.mmd`](student-management-sequence.mmd) | [png](exports/student-management-sequence.png) | [svg](exports/student-management-sequence.svg) |
| 11 | [Activity diagram – Student registration](#11-activity-diagram--student-registration) | [`activity-registration.mmd`](activity-registration.mmd) | [png](exports/activity-registration.png) | [svg](exports/activity-registration.svg) |
| 12 | [Activity diagram – Admin management](#12-activity-diagram--admin-management) | [`activity-admin-management.mmd`](activity-admin-management.mmd) | [png](exports/activity-admin-management.png) | [svg](exports/activity-admin-management.svg) |
| 13 | [Entity relationship diagram](#13-entity-relationship-diagram) | [`er-diagram.mmd`](er-diagram.mmd) | [png](exports/er-diagram.png) | [svg](exports/er-diagram.svg) |
| 14 | [DFD Level 0 – Context diagram](#14-dfd-level-0--context-diagram) | [`dfd-level-0.mmd`](dfd-level-0.mmd) | [png](exports/dfd-level-0.png) | [svg](exports/dfd-level-0.svg) |
| 15 | [DFD Level 1 – Main processes](#15-dfd-level-1--main-processes) | [`dfd-level-1.mmd`](dfd-level-1.mmd) | [png](exports/dfd-level-1.png) | [svg](exports/dfd-level-1.svg) |

## 1. High-level system architecture

Browser, Tomcat (filter, servlets, JSP, DAO, utilities, model) and MySQL.

```mermaid
---
title: High-Level System Architecture
---
flowchart LR
    subgraph Client["Client (Web Browser)"]
        UI["HTML pages + Bootstrap 5 CSS<br/>app.js (validation, confirm dialog)"]
    end

    subgraph Server["Apache Tomcat 9 (Java 17)"]
        direction TB
        F["AuthFilter<br/>(protects /admin/*)"]
        C["Controllers<br/>13 Servlets"]
        V["Views<br/>JSP + JSTL"]
        D["DAO Layer<br/>StudentDAO, AdminDAO"]
        U["Utilities<br/>DBConnection, ValidationUtil,<br/>PasswordUtil, CsrfUtil, CsvUtil"]
        M["Model<br/>Student, Admin, StudentFilter"]
    end

    subgraph DB["MySQL 8 Server"]
        T1[("students")]
        T2[("admin")]
    end

    UI -- "HTTP request (GET / POST)" --> F
    F --> C
    C -- "uses" --> U
    C -- "calls" --> D
    D -- "JDBC + PreparedStatement" --> DB
    D -. "returns objects" .-> M
    C -- "forward with data" --> V
    V -- "HTML response" --> UI
```

## 2. MVC + DAO architecture

The eight steps from request to HTML across Controller, DAO, Model and View.

```mermaid
---
title: MVC + DAO Architecture
---
flowchart TB
    Browser(["Browser"])

    subgraph Controller["CONTROLLER - com.student.servlet"]
        S1["RegisterServlet"]
        S2["LoginServlet / LogoutServlet"]
        S3["DashboardServlet"]
        S4["StudentListServlet / StudentProfileServlet"]
        S5["Add / Edit / Delete / Status / Export Servlets"]
    end

    subgraph View["VIEW - /WEB-INF/views (JSP)"]
        J1["register.jsp, registration-success.jsp"]
        J2["login.jsp"]
        J3["admin/dashboard.jsp"]
        J4["admin/students.jsp, admin/student-profile.jsp"]
        J5["admin/student-form.jsp"]
    end

    subgraph Model["MODEL - com.student.model"]
        M1["Student"]
        M2["Admin"]
        M3["StudentFilter"]
    end

    subgraph DAO["DAO - com.student.dao"]
        D1["StudentDAO"]
        D2["AdminDAO"]
    end

    DB[("MySQL<br/>student_registration_db")]

    Browser -- "1 request" --> Controller
    Controller -- "2 call method" --> DAO
    DAO -- "3 SQL" --> DB
    DB -- "4 rows (ResultSet)" --> DAO
    DAO -- "5 Model objects" --> Controller
    Controller -- "6 request.setAttribute + forward" --> View
    View -- "7 reads Model with EL" --> Model
    View -- "8 HTML" --> Browser
```

## 3. Component diagram

Web components, data/utility components, setup tool, driver, configuration and database.

```mermaid
---
title: Component Diagram
---
flowchart LR
    subgraph WebApp["student-registration.war"]
        direction LR
        subgraph Web["Web components"]
            LIS["AppContextListener"]
            AF["AuthFilter"]
            HSF["HttpHeaderSecurityFilter<br/>(Tomcat built-in)"]
            PUB["Public servlets<br/>RegisterServlet<br/>RegistrationSuccessServlet<br/>LoginServlet<br/>SetupServlet (first-time setup)"]
            ADM["Admin servlets<br/>DashboardServlet, StudentListServlet,<br/>StudentProfileServlet, AddStudentServlet,<br/>EditStudentServlet, DeleteStudentServlet,<br/>StudentStatusServlet, ExportStudentsServlet,<br/>LogoutServlet"]
            HLP["StudentRequestHelper"]
            JSP["JSP views + JSPF fragments"]
            STATIC["Static assets<br/>app.css, app.js, Bootstrap, icons, font"]
        end
        subgraph Logic["Data and utilities"]
            DAO["StudentDAO / AdminDAO"]
            MOD["Student / Admin / StudentFilter"]
            VAL["ValidationUtil + AppConstants"]
            SEC["PasswordUtil (jBCrypt)<br/>CsrfUtil"]
            CSV["CsvUtil"]
            DBC["DBConnection"]
        end
    end
    TOOL["AdminSetup<br/>(command-line tool)"]
    DRV["MySQL Connector/J<br/>(JDBC driver)"]
    MYSQL[("MySQL 8")]
    CFG["db.properties or<br/>DB_URL / DB_USER / DB_PASSWORD"]

    HSF --> AF --> ADM
    PUB --> HLP
    ADM --> HLP
    HLP --> VAL
    PUB --> DAO
    ADM --> DAO
    PUB --> SEC
    ADM --> CSV
    PUB --> JSP
    ADM --> JSP
    DAO --> MOD
    DAO --> DBC
    TOOL --> DAO
    TOOL --> SEC
    LIS --> DBC
    DBC --> CFG
    DBC --> DRV --> MYSQL
```

## 4. Deployment diagram

Everything on one laptop: browser, Tomcat on 8080, MySQL on 3306.

```mermaid
---
title: Deployment Diagram (local execution on one laptop)
---
flowchart TB
    subgraph Laptop["Student laptop (Windows / macOS / Linux)"]
        subgraph Browser["Web browser (Chrome, Edge, Firefox)"]
            Pages["http://localhost:8080/student-registration/"]
        end
        subgraph JVM["Java 17 JVM"]
            subgraph Tomcat["Apache Tomcat 9 - port 8080"]
                WAR["student-registration.war<br/>WEB-INF/classes (compiled code)<br/>WEB-INF/lib: mysql-connector-j, jbcrypt, jstl"]
            end
        end
        subgraph MySQLServer["MySQL 8 Server - port 3306"]
            Schema[("student_registration_db<br/>students, admin")]
        end
        Workbench["MySQL Workbench<br/>(optional, to view tables)"]
        Maven["Maven 3<br/>mvn clean package"]
    end

    Pages -- "HTTP :8080" --> Tomcat
    WAR -- "JDBC :3306" --> Schema
    Workbench -- ":3306" --> Schema
    Maven -- "builds" --> WAR
```

## 5. Request–response lifecycle

Sequence of one admin request through filter, servlet, DAO, MySQL and JSP.

```mermaid
---
title: Request-Response Lifecycle (example - open the student list)
---
sequenceDiagram
    autonumber
    actor Admin as Admin (Browser)
    participant Tomcat as Tomcat 9
    participant Filter as AuthFilter
    participant Servlet as StudentListServlet
    participant DAO as StudentDAO
    participant DBC as DBConnection
    participant DB as MySQL
    participant JSP as students.jsp

    Admin->>Tomcat: GET /admin/students?semester=3 (cookie JSESSIONID)
    Tomcat->>Filter: doFilter(request, response, chain)
    Filter->>Filter: session has "loggedInAdmin"?
    alt not logged in
        Filter-->>Admin: 302 redirect to /login?required=true
    else logged in
        Filter->>Servlet: chain.doFilter() calls doGet()
        Servlet->>Servlet: readFilterFromRequest() builds StudentFilter
        Servlet->>DAO: countStudents(filter)
        DAO->>DBC: getConnection()
        DBC->>DB: open JDBC connection
        DAO->>DB: SELECT COUNT(*) ... WHERE semester = ?
        DB-->>DAO: ResultSet (count)
        Servlet->>DAO: searchStudents(filter, offset, 10)
        DAO->>DB: SELECT ... WHERE semester = ? ORDER BY ... LIMIT ? OFFSET ?
        DB-->>DAO: ResultSet rows
        DAO-->>Servlet: List of Student objects
        Servlet->>JSP: request.setAttribute(...) + forward()
        JSP-->>Admin: 200 OK - HTML table of students
    end
```

## 6. Use case diagram

Student and Administrator use cases UC-01 to UC-13.

```mermaid
---
title: Use Case Diagram
---
flowchart LR
    Student["Actor:<br/>Student<br/>(public visitor)"]
    Admin["Actor:<br/>Administrator"]

    subgraph System["Student Registration System"]
        direction TB
        UC1(["UC-01 Register as a student"])
        UC2(["UC-02 View registration ID"])
        UC3(["UC-03 Log in"])
        UC4(["UC-04 Log out"])
        UC5(["UC-05 View dashboard statistics"])
        UC6(["UC-06 View / search / filter students"])
        UC7(["UC-07 View student profile"])
        UC8(["UC-08 Add student"])
        UC9(["UC-09 Edit student"])
        UC10(["UC-10 Delete student"])
        UC11(["UC-11 Change student status"])
        UC12(["UC-12 Export students to CSV"])
        UC13(["UC-13 Print student profile"])
        UC14(["UC-14 Create first administrator (first-time setup)"])
        UCV(["Validate student data"])
    end

    Student --- UC1
    Student --- UC2
    Admin --- UC3
    Admin --- UC4
    Admin --- UC5
    Admin --- UC6
    Admin --- UC7
    Admin --- UC8
    Admin --- UC9
    Admin --- UC10
    Admin --- UC11
    Admin --- UC12
    Admin --- UC13
    Admin -. "first start only" .- UC14

    UC1 -. "include" .-> UCV
    UC8 -. "include" .-> UCV
    UC9 -. "include" .-> UCV
    UC1 -. "extend: on success" .-> UC2
```

## 7. Class diagram

Main classes, inheritance, interfaces and dependencies.

```mermaid
---
title: Class Diagram (main classes)
---
classDiagram
    direction LR

    class Student {
        -int studentId
        -String rollNumber
        -String fullName
        -String email
        -String phone
        -String gender
        -LocalDate dob
        -String department
        -String course
        -int semester
        -String address
        -String status
        -LocalDateTime createdAt
        +Student()
        +Student(rollNumber, fullName, email, phone, gender, dob, department, course, semester, address)
        +getRegistrationId() String
        +getAge() int
        +isActive() boolean
        +getInitials() String
        +getDobFormatted() String
        +getCreatedAtFormatted() String
    }
    class Admin {
        -int adminId
        -String username
        -String passwordHash
    }
    class StudentFilter {
        -String keyword
        -String department
        -Integer semester
        -String status
        +isActive() boolean
    }
    class Serializable {
        <<interface>>
    }
    Serializable <|.. Student
    Serializable <|.. Admin

    class StudentDAO {
        +addStudent(Student) int
        +getStudentById(int) Student
        +searchStudents(StudentFilter, int, int) List~Student~
        +searchStudents(StudentFilter) List~Student~
        +countStudents(StudentFilter) int
        +isRollNumberTaken(String, int) boolean
        +isEmailTaken(String, int) boolean
        +updateStudent(Student) boolean
        +updateStatus(int, String) boolean
        +deleteStudent(int) boolean
        +countAllStudents() int
        +countStudentsByStatus(String) int
        +countDepartments() int
        +getRecentStudents(int) List~Student~
        +getDepartmentCounts() Map~String,Integer~
        -buildWhereClause(StudentFilter, List) String
        -mapRowToStudent(ResultSet) Student
    }
    class AdminDAO {
        +findByUsername(String) Admin
        +createAdmin(String, String) void
        +updatePassword(String, String) boolean
    }
    class DuplicateStudentException {
        -String field
        +getField() String
    }
    class Exception
    Exception <|-- DuplicateStudentException
    StudentDAO ..> DuplicateStudentException : throws
    StudentDAO ..> Student : creates
    StudentDAO ..> StudentFilter : uses
    AdminDAO ..> Admin : creates

    class DBConnection {
        <<utility>>
        +getConnection()$ Connection
        +getConfiguredUrl()$ String
    }
    class ValidationUtil {
        <<utility>>
        +validateStudent(Student)$ Map~String,String~
        +isValidEmail(String)$ boolean
        +isValidPhone(String)$ boolean
        +isValidSemester(int)$ boolean
    }
    class PasswordUtil {
        <<utility>>
        +hashPassword(String)$ String
        +checkPassword(String, String)$ boolean
        +isStrongPassword(String)$ boolean
    }
    class CsrfUtil {
        <<utility>>
        +generateToken()$ String
        +isValid(HttpSession, String)$ boolean
    }
    StudentDAO ..> DBConnection
    AdminDAO ..> DBConnection

    class HttpServlet {
        <<javax.servlet>>
        #doGet(request, response)
        #doPost(request, response)
    }
    class RegisterServlet {
        -StudentDAO studentDAO
        #doGet()
        #doPost()
    }
    class LoginServlet {
        -AdminDAO adminDAO
        #doGet()
        #doPost()
    }
    class StudentListServlet {
        -StudentDAO studentDAO
        #doGet()
    }
    class EditStudentServlet {
        -StudentDAO studentDAO
        #doGet()
        #doPost()
    }
    HttpServlet <|-- RegisterServlet
    HttpServlet <|-- LoginServlet
    HttpServlet <|-- StudentListServlet
    HttpServlet <|-- EditStudentServlet
    note for HttpServlet "9 more servlets extend HttpServlet in the same way:\nRegistrationSuccessServlet, SetupServlet, LogoutServlet,\nDashboardServlet, StudentProfileServlet,\nAddStudentServlet, DeleteStudentServlet,\nStudentStatusServlet, ExportStudentsServlet"

    class StudentRequestHelper {
        <<helper>>
        ~readStudentFromRequest(request)$ Student
        ~validateStudent(Student, StudentDAO, int)$ Map
        ~readFilterFromRequest(request)$ StudentFilter
    }
    RegisterServlet --> StudentDAO
    StudentListServlet --> StudentDAO
    EditStudentServlet --> StudentDAO
    LoginServlet --> AdminDAO
    LoginServlet ..> PasswordUtil
    LoginServlet ..> CsrfUtil
    RegisterServlet ..> StudentRequestHelper
    EditStudentServlet ..> StudentRequestHelper
    StudentRequestHelper ..> ValidationUtil

    class Filter {
        <<interface>>
        +doFilter(request, response, chain)
    }
    class AuthFilter {
        +doFilter(request, response, chain)
    }
    Filter <|.. AuthFilter
    AuthFilter ..> CsrfUtil
```

## 8. Sequence diagram – Student registration

From GET /register to the success page, with error and race-condition alternatives.

```mermaid
---
title: Sequence Diagram - Student Registration
---
sequenceDiagram
    autonumber
    actor S as Student (Browser)
    participant RS as RegisterServlet
    participant H as StudentRequestHelper
    participant V as ValidationUtil
    participant DAO as StudentDAO
    participant DBC as DBConnection
    participant DB as MySQL
    participant SS as RegistrationSuccessServlet
    participant JSP as JSP views

    S->>RS: GET /register
    RS->>JSP: forward to register.jsp (empty Student, drop-down lists)
    JSP-->>S: registration form
    S->>S: app.js checks required fields (browser side)
    S->>RS: POST /register (form fields)
    RS->>H: readStudentFromRequest(request)
    H-->>RS: Student object (trimmed, roll upper-case, email lower-case)
    RS->>RS: setStatus("Active")
    RS->>H: validateStudent(student, studentDAO, 0)
    H->>V: validateStudent(student)
    V-->>H: format errors (Map)
    H->>DAO: isRollNumberTaken(roll, 0) / isEmailTaken(email, 0)
    DAO->>DB: SELECT COUNT(*) FROM students WHERE roll_number = ? ...
    DB-->>DAO: count
    H-->>RS: errors Map
    alt errors found
        RS->>JSP: forward to register.jsp with student + errors
        JSP-->>S: form again, wrong fields highlighted
    else valid
        RS->>DAO: addStudent(student)
        DAO->>DBC: getConnection()
        DAO->>DB: INSERT INTO students (...) VALUES (?, ?, ...)
        alt UNIQUE constraint violated (rare race)
            DB-->>DAO: SQLIntegrityConstraintViolationException
            DAO-->>RS: DuplicateStudentException(field)
            RS->>JSP: register.jsp with the field error
        else inserted
            DB-->>DAO: generated student_id
            DAO-->>RS: newStudentId
            RS->>DAO: getStudentById(newStudentId)
            DAO->>DB: SELECT ... WHERE student_id = ?
            RS->>RS: session.setAttribute("registeredStudent", student)
            RS-->>S: 302 redirect to /register/success
            S->>SS: GET /register/success
            SS->>SS: take student from session, then remove it
            SS->>JSP: forward to registration-success.jsp
            JSP-->>S: "Registration Successful" + SRS-000NN
        end
    end
```

## 9. Sequence diagram – Admin login and logout

BCrypt check, session and CSRF token creation, filter, logout.

```mermaid
---
title: Sequence Diagram - Admin Login and Logout
---
sequenceDiagram
    autonumber
    actor A as Administrator (Browser)
    participant LS as LoginServlet
    participant ADAO as AdminDAO
    participant DB as MySQL
    participant PU as PasswordUtil (BCrypt)
    participant Session as HttpSession
    participant AF as AuthFilter
    participant DS as DashboardServlet
    participant LO as LogoutServlet

    A->>LS: GET /login
    LS-->>A: login.jsp
    A->>LS: POST /login (username, password)
    LS->>ADAO: findByUsername(username)
    ADAO->>DB: SELECT admin_id, username, password_hash FROM admin WHERE username = ?
    DB-->>ADAO: row or nothing
    ADAO-->>LS: Admin or null
    LS->>PU: checkPassword(password, admin.passwordHash)
    PU-->>LS: true / false
    alt admin not found or wrong password
        LS-->>A: login.jsp + "Invalid username or password."
    else correct
        LS->>Session: invalidate old session, create new one (30 min timeout)
        LS->>Session: setAttribute("loggedInAdmin", admin)
        LS->>Session: setAttribute("csrfToken", random token)
        LS-->>A: 302 redirect to /admin/dashboard + JSESSIONID cookie
        A->>AF: GET /admin/dashboard (cookie)
        AF->>Session: loggedInAdmin present?
        AF->>DS: chain.doFilter()
        DS-->>A: dashboard.jsp
    end
    A->>AF: POST /admin/logout (csrfToken)
    AF->>Session: token matches?
    AF->>LO: chain.doFilter()
    LO->>Session: invalidate()
    LO-->>A: 302 redirect to /login?logout=true
```

## 10. Sequence diagram – Student management

Search, edit and delete.

```mermaid
---
title: Sequence Diagram - Student Management (search, edit, delete)
---
sequenceDiagram
    autonumber
    actor A as Administrator (Browser)
    participant AF as AuthFilter
    participant LS as StudentListServlet
    participant ES as EditStudentServlet
    participant DS as DeleteStudentServlet
    participant DAO as StudentDAO
    participant DB as MySQL

    Note over A,DB: 1. Search and filter
    A->>AF: GET /admin/students?keyword=mehta&department=...
    AF->>LS: logged in - continue
    LS->>DAO: countStudents(filter), searchStudents(filter, offset, 10)
    DAO->>DB: SELECT ... WHERE (full_name LIKE ? OR roll_number LIKE ? OR email LIKE ?) AND department = ?
    DB-->>DAO: rows
    DAO-->>LS: List of Student
    LS-->>A: students.jsp (table + paging)

    Note over A,DB: 2. Edit
    A->>AF: GET /admin/students/edit?id=5
    AF->>ES: continue
    ES->>DAO: getStudentById(5)
    ES-->>A: student-form.jsp pre-filled
    A->>AF: POST /admin/students/edit (fields, id, csrfToken)
    AF->>AF: CSRF token valid?
    AF->>ES: continue
    ES->>DAO: validate + isRollNumberTaken(roll, 5) / isEmailTaken(email, 5)
    ES->>DAO: updateStudent(student)
    DAO->>DB: UPDATE students SET ... WHERE student_id = ?
    ES-->>A: 302 to /admin/students/view?id=5 + flash "Changes saved"

    Note over A,DB: 3. Delete (after confirmation dialog in app.js)
    A->>AF: POST /admin/students/delete (id, csrfToken)
    AF->>DS: continue
    DS->>DAO: getStudentById(id)
    DS->>DAO: deleteStudent(id)
    DAO->>DB: DELETE FROM students WHERE student_id = ?
    DS-->>A: 302 to /admin/students + flash "Student ... was deleted."
```

## 11. Activity diagram – Student registration

Decision points of the registration process.

```mermaid
---
title: Activity Diagram - Student Registration
---
flowchart TD
    Start(["Start"]) --> Open["Student opens /register"]
    Open --> Fill["Fill in personal, academic and contact details"]
    Fill --> Submit["Click Submit Registration"]
    Submit --> BrowserCheck{"Browser checks pass?<br/>(required, email, pattern)"}
    BrowserCheck -- "No" --> ShowHint["Highlight fields in red"] --> Fill
    BrowserCheck -- "Yes" --> Post["POST /register to RegisterServlet"]
    Post --> Read["Read and clean form fields<br/>(trim, roll upper-case, email lower-case)"]
    Read --> ServerCheck{"ValidationUtil:<br/>all fields valid?"}
    ServerCheck -- "No" --> Errors["Return form with error messages"] --> Fill
    ServerCheck -- "Yes" --> Dup{"Roll number or email<br/>already in MySQL?"}
    Dup -- "Yes" --> DupErr["Show 'already registered' message"] --> Fill
    Dup -- "No" --> Insert["INSERT INTO students<br/>(PreparedStatement)"]
    Insert --> Constraint{"UNIQUE / CHECK<br/>constraints OK?"}
    Constraint -- "No" --> DupErr
    Constraint -- "Yes" --> Id["MySQL generates student_id"]
    Id --> Redirect["Redirect to /register/success"]
    Redirect --> Show["Show Registration ID SRS-000NN"]
    Show --> End(["End"])
```

## 12. Activity diagram – Admin management

Login loop and all administrative actions.

```mermaid
---
title: Activity Diagram - Admin Management
---
flowchart TD
    Start(["Start"]) --> Login["Open /login and enter username + password"]
    Login --> Check{"BCrypt password<br/>matches?"}
    Check -- "No" --> LoginErr["Show 'Invalid username or password'"] --> Login
    Check -- "Yes" --> Session["Create new session + CSRF token"]
    Session --> Dash["View dashboard statistics"]
    Dash --> Choice{"Choose an action"}
    Choice -- "Find students" --> Search["Search by name / roll / email,<br/>filter by department, semester, status"]
    Search --> Results{"Any results?"}
    Results -- "No" --> Empty["Show empty state"] --> Choice
    Results -- "Yes" --> Profile["Open student profile"]
    Profile --> Action{"Action on student"}
    Action -- "Edit" --> EditForm["Change details in form"] --> Valid{"Valid and not<br/>duplicate?"}
    Valid -- "No" --> EditForm
    Valid -- "Yes" --> Update["UPDATE students"] --> Flash["Show success message"]
    Action -- "Change status" --> Status["UPDATE status"] --> Flash
    Action -- "Delete" --> Confirm{"Confirm in dialog?"}
    Confirm -- "Cancel" --> Profile
    Confirm -- "Yes" --> Delete["DELETE FROM students"] --> Flash
    Action -- "Print" --> Print["Browser print view"] --> Choice
    Choice -- "Add student" --> AddForm["Fill student form"] --> Valid
    Choice -- "Export CSV" --> Csv["Download students-YYYY-MM-DD.csv"] --> Choice
    Flash --> Choice
    Choice -- "Log out" --> Logout["Invalidate session"] --> End(["End"])
```

## 13. Entity relationship diagram

Tables students and admin with keys and constraints.

```mermaid
---
title: Entity Relationship Diagram - student_registration_db
---
erDiagram
    STUDENTS {
        INT student_id PK "AUTO_INCREMENT"
        VARCHAR(20) roll_number UK "NOT NULL"
        VARCHAR(100) full_name "NOT NULL"
        VARCHAR(100) email UK "NOT NULL"
        VARCHAR(15) phone "NOT NULL"
        VARCHAR(10) gender "CHECK Male/Female/Other"
        DATE dob "NOT NULL"
        VARCHAR(50) department "INDEX"
        VARCHAR(50) course "NOT NULL"
        INT semester "CHECK 1-8, INDEX"
        TEXT address "NOT NULL"
        VARCHAR(20) status "DEFAULT Active, CHECK, INDEX"
        TIMESTAMP created_at "DEFAULT CURRENT_TIMESTAMP, INDEX"
    }
    ADMIN {
        INT admin_id PK "AUTO_INCREMENT"
        VARCHAR(50) username UK "NOT NULL"
        VARCHAR(255) password_hash "BCrypt hash"
        TIMESTAMP created_at "DEFAULT CURRENT_TIMESTAMP"
    }
```

## 14. DFD Level 0 – Context diagram

The system as one process with its two external entities.

```mermaid
---
title: DFD Level 0 - Context Diagram
---
flowchart LR
    Student["STUDENT<br/>(external entity)"]
    Admin["ADMINISTRATOR<br/>(external entity)"]
    System(("0.0<br/>Student<br/>Registration<br/>System"))

    Student -- "Registration details" --> System
    System -- "Registration ID /<br/>validation errors" --> Student
    Admin -- "Login credentials,<br/>search criteria,<br/>student updates,<br/>delete / status requests" --> System
    System -- "Dashboard statistics,<br/>student lists and profiles,<br/>CSV file,<br/>confirmation messages" --> Admin
```

## 15. DFD Level 1 – Main processes

Six processes and three data stores.

```mermaid
---
title: DFD Level 1 - Main System Processes
---
flowchart TB
    Student["STUDENT"]
    Admin["ADMINISTRATOR"]

    P1(("1.0<br/>Register<br/>Student"))
    P2(("2.0<br/>Authenticate<br/>Admin"))
    P3(("3.0<br/>Search and<br/>View Students"))
    P4(("4.0<br/>Manage Student<br/>Records"))
    P5(("5.0<br/>Generate<br/>Statistics"))
    P6(("6.0<br/>Export<br/>CSV"))

    D1[("D1 students")]
    D2[("D2 admin")]
    D3[("D3 HTTP session")]

    Student -- "student details" --> P1
    P1 -- "registration ID / errors" --> Student
    P1 -- "check duplicates" --> D1
    P1 -- "new student row" --> D1

    Admin -- "username, password" --> P2
    D2 -- "password hash" --> P2
    P2 -- "admin + CSRF token" --> D3
    P2 -- "login result" --> Admin

    Admin -- "keyword, filters, page" --> P3
    D3 -- "session check" --> P3
    D1 -- "matching rows" --> P3
    P3 -- "student list / profile" --> Admin

    Admin -- "add / edit / status / delete" --> P4
    D3 -- "session + token check" --> P4
    P4 -- "INSERT / UPDATE / DELETE" --> D1
    P4 -- "success / error message" --> Admin

    D1 -- "counts, GROUP BY department,<br/>recent rows" --> P5
    P5 -- "dashboard figures" --> Admin

    Admin -- "filters" --> P6
    D1 -- "matching rows" --> P6
    P6 -- "students-DATE.csv" --> Admin
```
