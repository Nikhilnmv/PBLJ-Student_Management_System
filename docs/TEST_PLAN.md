# Test Plan

## 1. Objective
Verify that the Student Registration System meets the requirements in [SRS.md](SRS.md):
registration, validation, authentication, dashboard, student management, CSV export, security
protections, error handling, usability and performance — and record honest results.

## 2. Scope

**In scope:** all functional requirements FR-01 to FR-30 and non-functional requirements
NFR-01 to NFR-12, on Java 17 + Tomcat 9 + MySQL 8.

**Out of scope:** load testing with many simultaneous users, penetration testing by a security
professional, older browsers (Internet Explorer), deployment on a public server.

## 3. Test levels and types

| Level / type | What is tested | Tool | How to run | Automated |
|---|---|---|---|---|
| Unit tests | `ValidationUtil`, `PasswordUtil`, `CsvUtil`, `Student`, `StudentFilter`, `CsrfUtil` | JUnit 5 | `mvn test` | Yes (28 tests) |
| Integration tests | `StudentDAO`, `AdminDAO` against real MySQL (CRUD, constraints, search, paging, statistics, SQL injection) | JUnit 5 + MySQL | `mvn test -Dintegration=true` | Yes (15 tests) |
| Functional / system tests | Every page and action through real HTTP requests to the deployed application | Bash + curl | `ADMIN_PASSWORD=… ./scripts/functional-test.sh` | Yes (67 checks) |
| Security tests | SQL injection, XSS, CSRF, access control, session handling, headers, secrets | curl script + manual | as above + manual steps in TEST_CASES | Mostly |
| Browser / UI tests | Client-side validation, double-submit guard, confirm dialog, mobile layout, print layout | Real browser (Chromium, automated during development) + manual | Manual steps in TEST_CASES | Partly |
| Error-handling tests | Behaviour with MySQL stopped and restarted | Manual (stop/start MySQL) | TEST_CASES TC-SEC-11 | No |
| Performance check | Response times with 10,000 students | curl timing + `EXPLAIN` | TEST_CASES TC-PERF-01 | No |
| Setup tests | First-time setup page (`/setup`) and `AdminSetup` tool: create, reject weak password / mismatch, page closed once an admin exists | Browser, curl script, terminal | `/setup`; `./mvnw compile exec:java` | Partly |
| Start-up tests | `start.sh` / `start.bat`: first-run questions, build, start, browser opens, Ctrl+C stops | Terminal | `./start.sh` / `start.bat` | No |

Why JUnit only for code that needs no server: utility and validation logic can be tested quickly
and repeatedly in isolation. Code that depends on MySQL or Tomcat is covered by the integration
tests and the HTTP functional script, which test the real components instead of fakes.

## 4. Test environment

| Item | Development test environment (executed) | Student laptop (to be executed) |
|---|---|---|
| OS | Ubuntu 24.04 (cloud development container) | Windows 10/11 or macOS |
| Java | OpenJDK 17.0.20 | JDK 17 |
| Maven | 3.9.11 | 3.8+ |
| MySQL | 8.0.46 | 8.0.x |
| Tomcat | 9.0.111 | 9.0.x |
| Browser | Chromium (automated) | Chrome / Edge / Firefox |
| Data | `sample_data.sql` (16 fictional students) + test records | same |

## 5. Test data
- 16 fictional students from `database/sample_data.sql` (all `@example.com`).
- Integration tests create rows with roll numbers starting `ITEST` and admins starting `itest_`, and delete them after each test.
- The functional script creates rows starting `FT<run-id>` and deletes them at the end.
- The performance check temporarily inserted 10,000 rows starting `PERF` and deleted them afterwards.
- Security inputs are harmless strings such as `' OR '1'='1`, `Robert'); DROP TABLE students;--` and `<script>alert('xss')</script>`.

## 6. Entry criteria
- Code compiles (`mvn clean package` succeeds).
- Database created from `schema.sql`, sample data loaded, admin account created.
- Application deployed on Tomcat and the start-up log shows "connected to MySQL".

## 7. Exit criteria
- All unit and integration tests pass.
- All functional script checks pass.
- All executed manual test cases pass, or failures are fixed and re-tested.
- No open defect that causes wrong data, a security hole, or a crash.
- Tests that could not be executed are listed as **pending** in [TEST_RESULTS.md](TEST_RESULTS.md) — never marked as passed.

## 8. Defect handling
Defects found during testing are fixed, the related test is re-run, and the defect is recorded in
[TEST_RESULTS.md](TEST_RESULTS.md#5-defects-found-and-fixed).

## 9. Deliverables
- [TEST_CASES.md](TEST_CASES.md) — every test case with input, expected result, actual result and status.
- [TEST_RESULTS.md](TEST_RESULTS.md) — summary of each test run, raw outputs, defects, pending tests.
- [TRACEABILITY_MATRIX.md](TRACEABILITY_MATRIX.md) — requirement → code → test mapping.
- Automated tests in `src/test/java` and `scripts/functional-test.sh`.

## 10. How to re-run everything on your laptop
```bash
mvn clean package                                   # build + 34 unit tests
mvn test -Dintegration=true                         # + 15 MySQL integration tests
# deploy target/student-registration.war to Tomcat 9 and start it, then:
ADMIN_PASSWORD='your-admin-password' ./scripts/functional-test.sh   # 69 HTTP checks
```
Then follow the manual cases marked **Manual** in [TEST_CASES.md](TEST_CASES.md) and fill in the
"Actual result" column for your own machine.
