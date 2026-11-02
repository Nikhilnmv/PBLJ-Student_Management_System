# Test Results

All results below come from tests that were **actually executed**. Tests that have not been run are
listed separately in [§6 Pending tests](#6-pending-tests) and are **not** counted as passed.

## 1. Summary

| Test type | Executed | Passed | Failed | Pending | Evidence |
|---|---|---|---|---|---|
| Unit tests (JUnit 5) | 34 | 34 | 0 | 0 | §8 |
| Integration tests (JUnit 5 + MySQL) | 15 | 15 | 0 | 0 | §8 |
| HTTP functional checks (`functional-test.sh`) | 69 | 69 | 0 | 0 | §8 |
| Test cases in [TEST_CASES.md](TEST_CASES.md) (89 in total) | 81 | 81 | 0 | 8 | TEST_CASES.md |

Sections 3–7 record the earlier runs (at that time 28 unit tests and 67 HTTP checks). Section 8
records the latest run, after the first-time setup page, database hints and start scripts were added.

Final verification run (clean build, fresh deployment): see [§7](#7-final-verification-run).

## 2. Test environment (executed)

| Item | Value |
|---|---|
| Date | 02 November 2026 |
| OS | Ubuntu 24.04 LTS (cloud development container) |
| Java | OpenJDK 17.0.20 (build with `--release 17`) |
| Maven | 3.9.11 |
| Application server | Apache Tomcat 9.0.111 |
| Database | MySQL Community Server 8.0.46, app user `srs_app` (SELECT/INSERT/UPDATE/DELETE only) |
| Browser | Chromium (driven automatically for UI checks) |
| Data | 16 sample students + 1 manually registered test student (17 rows) |

> These results prove the application works on this reference environment. Re-run the same tests on
> your own laptop (Windows/macOS) and record your results in the pending rows of TEST_CASES.md.

## 3. Automated test results

### 3.1 Unit tests — `mvn test`
```
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.model.StudentTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.ValidationUtilTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.PasswordUtilTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.CsvUtilTest
[WARNING] Tests run: 15, Failures: 0, Errors: 0, Skipped: 15 -- in com.student.dao.StudentDAOIntegrationTest
[WARNING] Tests run: 43, Failures: 0, Errors: 0, Skipped: 15
[INFO] BUILD SUCCESS
```
The 15 integration tests are **skipped** in a normal build on purpose (they need MySQL).

### 3.2 Integration tests — `mvn test -Dintegration=true`
```
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.model.StudentTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.ValidationUtilTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.PasswordUtilTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.CsvUtilTest
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.dao.StudentDAOIntegrationTest
[INFO] Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
After the run: `SELECT COUNT(*) FROM students WHERE roll_number LIKE 'ITEST%'` → `0` (all test rows cleaned up).

### 3.3 HTTP functional tests — `scripts/functional-test.sh`
```
Testing http://localhost:8080/student-registration (run 41800)

1. Student registration
  PASS  TC-REG-01a  Registration form opens
  PASS  TC-REG-01b  Valid registration redirects to success page
  PASS  TC-REG-01c  Success page shows a registration ID
  PASS  TC-REG-10   Refreshing the success page does not resubmit
  PASS  TC-REG-02a  Empty form is rejected (stays on form)
  PASS  TC-REG-02b  Empty form lists required-field errors
  PASS  TC-REG-03   Invalid email is rejected
  PASS  TC-REG-04   Invalid phone number is rejected
  PASS  TC-REG-05   Duplicate roll number is rejected (case-insensitive)
  PASS  TC-REG-06   Duplicate email is rejected (case-insensitive)
  PASS  TC-REG-07   Semester outside 1-8 is rejected
  PASS  TC-REG-08   Date of birth in the future is rejected
  PASS  TC-REG-12   Department not in the list is rejected

2. Authentication and access control
  PASS  TC-AUTH-05  Dashboard without login redirects to login
  PASS  TC-SEC-04   Protected without login: /admin/students
  PASS  TC-SEC-04   Protected without login: /admin/students/view?id=1
  PASS  TC-SEC-04   Protected without login: /admin/students/add
  PASS  TC-SEC-04   Protected without login: /admin/students/export
  PASS  TC-AUTH-02  Wrong password is rejected
  PASS  TC-AUTH-03  Unknown username gets the same message
  PASS  TC-AUTH-04  Empty login fields are rejected
  PASS  TC-SEC-02a  SQL injection in username does not log in
  PASS  TC-SEC-02b  SQL injection in password does not log in
  PASS  TC-AUTH-01  Correct credentials redirect to dashboard
  PASS  TC-AUTH-08  Session ID changes at login
  PASS  TC-DASH-01a Dashboard opens after login
  PASS  TC-AUTH-07  Admin pages are not cached
  PASS  TC-SEC-09   Anti-clickjacking header present
  PASS  TC-AUTH-10  Login page redirects when already logged in

3. Student management
  PASS  TC-REG-01d  Registered student is stored and searchable by roll number
  PASS  TC-REG-09   Forged 'status=Inactive' was ignored (stored as Active)
  PASS  TC-REG-11   Roll number stored in upper case
  PASS  TC-MGT-01   Student list shows records
  PASS  TC-MGT-02   Search by name
  PASS  TC-MGT-03   Search by roll number
  PASS  TC-MGT-04   Search by email
  PASS  TC-MGT-08   Combined keyword + department + semester filter
  PASS  TC-MGT-06   Semester filter excludes non-matching students
  PASS  TC-MGT-05   Department filter excludes non-matching students
  PASS  TC-MGT-07   Status filter excludes non-matching students
  PASS  TC-SEC-03   SQL injection in search returns no rows
  PASS  TC-MGT-09   Profile shows the student's registration ID
  PASS  TC-MGT-10   Profile with unknown ID gives 404
  PASS  TC-SEC-10   Error page shows no Java stack trace
  PASS  TC-MGT-11   Admin can add a student
  PASS  TC-SEC-06a  Add without CSRF token is refused
  PASS  TC-MGT-12a  Edit saves and returns to profile
  PASS  TC-MGT-12b  Edited name is shown
  PASS  TC-MGT-12c  Edited semester is shown
  PASS  TC-MGT-13   Edit to another student's roll number is rejected
  PASS  TC-MGT-14   Status changed to Inactive
  PASS  TC-MGT-17a  CSV is downloaded as a file
  PASS  TC-MGT-17b  CSV contains header + 2 filtered rows
  PASS  TC-MGT-16   Delete via GET is not allowed
  PASS  TC-SEC-06b  Delete without CSRF token is refused
  PASS  TC-MGT-15a  Delete with token redirects to list
  PASS  TC-MGT-15b  Deleted student no longer exists

4. Security: SQL injection and XSS in registration
  PASS  TC-SEC-01a  SQL in the name field is rejected by validation
  PASS  TC-SEC-01b  SQL text in address is stored as plain text (table not dropped)
  PASS  TC-SEC-07a  HTML in address is displayed escaped
  PASS  TC-SEC-07b  No executable <script> from user input
  PASS  TC-SEC-08a  First submission is saved
  PASS  TC-SEC-08b  Identical second submission is rejected as duplicate

5. Logout
  PASS  TC-SEC-06c  Logout without CSRF token is refused
  PASS  TC-DASH-02  Dashboard total drops by 3 after deleting test students
  PASS  TC-AUTH-06a Logout redirects to login page
  PASS  TC-AUTH-06b Old session cannot open the dashboard after logout

Result: 67 passed, 0 failed
```
Database rows before the run: 17 · after the run: 17 · leftover `FT…` rows: 0.

## 4. Manual and browser test results (highlights)

| Test | Result |
|---|---|
| Admin setup tool (TC-AUTH-12) | Weak password and mismatch rejected; admin created with a 60-char `$2a$12$` hash; re-run without changes when answering "no". |
| Dashboard vs SQL (TC-DASH-01, 03) | Cards 17 / 15 / 2 / 7 and all 7 department counts identical to direct SQL queries. |
| Database stopped (TC-SEC-11) | Home and registration form still load; submitting → friendly 500 page; login → "The database is not available right now."; no stack traces; Tomcat log contains the details ("Communications link failure"). After restarting MySQL the dashboard worked again without restarting Tomcat. |
| Browser validation, double submit (TC-REG-13, TC-SEC-08) | Empty submit blocked in the browser; two rapid clicks → exactly 1 submission; button disabled showing "Submitting…". |
| Confirmation dialog (TC-MGT-20) | Cancel kept the record; Delete removed it and showed "Student Browser Test Student (24IT-988) was deleted." |
| Responsive layout (TC-UI-01) | 8 pages at 390 px width: no horizontal overflow. Dashboard and student list at 360, 390, 576, 768, 992, 1024, 1200, 1280, 1366, 1440 and 1920 px: no page or table overflow (after fixing D-10). Slide-in admin menu works. |
| Offline (TC-UI-02) | No page references any internet resource. |
| Performance (TC-PERF-01) | 10,017 students: dashboard 38 ms, list 18 ms, page 500 34 ms, search 41–42 ms, filters 17 ms, profile 7 ms; CSV export of 10,018 lines 0.35 s. `EXPLAIN` confirmed index use (`idx_students_semester`, backward scan on `idx_students_created_at`). Temporary rows deleted afterwards. |
| Secrets (TC-SEC-12) | `db.properties` ignored by Git; database and admin passwords found 0 times in the full Git history. |

## 5. Defects found and fixed

| ID | Found by | Defect | Fix | Re-tested |
|---|---|---|---|---|
| D-01 | Re-running `schema.sql` | Script failed on second run: MySQL has no `CREATE INDEX IF NOT EXISTS` | Indexes moved inside `CREATE TABLE IF NOT EXISTS` | Ran schema twice — OK |
| D-02 | Visual review | Cards looked grey: Bootstrap 5.3 uses `--bs-body-bg` for card backgrounds | `--bs-body-bg: #fff`; page background set on `body` | Screenshots — OK |
| D-03 | Code review | `student-form.jsp` used EL `concat()` with a number for the Cancel link (would fail at runtime) | Replaced by `<c:set>` | Edit/Add pages load — OK |
| D-04 | Code review | The admin top bar showed a static "MySQL connected" label that was not a real check | Removed (no fake status indicators) | — |
| D-05 | Visual review | Filter bar: "Apply" button squeezed when the clear (×) button appeared | Column widths rebalanced | Screenshot — OK |
| D-06 | Print-media rendering | Printed profile included the navy sidebar stripe and grey background | `@media print` rules for the admin layout | Print view/PDF — OK |
| D-07 | Visual review | Success page buttons wrapped onto two lines | `text-nowrap`, shorter label | Screenshot — OK |
| D-08 | Unit test design | Date format depended on the computer's language (`MMM`, `a`) | `DateTimeFormatter…(…, Locale.ENGLISH)` | `StudentTest` — OK |
| D-09 | Code review | CSV servlet contained an invisible literal BOM character in the source | Replaced with the readable escape `'\uFEFF'` | CSV still opens as UTF-8 — OK |
| D-10 | Final verification (screenshots at 1280 px, then automated width measurement) | On 360–1024 px and 1200–1280 px screens the student table (and the dashboard table at 1280 px) was wider than its card, so the Actions/Status columns were cut off and needed sideways scrolling | Admin sidebar becomes a slide-in menu below 1200 px; less important columns hidden on small screens; long names/emails truncated with "…" (full text on hover); filter bar wraps to two rows below 1400 px | No overflow at 11 widths; all tests re-run — OK |

Two defects in the **test script itself** (not the application) were also fixed: overriding a form
field by appending a second value (the servlet reads the first value), and counting an already
deleted student during clean-up.

## 6. Pending tests

These have **not** been executed. They are not counted as passed.

| ID | Test | Why pending | How to run |
|---|---|---|---|
| TC-AUTH-11 | Session timeout after 30 min | Needs a 30-minute idle wait | Log in, wait 31 minutes, click a menu item → expect the login page |
| TC-DASH-05 | Dashboard with an empty database | Needs the `students` table emptied | `DELETE FROM students;` on a test copy, open the dashboard |
| TC-UI-03 | Firefox / Edge / Safari | Only Chromium available during development | Open each page in each browser |
| TC-ENV-01 | Setup on Windows | Needs your laptop | Follow SETUP_AND_EXECUTION.md |
| TC-ENV-02 | Setup on macOS | Needs a Mac | Follow SETUP_AND_EXECUTION.md |
| TC-ENV-03 | Run from IntelliJ / Eclipse | Needs the IDE | SETUP_AND_EXECUTION.md §7 options B/C |
| TC-ENV-04 | View rows in MySQL Workbench | Needs Workbench | Register a student; refresh `students` in Workbench |
| — | Printing on paper | Needs a printer | Print a profile and the success page |

## 7. Final verification run

Performed on 02 November 2026 after all code and documentation were finished, following
[SETUP_AND_EXECUTION.md](SETUP_AND_EXECUTION.md) step by step, as a new user would:

1. **Fresh clone** of the GitHub repository into an empty folder → no `db.properties`
   present (only the example) → `mvn clean package` → `BUILD SUCCESS`, WAR with 28 classes.
2. **Database recreated from scratch:** `DROP DATABASE`, then `schema.sql` and `sample_data.sql`
   → 2 tables, 16 students, 0 admins; `srs_app` user re-granted.
3. **Configuration:** `db.properties` created from `db.properties.example`; `git status` shows it is ignored.
4. **Admin account:** created with `mvn compile exec:java` → "SUCCESS: administrator 'admin' created."
5. **Deployment** to a clean Tomcat 9.0.111 on Java 17 → `localhost.<date>.log`:
   `Student Registration System: connected to MySQL (8.0.46…)`; home page HTTP 200.
6. **All automated tests** (re-run once more after fixing D-10), `mvn test -Dintegration=true`:
   ```
   [INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.model.StudentTest
   [INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.ValidationUtilTest
   [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.PasswordUtilTest
   [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.CsvUtilTest
   [INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.dao.StudentDAOIntegrationTest
   [INFO] Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
   [INFO] BUILD SUCCESS
   ```
   `functional-test.sh` (last lines):
   ```
     PASS  TC-AUTH-06a Logout redirects to login page
     PASS  TC-AUTH-06b Old session cannot open the dashboard after logout
   
   Result: 67 passed, 0 failed
   ```
   Students before and after the functional run: 16 → 16 (test data cleaned up).
7. **Browser checks:**
   ```
   1) empty submit blocked in browser: true | invalid fields: 12 | still on /student-registration/register
      gender selected via label: true
   2) roll number auto-uppercased while typing: 24IT-988
   3) after submit URL: /student-registration/register/success | ID shown: SRS-00084
   4) modal message: Delete Browser Test Student (24IT-988)? This permanently removes the record from the database.
   5) cancel keeps record: true
   6) after confirm flash: Student Browser Test Student (24IT-988) was deleted.
   7) empty state: No students match your search
   8) mobile menu opens: true
   JS errors: none
   submissions sent: 1 | button disabled: true | button text: Submitting...
   no overflow at any width
   ```
8. **Repository:** working tree clean and identical to GitHub after the final push; no secrets tracked.

**Verdict:** every executed test passes on a completely fresh setup. The pending items in §6 still
need to be done on your own laptop.

## 8. Re-verification after adding the one-click start and first-time setup page

Date: 02 November 2026. Changes tested: Maven Wrapper (`mvnw`), Tomcat 9 run by the Cargo plugin,
`start.sh` / `start.bat`, first-time setup page `/setup`, beginner-friendly database hints.

1. **A clean copy of the project** with no `db.properties`, and a **brand-new database** created by
   running `schema.sql` and `sample_data.sql` (as you would in MySQL Workbench) → 16 students, 0 admins.
2. **`./start.sh`** asked for the MySQL username and password, saved `db.properties`, built the
   project, started Tomcat 9 and printed `READY: http://localhost:8080/student-registration/`.
3. **First-time setup** in a real browser: `/login` → redirected to `/setup` → admin created →
   "Administrator account created. Please sign in." → login → dashboard. Weak password, mismatched
   passwords and an invalid username were rejected. Afterwards `/setup` redirected to `/login`, and a
   POST trying to create a second admin was refused (admin count stayed 1).
4. **Database hint:** with MySQL stopped, the login page said "Cannot reach the MySQL server on
   localhost:3306. Make sure MySQL is running (…)"; after starting MySQL the page worked again.
5. **Ctrl+C** stopped the application within 5 seconds with no Tomcat process left.
6. **Automated tests** — `mvn test -Dintegration=true`:
   ```
   [INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.model.StudentTest
   [INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.ValidationUtilTest
   [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.DBConnectionTest
   [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.PasswordUtilTest
   [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.util.CsvUtilTest
   [INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0 -- in com.student.dao.StudentDAOIntegrationTest
   [INFO] Tests run: 49, Failures: 0, Errors: 0, Skipped: 0
   [INFO] BUILD SUCCESS
   ```
   `functional-test.sh` against the instance started by `start.sh` (last lines):
   ```
     PASS  TC-AUTH-06b Old session cannot open the dashboard after logout
   
   Result: 69 passed, 0 failed
   ```
7. **Fresh download from GitHub** (commit `a1ce762`): Windows scripts checked out with CRLF line
   endings, `start.sh` executable; brand-new database from the two SQL files; `start.sh` → READY;
   first-time setup → admin created; `functional-test.sh` → `Result: 69 passed, 0 failed`;
   `./mvnw test -Dintegration=true` → all 49 tests pass; Ctrl+C stopped the application. The demo
   video `docs/demo/student-registration-demo.mp4` was recorded from this instance.
8. **Not executed:** `start.bat` and the PowerShell helper scripts (no Windows in the development
   environment) — listed as TC-ENV-06 (pending). If they fail on your PC, use the manual steps in
   [START_HERE.md](START_HERE.md#if-something-goes-wrong).
