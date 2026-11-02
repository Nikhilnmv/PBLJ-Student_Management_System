# Student Registration System

A web-based student registration and management system built with **Java Servlets, JSP, JDBC and
MySQL** on **Apache Tomcat 9**. Academic minor project for *Project Based Learning Using Java (PBLJ)*.

> **New here? Read [docs/START_HERE.md](docs/START_HERE.md)** — what the project is, what is done,
> and how to run it on your laptop with MySQL Workbench in a few steps.
>
> 🎬 **Demo video (1½ min):** [docs/demo/student-registration-demo.mp4](docs/demo/student-registration-demo.mp4)

Students register online and instantly receive a registration ID. Administrators log in to a secure
panel to see live statistics and to search, filter, view, add, edit, delete, activate/deactivate,
print and export student records.

![Admin dashboard](docs/screenshots/dashboard.png)

---

## Features

**Students (public)**
- Registration form: name, roll number, email, phone, gender, date of birth, department, course, semester, address
- Validation in the browser **and** on the server (email, phone, semester 1–8, age 15–60, lists …)
- Duplicate roll numbers and emails rejected (Java check + MySQL UNIQUE constraints)
- Automatically generated registration ID (e.g. `SRS-00017`) and printable confirmation

**Administrators**
- Secure login (BCrypt-hashed passwords, session-based, 30-minute timeout, logout); first-time setup page creates the first admin
- Dashboard: total, active, inactive, departments, recent registrations, students per department — all from live SQL
- Student list with paging, search (name / roll number / email) and filters (department / semester / status)
- Profile, add, edit, delete (with confirmation), Active/Inactive status, CSV export, print-friendly profile
- Success/error notifications, empty states, responsive layout, works offline (no CDN)

**Security:** PreparedStatement everywhere · server-side validation · BCrypt · `AuthFilter` on `/admin/*` ·
CSRF tokens · POST-only changes · output escaping (XSS) · HttpOnly/SameSite cookies · anti-clickjacking
header · friendly error pages · no secrets in Git.

## Screenshots

| Registration | Registration success |
|---|---|
| ![Registration form](docs/screenshots/register.png) | ![Registration success](docs/screenshots/registration-success.png) |
| **Admin login** | **All students (search & filters)** |
| ![Login](docs/screenshots/login.png) | ![Student list](docs/screenshots/students.png) |
| **Student profile** | **Mobile view** |
| ![Profile](docs/screenshots/profile.png) | ![Mobile](docs/screenshots/mobile.png) |

<sub>Screenshots captured from this application during development testing, using the fictional sample data.</sub>

## Technology stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Web layer | Java Servlets 4.0 (`javax.servlet`), JSP 2.3, JSTL 1.2 |
| Frontend | HTML, CSS, JavaScript, Bootstrap 5.3, Bootstrap Icons (bundled locally) |
| Database | MySQL 8 via JDBC (MySQL Connector/J 8.4) |
| Password hashing | jBCrypt 0.4 |
| Server | Apache Tomcat 9 |
| Build | Maven (WAR packaging) |
| Tests | JUnit 5, Bash + curl functional test script |
| Architecture | MVC + DAO |

## Quick start

Step-by-step with pictures: **[docs/START_HERE.md](docs/START_HERE.md)**. In short:

1. Install **Java 17** and **MySQL Server 8** (MySQL Workbench is optional but handy).
   Maven and Tomcat are **not** needed — they are downloaded automatically.
2. Download this repository (Code → Download ZIP) and extract it.
3. In MySQL Workbench run `database/schema.sql`, then `database/sample_data.sql`.
4. Start the app: **Windows** double-click `start.bat` · **macOS/Linux** run `./start.sh`.
   The first time it asks for your MySQL username and password (stored only in the git-ignored
   `src/main/resources/db.properties`), then opens <http://localhost:8080/student-registration/>.
5. Click **Admin Login** — the first time you are asked to create the administrator account.

Other ways to run it (Tomcat installed by hand, IntelliJ, Eclipse): [docs/SETUP_AND_EXECUTION.md](docs/SETUP_AND_EXECUTION.md).

## Project structure

```
├── start.bat / start.sh            one-click start (Windows / macOS, Linux)
├── mvnw, mvnw.cmd                  Maven Wrapper (downloads Maven automatically)
├── pom.xml                         Maven build (dependencies, Java 17, WAR, Tomcat via Cargo)
├── database/                       schema.sql, sample_data.sql (fictional data)
├── scripts/functional-test.sh      69 automated HTTP checks (curl)
├── src/main/java/com/student/
│   ├── model/                      Student, Admin, StudentFilter            (Model)
│   ├── dao/                        StudentDAO, AdminDAO, DuplicateStudentException (DAO)
│   ├── servlet/                    13 servlets + StudentRequestHelper       (Controller)
│   ├── filter/                     AuthFilter (protects /admin/*)
│   ├── listener/                   AppContextListener (start-up DB check)
│   └── util/                       DBConnection, ValidationUtil, PasswordUtil, CsrfUtil,
│                                   CsvUtil, AppConstants, AdminSetup
├── src/main/resources/             db.properties.example
├── src/main/webapp/
│   ├── index.jsp, assets/          home page, CSS, JS, Bootstrap, icons, font
│   └── WEB-INF/                    web.xml, views/ (JSP pages and fragments)  (View)
├── src/test/java/                  JUnit unit + integration tests
└── docs/                           documentation, diagrams (Mermaid + PNG/SVG)
```

## Testing

```bash
./mvnw test                           # 34 unit tests (Windows: mvnw.cmd test)
./mvnw test -Dintegration=true        # + 15 integration tests against MySQL
ADMIN_PASSWORD='your-admin-password' ./scripts/functional-test.sh   # 69 HTTP checks (app must be running)
```
Results, including performance with 10,000 students: [docs/TEST_RESULTS.md](docs/TEST_RESULTS.md).

## Documentation

| Document | Contents |
|---|---|
| [START_HERE](docs/START_HERE.md) | **Begin here:** what is done, run it in a few steps, check it works |
| [SETUP_AND_EXECUTION](docs/SETUP_AND_EXECUTION.md) | Detailed installation, other ways to run, troubleshooting |
| [PROJECT_OVERVIEW](docs/PROJECT_OVERVIEW.md) | Problem, objectives, scope, features, limitations, future scope |
| [COMPLETE_PROJECT_GUIDE](docs/COMPLETE_PROJECT_GUIDE.md) | **Main learning guide** — how everything works |
| [CODE_WALKTHROUGH](docs/CODE_WALKTHROUGH.md) | File-by-file explanation and traced flows |
| [JAVA_CONCEPTS_USED](docs/JAVA_CONCEPTS_USED.md) | 28 Java concepts with real code from this project |
| [DATABASE_EXPLANATION](docs/DATABASE_EXPLANATION.md) | Tables, constraints, every SQL query, JDBC |
| [DATABASE_DESIGN](docs/DATABASE_DESIGN.md) | ER diagram, data dictionary, design rationale |
| [ARCHITECTURE](docs/ARCHITECTURE.md) | Architecture, MVC, component, deployment, request lifecycle |
| [SRS](docs/SRS.md) | Software requirements specification |
| [USE_CASES](docs/USE_CASES.md) | Actors, use case diagram and descriptions |
| [TEST_PLAN](docs/TEST_PLAN.md) · [TEST_CASES](docs/TEST_CASES.md) · [TEST_RESULTS](docs/TEST_RESULTS.md) · [TRACEABILITY_MATRIX](docs/TRACEABILITY_MATRIX.md) | Testing |
| [DESIGN_DECISIONS](docs/DESIGN_DECISIONS.md) | Why each technology/design was chosen, with trade-offs |
| [DEVELOPMENT_PROCESS](docs/DEVELOPMENT_PROCESS.md) | How the project was developed |
| [VIVA_PREPARATION](docs/VIVA_PREPARATION.md) | Introductions, Q&A, demo script, quick revision |
| [PROJECT_REPORT_MATERIAL](docs/PROJECT_REPORT_MATERIAL.md) · [REPORT_SCREENSHOTS_CHECKLIST](docs/REPORT_SCREENSHOTS_CHECKLIST.md) | Material for the final report |
| [diagrams/](docs/diagrams/README.md) | 15 UML, ER, DFD and architecture diagrams |

## Security note
This project is designed to run locally. Never commit `db.properties` or any password. For deployment
on a real network, HTTPS and the improvements listed in [PROJECT_OVERVIEW.md](docs/PROJECT_OVERVIEW.md#10-future-enhancements)
would be required.

## Credits
Bootstrap and Bootstrap Icons (MIT License) and the Inter font (SIL Open Font License) are included in
`src/main/webapp/assets/vendor/` with their licence files.
