# Database Explanation

This document explains the database from the ground up: what a relational database is, why MySQL,
every table and column, the constraints and indexes, every SQL query the application runs, and how
Java talks to MySQL through JDBC.

Formal design (ER diagram, data dictionary): [DATABASE_DESIGN.md](DATABASE_DESIGN.md).
SQL files: [`database/schema.sql`](../database/schema.sql), [`database/sample_data.sql`](../database/sample_data.sql).

---

## 1. What is a relational database?

A **relational database** stores data in **tables**. A table is like a spreadsheet:
- each **column** has a name and a data type (`full_name` is text, `semester` is a number),
- each **row** is one record (one student),
- a **primary key** uniquely identifies each row,
- **constraints** are rules the database enforces (no empty name, no duplicate email, semester 1–8).

We talk to the database using **SQL** (Structured Query Language): `CREATE`, `INSERT`, `SELECT`,
`UPDATE`, `DELETE`. A **DBMS** (Database Management System) such as MySQL stores the data on disk,
answers queries, enforces constraints and lets many users work at the same time safely.

## 2. Why MySQL?

- Free, open source, and the most widely taught relational database in Indian colleges.
- Runs on Windows, macOS and Linux; MySQL Workbench gives a friendly GUI for the demo.
- Fully supports what we need: primary keys, `UNIQUE`, `CHECK` (MySQL 8.0.16+), indexes,
  `AUTO_INCREMENT`, `TIMESTAMP DEFAULT CURRENT_TIMESTAMP`, transactions (InnoDB engine).
- An official JDBC driver (MySQL Connector/J) is available from Maven Central.

## 3. The database: `student_registration_db`

```sql
CREATE DATABASE IF NOT EXISTS student_registration_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```
- `IF NOT EXISTS` — the script can run again without errors and without deleting data.
- `utf8mb4` — can store every character in every language (Hindi, Tamil, accents, emoji).
- `utf8mb4_unicode_ci` — the *collation* (rules for comparing text). `ci` = **case-insensitive**, so
  `AARAV.MEHTA@EXAMPLE.COM` and `aarav.mehta@example.com` count as the same email for the UNIQUE rule.

## 4. Table `students`

```sql
CREATE TABLE IF NOT EXISTS students (
    student_id   INT          NOT NULL AUTO_INCREMENT,
    roll_number  VARCHAR(20)  NOT NULL,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    phone        VARCHAR(15)  NOT NULL,
    gender       VARCHAR(10)  NOT NULL,
    dob          DATE         NOT NULL,
    department   VARCHAR(50)  NOT NULL,
    course       VARCHAR(50)  NOT NULL,
    semester     INT          NOT NULL,
    address      TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'Active',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_students PRIMARY KEY (student_id),
    CONSTRAINT uk_students_roll_number UNIQUE (roll_number),
    CONSTRAINT uk_students_email       UNIQUE (email),
    CONSTRAINT chk_students_semester CHECK (semester BETWEEN 1 AND 8),
    CONSTRAINT chk_students_status   CHECK (status IN ('Active', 'Inactive')),
    CONSTRAINT chk_students_gender   CHECK (gender IN ('Male', 'Female', 'Other')),
    INDEX idx_students_department (department),
    INDEX idx_students_semester   (semester),
    INDEX idx_students_status     (status),
    INDEX idx_students_created_at (created_at)
) ENGINE = InnoDB;
```

### 4.1 Column by column

| Column | Type | Why this type / rule |
|---|---|---|
| `student_id` | `INT AUTO_INCREMENT` | MySQL generates 1, 2, 3 … automatically → unique registration ID `SRS-00001` |
| `roll_number` | `VARCHAR(20)` UNIQUE | College roll numbers mix letters and digits (`23CSE001`); no two students may share one |
| `full_name` | `VARCHAR(100)` | Variable-length text; 100 characters is plenty for a name |
| `email` | `VARCHAR(100)` UNIQUE | One registration per email address |
| `phone` | `VARCHAR(15)` | Stored as **text**, not a number: phone numbers are never added or multiplied, and a number type would lose leading zeros or `+` signs |
| `gender` | `VARCHAR(10)` + CHECK | Only `Male`, `Female`, `Other` allowed |
| `dob` | `DATE` | A real date type: MySQL rejects `2005-02-30` and allows date calculations |
| `department` | `VARCHAR(50)` + INDEX | Filtered and grouped on the admin pages |
| `course` | `VARCHAR(50)` | B.E., B.Tech, BCA … |
| `semester` | `INT` + CHECK 1–8 + INDEX | A number, so it can be compared and range-checked |
| `address` | `TEXT` | Addresses can be long and multi-line (application limits it to 500 characters) |
| `status` | `VARCHAR(20)` DEFAULT `'Active'` + CHECK | New rows are Active unless stated otherwise |
| `created_at` | `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` + INDEX | MySQL records the registration time itself; used for "Recent Registrations" and sorting |

### 4.2 Keys and constraints

- **Primary key (`pk_students`)** — `student_id` uniquely identifies each row and can never be `NULL`.
  Every `UPDATE`/`DELETE` in the application uses `WHERE student_id = ?`, so exactly one row is affected.
- **Unique constraints** — `uk_students_roll_number`, `uk_students_email`. The *database* guarantees
  uniqueness even if two people submit the form at the same moment (the Java pre-check alone could
  not guarantee that). The constraint names appear in MySQL's error message, which `StudentDAO`
  uses to tell the user *which* field is duplicated.
- **NOT NULL** — every field is mandatory, matching the form.
- **CHECK constraints** — MySQL 8.0.16+ rejects impossible values even if someone bypasses the Java
  validation (tested in `StudentDAOIntegrationTest.checkConstraintRejectsInvalidSemester`).
- **DEFAULT** — `status` and `created_at` are filled automatically.

### 4.3 Indexes
An **index** is like the index at the back of a book: MySQL can jump to matching rows instead of
reading the whole table.
- `PRIMARY KEY` and the two `UNIQUE` constraints create indexes automatically (fast lookups by ID,
  roll number, email — used by the duplicate checks).
- `idx_students_department`, `idx_students_semester`, `idx_students_status` — speed up the filters
  and dashboard counts.
- `idx_students_created_at` — speeds up `ORDER BY created_at DESC` (newest first).
- Note: the keyword search uses `LIKE '%text%'`. A pattern **starting** with `%` cannot use an index,
  so MySQL scans the table — perfectly fast for thousands of rows; a FULLTEXT index would be the
  next step for millions.

### 4.4 Why the indexes are inside `CREATE TABLE`
MySQL has no `CREATE INDEX IF NOT EXISTS`. Declaring indexes inside `CREATE TABLE IF NOT EXISTS`
keeps `schema.sql` safe to run repeatedly.

## 5. Table `admin`

```sql
CREATE TABLE IF NOT EXISTS admin (
    admin_id      INT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_admin PRIMARY KEY (admin_id),
    CONSTRAINT uk_admin_username UNIQUE (username)
) ENGINE = InnoDB;
```
- The column is called **`password_hash`** (not `password`) to make clear that it **never** contains
  the real password — only a 60-character BCrypt hash like `$2a$12$...`.
- `VARCHAR(255)` leaves room if a different hashing algorithm is used later.
- No rows are inserted by `schema.sql`; the first admin is created with `AdminSetup`, so no
  password ever appears in a file on GitHub.

## 6. Relationship between the tables
The two tables are **independent** — there is no foreign key. Administrators manage students
through the application, but the database does not record *which* admin changed *which* student.
(An audit table with a foreign key to `admin` is listed as a future enhancement.)

## 7. Sample data
`sample_data.sql` inserts 16 fictional students across all 7 departments, semesters 1–7 and both
statuses. `INSERT IGNORE` skips rows whose roll number/email already exist, so the script is safe to
re-run. Registration times use `NOW() - INTERVAL n DAY`, so "recent registrations" always look realistic.

> **Why might IDs skip numbers?** InnoDB reserves `AUTO_INCREMENT` values even for inserts that are
> ignored or fail (for example a duplicate). Gaps are normal and harmless — IDs only need to be
> unique, not consecutive.

---

## 8. Every SQL query the application runs

All are in `StudentDAO.java` / `AdminDAO.java` and all use `PreparedStatement` placeholders (`?`).

### CREATE (INSERT)
```sql
INSERT INTO students (roll_number, full_name, email, phone, gender, dob,
                      department, course, semester, address, status)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
```
`student_id` and `created_at` are omitted — MySQL fills them. `Statement.RETURN_GENERATED_KEYS`
returns the new `student_id`.
```sql
INSERT INTO admin (username, password_hash) VALUES (?, ?)
```

### READ (SELECT)
```sql
-- one student (profile, edit form)
SELECT student_id, roll_number, ..., created_at FROM students WHERE student_id = ?

-- duplicate checks (excludeId = 0 for new students, own id when editing)
SELECT COUNT(*) FROM students WHERE roll_number = ? AND student_id <> ?
SELECT COUNT(*) FROM students WHERE email = ? AND student_id <> ?

-- search with every filter chosen (parts are added only when a filter is used)
SELECT ... FROM students
 WHERE 1 = 1
   AND (full_name LIKE ? OR roll_number LIKE ? OR email LIKE ?)
   AND department = ?
   AND semester = ?
   AND status = ?
 ORDER BY created_at DESC, student_id DESC
 LIMIT ? OFFSET ?

-- number of matches (for "Showing 1-10 of 17" and the page count)
SELECT COUNT(*) FROM students WHERE 1 = 1 AND ...

-- admin login
SELECT admin_id, username, password_hash FROM admin WHERE username = ?
```

### Dashboard statistics (aggregate functions)
```sql
SELECT COUNT(*) FROM students                              -- total
SELECT COUNT(*) FROM students WHERE status = ?             -- active / inactive
SELECT COUNT(DISTINCT department) FROM students            -- departments with students
SELECT ... FROM students ORDER BY created_at DESC, student_id DESC LIMIT ?   -- recent 5
SELECT department, COUNT(*) AS total FROM students
 GROUP BY department ORDER BY total DESC, department ASC   -- per department
```
- `COUNT(*)` counts rows; `COUNT(DISTINCT department)` counts different values.
- `GROUP BY department` makes one group per department and `COUNT(*)` counts the rows in each group.

### UPDATE
```sql
UPDATE students SET roll_number = ?, full_name = ?, email = ?, phone = ?, gender = ?, dob = ?,
                    department = ?, course = ?, semester = ?, address = ?, status = ?
 WHERE student_id = ?
UPDATE students SET status = ? WHERE student_id = ?
UPDATE admin SET password_hash = ? WHERE username = ?
```
`executeUpdate()` returns how many rows changed: `1` = success, `0` = no such student.

### DELETE
```sql
DELETE FROM students WHERE student_id = ?
```
**Always** with a `WHERE` on the primary key — a `DELETE` without `WHERE` would empty the table.

---

## 9. CRUD summary

| Operation | SQL | Java method | Triggered by |
|---|---|---|---|
| **C**reate | `INSERT` | `StudentDAO.addStudent` | Registration form, Add Student |
| **R**ead | `SELECT` | `getStudentById`, `searchStudents`, `count…` | List, profile, dashboard, export |
| **U**pdate | `UPDATE` | `updateStudent`, `updateStatus` | Edit form, Mark Active/Inactive |
| **D**elete | `DELETE` | `deleteStudent` | Delete button (after confirmation) |

## 10. How Java connects to MySQL (JDBC)

```
StudentDAO  ──>  DBConnection.getConnection()  ──>  DriverManager  ──>  MySQL Connector/J  ──TCP 3306──>  MySQL
```

1. **Driver**: `Class.forName("com.mysql.cj.jdbc.Driver")` in `DBConnection`'s static block registers the driver.
2. **URL**: `jdbc:mysql://localhost:3306/student_registration_db?useSSL=false&allowPublicKeyRetrieval=true`
   - `jdbc:mysql://` — use the MySQL driver; `localhost:3306` — host and port; then the database name;
   - `useSSL=false` — plain connection, fine on the same computer;
   - `allowPublicKeyRetrieval=true` — required by MySQL 8's default login method when SSL is off.
3. **Credentials** come from environment variables or `db.properties` (never from source code).
4. **Connection** → **PreparedStatement** → **execute** → **ResultSet** → **close** (try-with-resources).

### 10.1 PreparedStatement in detail
```java
// AdminDAO.findByUsername
PreparedStatement statement = connection.prepareStatement(
        "SELECT admin_id, username, password_hash FROM admin WHERE username = ?");
statement.setString(1, username);
ResultSet resultSet = statement.executeQuery();
```
MySQL receives the SQL **structure** and the **value** separately. The value can never become part of
the SQL command, which is why SQL injection is impossible. It also handles quotes in values
(`O'Brien`) and converts Java types to SQL types (`setInt`, `setDate`).

**SQL injection demonstration (what would go wrong without it):**
```java
// NEVER do this:
String sql = "SELECT * FROM admin WHERE username = '" + username + "'";
// username typed:  ' OR '1'='1
// resulting SQL:   SELECT * FROM admin WHERE username = '' OR '1'='1'   -> returns every admin!
```
With `PreparedStatement`, the same input searches for a user literally named `' OR '1'='1` and finds
nothing. Verified by test TC-SEC-02 and `StudentDAOIntegrationTest.sqlInjectionIsTreatedAsText`.

### 10.2 ResultSet in detail
```java
// StudentDAO.mapRowToStudent (part)
student.setStudentId(resultSet.getInt("student_id"));
Date dob = resultSet.getDate("dob");
student.setDob(dob == null ? null : dob.toLocalDate());
Timestamp createdAt = resultSet.getTimestamp("created_at");
student.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
```
SQL types are mapped to Java types: `INT → int`, `VARCHAR/TEXT → String`, `DATE → java.sql.Date → LocalDate`,
`TIMESTAMP → java.sql.Timestamp → LocalDateTime`.

## 11. Transactions
Each DAO method runs **one** SQL statement with JDBC's default **auto-commit** mode: every statement
is its own transaction and is committed immediately. InnoDB guarantees each statement is atomic
(e.g. an `UPDATE` changes the whole row or nothing). No operation in this project needs several
statements to succeed or fail together, so explicit `commit()`/`rollback()` is not needed. If, for
example, deleting a student also had to write an audit row, we would use
`connection.setAutoCommit(false)` … `commit()` / `rollback()`.

## 12. Normalisation
- **1NF**: every column holds a single value (no lists in a column); each row is unique (primary key).
- **2NF**: the primary key is a single column, so every column depends on the whole key.
- **3NF**: no column depends on another non-key column — e.g. we do **not** store the student's age
  (it would depend on `dob`); age is calculated in Java (`Student.getAge()`).
- A trade-off: `department` and `course` are stored as text rather than as foreign keys to separate
  `departments`/`courses` tables. The allowed values are controlled by `AppConstants` and validation.
  Moving them to their own tables is a natural future improvement (see [DESIGN_DECISIONS.md](DESIGN_DECISIONS.md)).

## 13. Useful queries for the viva demo (MySQL Workbench)
```sql
USE student_registration_db;
SELECT * FROM students ORDER BY student_id DESC LIMIT 5;     -- newest registrations
SELECT department, COUNT(*) FROM students GROUP BY department; -- compare with the dashboard
SELECT username, password_hash FROM admin;                    -- shows the hash, not the password
DESCRIBE students;                                            -- column types
SHOW INDEX FROM students;                                     -- indexes
```
