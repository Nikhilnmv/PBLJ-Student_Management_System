# Test Cases

Every test case with its input, expected result, **actual result** and status.

**Status values**
- **Pass** — executed on 02 Nov 2026 in the development test environment (Ubuntu 24.04, OpenJDK 17.0.20,
  Tomcat 9.0.111, MySQL 8.0.46) and the actual result matched the expected result.
- **Pending** — not executed yet (for example, needs your own Windows/macOS laptop). The actual-result
  column is left empty on purpose. **Do not mark these as passed until you have run them.**

**Method** (in Remarks): *Script* = `scripts/functional-test.sh`; *JUnit* = `mvn test`;
*JUnit-IT* = `mvn test -Dintegration=true`; *Manual* = done by hand (curl, terminal or MySQL);
*Browser* = performed in a real (automated) Chromium browser.

**Common preconditions:** **P1** = Tomcat running with the WAR deployed, MySQL running, schema and
sample data loaded. **P2** = P1 + an admin account exists and the tester is logged in as admin.

---

## 1. Student registration (TC-REG)

| ID | Feature | Pre | Input | Expected result | Actual result | Status | Remarks |
|---|---|---|---|---|---|---|---|
| TC-REG-01 | Valid registration | P1 | All fields valid, roll `FT<run>A`, email `ft.<run>.a@example.com` | Redirect (302) to `/register/success`; page shows "Registration Successful!" and an ID `SRS-000NN`; row stored in MySQL | 302 → `/register/success`; ID shown (e.g. `SRS-00033`); row found by admin search and in MySQL | Pass | Script 01a–01d; also checked with `SELECT` |
| TC-REG-02 | Missing fields | P1 | Empty form | Form shown again (200) with a message under every required field; nothing saved | 10 fields marked invalid, e.g. "Full name is required."; 0 rows inserted | Pass | Script 02a/02b; Manual |
| TC-REG-03 | Invalid email | P1 | email = `not-an-email` | "Please enter a valid email address, e.g. name@example.com." | Message shown; not saved | Pass | Script |
| TC-REG-04 | Invalid phone | P1 | phone = `12345` | "Phone number must be 10 digits and start with 6, 7, 8 or 9." | Message shown; not saved | Pass | Script |
| TC-REG-05 | Duplicate roll number | P1 + TC-REG-01 done | same roll number in lower case `ft<run>a` | "This roll number is already registered." | Message shown; not saved | Pass | Script; proves case-insensitive check |
| TC-REG-06 | Duplicate email | P1 + TC-REG-01 done | same email in upper case | "This email address is already registered." | Message shown; not saved | Pass | Script |
| TC-REG-07 | Semester out of range | P1 | semester = `9` | "Semester must be between 1 and 8." | Message shown; not saved | Pass | Script |
| TC-REG-08 | Invalid date of birth | P1 | dob = `2099-01-01` | "Age must be between 15 and 60 years." | Message shown; not saved | Pass | Script |
| TC-REG-09 | Forged status | P1 | valid data + extra field `status=Inactive` | Student stored as **Active** | Profile shows Active badge | Pass | Script; status is forced in `RegisterServlet` |
| TC-REG-10 | Refresh success page | P1 + TC-REG-01 done | Reload `/register/success` | Redirect to `/register`; no second record | 302 → `/register`; still one record | Pass | Script; Post/Redirect/Get |
| TC-REG-11 | Normalisation | P1 | roll `24cse901`, email `Test.One@Example.com` | Stored as `24CSE901` and `test.one@example.com` | Stored exactly so (checked in MySQL) | Pass | Script + Manual |
| TC-REG-12 | Department not in list | P1 | department = `Hacking 101` (sent directly, bypassing the drop-down) | "Please select a department from the list." | Message shown; not saved | Pass | Script |
| TC-REG-13 | Browser-side validation | P1 | Click Submit on the empty form with JavaScript on | Browser blocks submission, fields turn red, focus moves to first error | Form got `was-validated`; 12 invalid inputs (10 fields, 3 gender radios count separately); URL unchanged | Pass | Browser |
| TC-REG-14 | Roll number typing | P1 | Type `24it-988` | Field shows `24IT-988` | `24IT-988` | Pass | Browser |
| TC-REG-15 | Registration with JavaScript disabled | P1, JS off | Submit an empty form | Server still rejects it with field messages | Same as TC-REG-02 (curl sends no JavaScript) | Pass | Script/Manual — curl is a client without JavaScript |

## 2. Authentication (TC-AUTH)

| ID | Feature | Pre | Input | Expected result | Actual result | Status | Remarks |
|---|---|---|---|---|---|---|---|
| TC-AUTH-01 | Successful login | P1 + admin exists | correct username/password | 302 → `/admin/dashboard` | 302 → `/admin/dashboard`; dashboard opens | Pass | Script |
| TC-AUTH-02 | Wrong password | P1 | `admin` / `wrong-password-1` | "Invalid username or password."; stays on login | As expected | Pass | Script |
| TC-AUTH-03 | Unknown user | P1 | `no_such_admin` / any | Same generic message (no hint that the user doesn't exist) | As expected | Pass | Script |
| TC-AUTH-04 | Empty fields | P1 | empty username/password | "Please enter both username and password." | As expected | Pass | Script |
| TC-AUTH-05 | Unauthorised dashboard access | P1, not logged in | open `/admin/dashboard` | 302 → `/login?required=true`; "Please log in to access the admin area." | As expected | Pass | Script |
| TC-AUTH-06 | Logout | P2 | click Log out (POST with token) | 302 → `/login?logout=true`; old cookie can no longer open admin pages | 302 → login; dashboard with old cookie → 302 to login | Pass | Script 06a/06b |
| TC-AUTH-07 | No caching of admin pages | P2 | open dashboard, inspect headers | `Cache-Control: no-cache, no-store, must-revalidate` | Header present | Pass | Script |
| TC-AUTH-08 | Session fixation protection | P1 | get a session cookie, then log in | Session ID changes at login | ID before ≠ ID after | Pass | Script |
| TC-AUTH-09 | Cookie flags | P1 | log in, inspect `Set-Cookie` | `HttpOnly; SameSite=Lax` | `Set-Cookie: JSESSIONID=…; Path=/student-registration; HttpOnly; SameSite=Lax` | Pass | Manual (curl) |
| TC-AUTH-10 | Login page when logged in | P2 | open `/login` | redirect to dashboard | 302 → `/admin/dashboard` | Pass | Script |
| TC-AUTH-11 | Session timeout | P2 | stay idle for 31 minutes, then click a menu item | Redirect to login | — | Pending | Configured in `web.xml` (`<session-timeout>30`) and `LoginServlet` (`setMaxInactiveInterval(1800)`); needs a 30-minute wait |
| TC-AUTH-12 | Admin setup tool | MySQL running | `mvn compile exec:java`: weak password `short`, then mismatched passwords, then a valid password; then run again and answer `no` | Weak and mismatched passwords rejected; admin created; hash `$2a$12$…` (60 chars) stored; second run makes no changes | Exactly as expected; `admin` table holds a 60-character `$2a$12$` hash | Pass | Manual |
| TC-AUTH-13 | Setup page closed once an admin exists | P1 + admin exists | open `/setup` | 302 → `/login` | 302 → `/login` | Pass | Script |
| TC-AUTH-14 | No extra admin through `/setup` | P1 + admin exists | POST `/setup` with username `intruder` | 302 → `/login`; no new admin | Redirected; `intruder` not created (admin count stayed 1) | Pass | Script + Manual |
| TC-AUTH-15 | First-time setup page | Fresh database, 0 admins | open `/login`; create admin `admin` with a valid password | Redirect to `/setup`; after saving → `/login?setup=done` "Administrator account created. Please sign in."; login works | Exactly as expected; 60-char `$2a$12$` hash stored; login → dashboard | Pass | Browser |
| TC-AUTH-16 | Setup page validation | 0 admins | password `short`; passwords that differ; username `a b` | A message under the right field; nothing saved | "Use 8-72 characters…", "The two passwords do not match.", "Use 4-50 letters…"; admin count still 0 | Pass | Manual (curl) |

## 3. Dashboard (TC-DASH)

| ID | Feature | Pre | Input | Expected result | Actual result | Status | Remarks |
|---|---|---|---|---|---|---|---|
| TC-DASH-01 | Statistics match database | P2, 17 students | open dashboard; run the same counts in MySQL | Cards equal SQL results | Dashboard 17 / 15 / 2 / 7 = SQL `COUNT(*)` 17, Active 15, Inactive 2, `COUNT(DISTINCT department)` 7 | Pass | Manual + Script 01a |
| TC-DASH-02 | Statistics update dynamically | P2 | delete 3 test students | Total decreases by 3 | Total dropped by exactly 3 | Pass | Script |
| TC-DASH-03 | Department-wise statistics | P2 | compare bars with `SELECT department, COUNT(*) … GROUP BY department` | Same names and counts, largest first | Identical: CSE 6, Computer Applications 3, ECE 2, IT 2, Mechanical 2, Civil 1, Electrical 1 | Pass | Manual |
| TC-DASH-04 | Recent registrations | P2 | register a new student | It appears first in "Recent Registrations" | Newest student listed first | Pass | JUnit-IT `statisticsReflectNewRows`; Browser |
| TC-DASH-05 | Empty database | P2, no students | open dashboard | Zeros and "No registrations yet" empty state | — | Pending | Requires emptying the `students` table |

## 4. Student management (TC-MGT)

| ID | Feature | Pre | Input | Expected result | Actual result | Status | Remarks |
|---|---|---|---|---|---|---|---|
| TC-MGT-01 | List students | P2 | open All Students | Table of students, newest first, 10 per page | 10 rows on page 1, "Page 1 of 2", "Showing 1–10 of 17" | Pass | Script + Manual |
| TC-MGT-02 | Search by name | P2 | keyword = name | Matching student(s) only | Correct student found (`mehta` → Aarav Mehta) | Pass | Script + Manual |
| TC-MGT-03 | Search by roll number | P2 | keyword = roll number | Matching student | Found (`22ECE` → Ishita Banerjee) | Pass | Script + Manual |
| TC-MGT-04 | Search by email | P2 | keyword = part of email | Matching student | Found (`kavya.joshi@` → Kavya Joshi) | Pass | Script + Manual |
| TC-MGT-05 | Filter by department | P2 | department = Computer Applications | Only that department | 3 students, equal to SQL count 3 | Pass | Script + Manual |
| TC-MGT-06 | Filter by semester | P2 | semester = 5 | Only semester-5 students | 3 students, equal to SQL count 3 | Pass | Script + Manual |
| TC-MGT-07 | Filter by status | P2 | status = Inactive | Only inactive students | 2 students (Sanya Kapoor, Rohan Verma) | Pass | Script + Manual |
| TC-MGT-08 | Combined filters | P2 | CSE + semester 3 (+ keyword in script) | Students matching all conditions | 3 students; script combination found its record | Pass | Script + Manual |
| TC-MGT-09 | View profile | P2 | click a name | Full profile with registration ID | Profile shows all fields and `SRS-…` | Pass | Script + Browser |
| TC-MGT-10 | Invalid profile ID | P2 | `?id=999999999`, `?id=abc`, no id | 404 friendly page | 404 for all three, no stack trace | Pass | Script + Manual |
| TC-MGT-11 | Add student | P2 | valid data, status Inactive | Saved; redirect to profile with "…was added with registration ID SRS-…" | As expected; status Inactive stored | Pass | Script + Manual |
| TC-MGT-12 | Edit student | P2 | change name and semester | Saved; profile shows new values; flash "Changes to … were saved." | As expected; MySQL row updated | Pass | Script + Manual |
| TC-MGT-13 | Edit to duplicate values | P2 | set roll/email of another student | Error under the field; no change | "This roll number is already registered." / "This email address is already registered." | Pass | Script + Manual |
| TC-MGT-14 | Change status | P2 | Mark Inactive / Mark Active | Status changes; flash "Status changed to …" | As expected; invalid value `Deleted` rejected, status unchanged | Pass | Script + Manual |
| TC-MGT-15 | Delete student | P2 | Delete (POST with token) | Redirect to list, flash "Student … was deleted."; profile now 404 | As expected; row gone from MySQL; deleting again → "Student not found…" | Pass | Script + Manual |
| TC-MGT-16 | Delete with GET | P2 | open `/admin/students/delete?id=…` | 405; nothing deleted | 405; record still present | Pass | Script |
| TC-MGT-17 | CSV export | P2 | Export CSV (all, and filtered) | File download `students-<date>.csv`, header + one line per student, commas quoted | Header + 17 lines for all; filtered export contained only the matching rows; parsed correctly (13 columns each) | Pass | Script + Manual |
| TC-MGT-18 | Empty state | P2 | keyword `zzzz` | "No students match your search" + Clear filters | As expected | Pass | Script + Browser |
| TC-MGT-19 | Pagination | P2 | page 2; page 999 | Page 2 shows remaining 7; out-of-range page shows last page | 7 rows on page 2; `page=999` → "Page 2 of 2" | Pass | Manual |
| TC-MGT-20 | Delete confirmation dialog | P2 | click Delete, then Cancel; again, then Delete | Cancel keeps the record; Delete removes it | Modal showed "Delete Browser Test Student (24IT-988)? …"; Cancel kept it; Delete removed it | Pass | Browser |
| TC-MGT-21 | Print profile | P2 | Print on a profile | Clean layout without menu/buttons | Print view shows only the profile cards (verified with print-media rendering and PDF) | Pass | Browser; a print-layout bug was found and fixed (see TEST_RESULTS §5) |
| TC-MGT-22 | Flash message shown once | P2 | reload page after a success message | Message disappears after reload | Not shown again | Pass | Manual |

## 5. Security (TC-SEC)

| ID | Feature | Pre | Input | Expected result | Actual result | Status | Remarks |
|---|---|---|---|---|---|---|---|
| TC-SEC-01 | SQL injection in registration | P1 | name `Robert'); DROP TABLE students;--`; address `… 1 Main St'); DROP TABLE students; --` | Name rejected by validation; address stored as plain text; table intact | Name rejected; address stored literally; `students` table still exists | Pass | Script 01a/01b |
| TC-SEC-02 | SQL injection in login | P1 | username `admin' OR '1'='1`; password `' OR '1'='1` | Login fails | "Invalid username or password." for both | Pass | Script |
| TC-SEC-03 | SQL injection in search | P2 | keyword `' OR '1'='1` | No rows (not all rows) | Empty state shown | Pass | Script; JUnit-IT `sqlInjectionIsTreatedAsText` |
| TC-SEC-04 | Unauthorised admin URLs | P1, not logged in | `/admin/students`, `/view?id=1`, `/add`, `/export` | Redirect to login | All redirected | Pass | Script |
| TC-SEC-05 | Session invalid after logout | P2 | logout, then reuse old cookie | Redirect to login | As expected | Pass | Script (= TC-AUTH-06b) |
| TC-SEC-06 | CSRF protection | P2 | POST add / delete / logout without `csrfToken` (or a forged one) | 403; no change | 403 for all; record not deleted | Pass | Script 06a/b/c + Manual |
| TC-SEC-07 | XSS (script in input) | P1/P2 | address `<script>alert('xss')</script> …` | Shown as text `&lt;script&gt;`; no script runs | Escaped in profile; no executable script | Pass | Script 07a/07b |
| TC-SEC-08 | Duplicate form submission | P1 | (a) same POST twice; (b) double-click Submit | (a) second rejected as duplicate; (b) only one request sent | (a) "This roll number is already registered."; (b) 1 submission, button disabled with "Submitting…" | Pass | Script 08a/08b; Browser |
| TC-SEC-09 | Security headers | P2 | inspect response headers | `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff` | Both present | Pass | Script + Manual |
| TC-SEC-10 | No stack traces | P1 | 404 page; database stopped (TC-SEC-11) | Friendly page, no Java exception text | No "Exception" text in any error page | Pass | Script + Manual |
| TC-SEC-11 | Database unavailable | P2 | stop MySQL; open home, register, login, dashboard; restart MySQL | Public pages load; register/dashboard → friendly 500; login → "database is not available"; full recovery after restart without restarting Tomcat | Exactly as expected; errors logged in `localhost.<date>.log`; dashboard 200 again after restart | Pass | Manual |
| TC-SEC-12 | No secrets in Git | — | `git ls-files`, `git log -p` searched for the DB and admin passwords | Only `db.properties.example`; passwords never committed | As expected (0 occurrences) | Pass | Manual |
| TC-SEC-13 | Password storage | admin created | `SELECT password_hash FROM admin` | BCrypt hash, never plain text; same password → different hashes | `$2a$12$…`; JUnit confirms random salt | Pass | Manual + JUnit `PasswordUtilTest` |
| TC-SEC-14 | Helpful database hints | P1 | stop MySQL, open `/login`; start MySQL, reload | Clear hint what to fix; hint gone once MySQL runs | "Cannot reach the MySQL server on localhost:3306. Make sure MySQL is running (…)"; gone after restart | Pass | Manual; JUnit `DBConnectionTest` covers the other hints |

## 6. Usability, compatibility and performance (TC-UI, TC-PERF, TC-ENV)

| ID | Feature | Pre | Input | Expected result | Actual result | Status | Remarks |
|---|---|---|---|---|---|---|---|
| TC-UI-01 | Mobile layout | P1/P2 | width 390 px: register, dashboard, student list; open menu | No horizontal scrolling; sidebar becomes slide-in menu | 8 pages at 390 px fit exactly (page width = screen width); dashboard and student list measured at 11 widths from 360 to 1920 px: no page or table overflow; slide-in menu works below 1200 px | Pass | Browser; defect D-10 found and fixed |
| TC-UI-02 | Works offline | P1 | check pages for external URLs | No CDN / internet resources | None found; Bootstrap, icons and font served locally | Pass | Manual (grep) |
| TC-UI-03 | Other browsers | P1 | Firefox, Edge, Safari | Same appearance and behaviour | — | Pending | Only Chromium was used in development |
| TC-PERF-01 | 10,000 students | P2 + 10,000 extra rows | time dashboard, list (page 1 and 500), search, filters, profile, CSV export | Each page < 1 s | Best of 5: dashboard 38 ms, list 18 ms, page 500 34 ms, name search 41 ms, roll search 42 ms, 3 filters 17 ms, profile 7 ms; CSV of 10,018 lines 0.35 s; `EXPLAIN` shows indexes used | Pass | Manual; extra rows deleted afterwards |
| TC-ENV-01 | Setup on Windows | fresh Windows laptop | follow SETUP_AND_EXECUTION.md | App runs at `localhost:8080/student-registration` | — | Pending | To be done by the student |
| TC-ENV-02 | Setup on macOS | fresh Mac | follow SETUP_AND_EXECUTION.md | App runs | — | Pending | To be done by the student |
| TC-ENV-03 | Run from IDE | IntelliJ Ultimate / Eclipse | Tomcat run configuration | App runs from the IDE | — | Pending | To be done by the student |
| TC-ENV-04 | Data visible in MySQL Workbench | P1 | register a student, refresh the table in Workbench | New row visible | — | Pending | Equivalent check done with the `mysql` command line (TC-REG-01) |
| TC-ENV-05 | `start.sh` first run | Java 17, MySQL running, schema imported, no `db.properties` | `./start.sh`, answer username and password | `db.properties` created; app builds, starts and prints READY; browser opens | Settings saved; READY printed after 11 s (downloads already cached) | Pass | Manual on Linux; macOS run pending |
| TC-ENV-06 | `start.bat` first run (Windows) | Windows, Java 17, MySQL running, schema imported | double-click `start.bat` | Same as TC-ENV-05 | — | Pending | Windows not available in the development environment |
| TC-ENV-07 | Ctrl+C stops the application | started with `./start.sh` | press Ctrl+C | Tomcat stops; port 8080 free | Stopped within 5 s; no Tomcat process left | Pass | Manual on Linux |

## 7. Automated unit and integration tests (summary)

| ID | Test class | Tests | What it checks | Status |
|---|---|---|---|---|
| TC-UNIT-01 | `ValidationUtilTest` | 13 | every validation rule incl. admin usernames, safe conversions, SQL text rejected | Pass |
| TC-UNIT-02 | `PasswordUtilTest` | 5 | BCrypt format/cost, wrong passwords, random salt, invalid hashes, strength rules | Pass |
| TC-UNIT-03 | `CsvUtilTest` | 4 | quoting, escaping quotes, formula protection | Pass |
| TC-UNIT-04 | `StudentTest` | 7 | default status, registration ID, initials, date formatting, age, filter, CSRF tokens | Pass |
| TC-UNIT-05 | `DBConnectionTest` | 5 | beginner-friendly hints for missing settings, wrong password, missing database/tables, stopped server | Pass |
| TC-INT-01 | `StudentDAOIntegrationTest` | 15 | insert/read, UNIQUE (roll, case-insensitive email), CHECK semester, update, status, delete, search, filters, paging, LIKE wildcards, SQL injection, statistics, AdminDAO count/create/find/reset | Pass |
