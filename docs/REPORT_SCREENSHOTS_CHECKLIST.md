# Report Screenshots Checklist

Capture these screenshots **from your own running application** for the project report. Use the
fictional sample data (never real personal data).

**Tips**
- Browser window ~1366 × 768 or larger, zoom 100 %, hide bookmarks bar and other tabs.
- Windows: `Win + Shift + S`; macOS: `Cmd + Shift + 4` (area) or `Cmd + Shift + 5`.
- Save as PNG with the suggested file names, in a folder such as `report-screenshots/`.
- For full-page captures: Chrome DevTools → `Ctrl/Cmd + Shift + P` → "Capture full size screenshot".
- Write a one-line caption for each figure (suggestions below).

| # | ✓ | Screenshot | How to get it | Suggested file name | Caption |
|---|---|---|---|---|---|
| 1 | ☐ | Home page | Open `/student-registration/` | `01-home.png` | Home page of the Student Registration System |
| 2 | ☐ | Registration form (empty) | Open `/register` | `02-registration-form.png` | Public student registration form |
| 3 | ☐ | Registration validation errors | Click Submit on the empty form (or enter a bad email/phone) | `03-registration-validation.png` | Field validation on the registration form |
| 4 | ☐ | Duplicate roll number error | Register with roll `23CSE001` | `04-duplicate-roll-number.png` | Duplicate roll number rejected |
| 5 | ☐ | Registration success | Complete a valid registration | `05-registration-success.png` | Registration success with generated registration ID |
| 6 | ☐ | Admin login | Open `/login` | `06-admin-login.png` | Administrator login page |
| 7 | ☐ | Invalid login | Enter a wrong password | `07-invalid-login.png` | Invalid credentials message |
| 8 | ☐ | Admin dashboard | Log in | `08-dashboard.png` | Admin dashboard with live statistics |
| 9 | ☐ | Student list | Sidebar → All Students | `09-student-list.png` | List of registered students with pagination |
| 10 | ☐ | Search results | Search `mehta` (or your test student) | `10-search-results.png` | Search by name / roll number / email |
| 11 | ☐ | Filter results | Department = Computer Science and Engineering, Semester = 3 | `11-filter-results.png` | Filtering by department and semester |
| 12 | ☐ | Empty search state | Search `zzzz` | `12-empty-state.png` | Empty state when no student matches |
| 13 | ☐ | Student profile | Click a student name | `13-student-profile.png` | Complete student profile |
| 14 | ☐ | Edit student | Profile → Edit | `14-edit-student.png` | Editing a student record |
| 15 | ☐ | Update success message | Save the edit | `15-update-success.png` | Confirmation after updating a student |
| 16 | ☐ | Add student form | Sidebar → Add Student | `16-add-student.png` | Admin adding a student |
| 17 | ☐ | Delete confirmation dialog | Click Delete on your **test** student | `17-delete-confirmation.png` | Confirmation before deletion |
| 18 | ☐ | Status changed | Profile → Mark Inactive | `18-status-inactive.png` | Changing student status |
| 19 | ☐ | CSV export opened in Excel/Sheets | Export CSV, open the file | `19-csv-export.png` | Exported student list |
| 20 | ☐ | Print preview of profile | Profile → Print | `20-print-profile.png` | Print-friendly student profile |
| 21 | ☐ | Mobile view | Chrome DevTools → device toolbar (iPhone 12) on the registration page and dashboard | `21-mobile-view.png` | Responsive layout on a phone |
| 22 | ☐ | Unauthorised access redirect | Log out, open `/admin/dashboard` | `22-login-required.png` | Admin pages require login |
| 23 | ☐ | MySQL: database tables | Workbench → `SHOW TABLES;` and `DESCRIBE students;` | `23-db-tables.png` | Tables of student_registration_db |
| 24 | ☐ | MySQL: students records | `SELECT * FROM students ORDER BY student_id DESC LIMIT 10;` | `24-db-students.png` | Student records stored in MySQL |
| 25 | ☐ | MySQL: newly registered row | Run the query right after screenshot 5 | `25-db-new-row.png` | Newly inserted registration in MySQL |
| 26 | ☐ | MySQL: admin table (hash) | `SELECT admin_id, username, password_hash FROM admin;` | `26-db-admin-hash.png` | Admin password stored as BCrypt hash |
| 27 | ☐ | MySQL: constraints/indexes | `SHOW INDEX FROM students;` | `27-db-indexes.png` | Keys and indexes on the students table |
| 28 | ☐ | Department statistics query | `SELECT department, COUNT(*) FROM students GROUP BY department;` next to the dashboard | `28-db-group-by.png` | Dashboard figures match SQL results |
| 29 | ☐ | Project structure | IDE project tree expanded (`model`, `dao`, `servlet` …) | `29-project-structure.png` | Source code organisation (MVC + DAO) |
| 30 | ☐ | Maven build success | Terminal: `mvn clean package` | `30-maven-build.png` | Successful Maven build |
| 31 | ☐ | Unit / integration tests | Terminal: `mvn test -Dintegration=true` | `31-tests-passed.png` | Automated tests passing |
| 32 | ☐ | Functional test script | Terminal: `ADMIN_PASSWORD=… ./scripts/functional-test.sh` (last lines) | `32-functional-tests.png` | Automated HTTP tests passing |
| 33 | ☐ | Tomcat start-up log | `catalina.<date>.log` line "connected to MySQL" | `33-tomcat-log.png` | Application connected to MySQL at start-up |

Diagrams for the report are already exported in `docs/diagrams/exports/` (PNG and SVG) — no
screenshots needed for them.

> Do not use screenshots taken by someone else or edited to show results that did not happen. Every
> screenshot should come from your own running copy of the project.
