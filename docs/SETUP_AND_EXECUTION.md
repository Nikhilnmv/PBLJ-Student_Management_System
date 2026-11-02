# Setup and Execution Guide

This guide takes you from a fresh laptop to a running Student Registration System.
Follow the steps **in order**. Commands are given for **Windows** and **macOS**
(Linux is the same as macOS except for installation).

> Time needed: about 45–60 minutes the first time (mostly downloads).

---

## 0. What you will install

| Software | Version | Why it is needed |
|---|---|---|
| JDK (Java Development Kit) | **17** | Compiles and runs the Java code |
| Apache Maven | 3.8 or newer | Downloads libraries and builds the WAR file |
| MySQL Community Server | **8.0** | Stores students and admin accounts |
| MySQL Workbench | 8.0 (optional) | A GUI to look at the tables during the demo |
| Apache Tomcat | **9.0.x** (not 10 or 11) | Runs the web application |
| Git | any | Downloads the project from GitHub |
| IDE | IntelliJ IDEA or Eclipse | Reading and editing the code |

> **Why Tomcat 9 and not 10/11?** Tomcat 10+ renamed the `javax.servlet` package to
> `jakarta.servlet`. This project uses `javax.servlet` (Servlet 4.0), which only Tomcat 9 supports.
> On Tomcat 10 the servlets would not be found and every page would return 404.

---

## 1. Install and verify the tools

### 1.1 Java 17

**Windows:** download "Eclipse Temurin 17 (LTS)" `.msi` from <https://adoptium.net>. During installation,
enable **"Set JAVA_HOME variable"** and **"Add to PATH"**.

**macOS (Homebrew):**
```bash
brew install openjdk@17
```
Then follow the `sudo ln -sfn ...` instruction that Homebrew prints, and add to `~/.zshrc`:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**Verify (both):** open a **new** terminal / Command Prompt:
```bash
java -version
```
You should see `openjdk version "17.x.x"`. (A newer JDK such as 21 also works for building because
the project is compiled with `--release 17`, but Java 17 is the documented version.)

### 1.2 Maven

**Windows:** download the "Binary zip archive" from <https://maven.apache.org/download.cgi>, extract it
to e.g. `C:\tools\apache-maven-3.9.x`, then add `C:\tools\apache-maven-3.9.x\bin` to the **Path**
environment variable (Start → "Edit the system environment variables" → Environment Variables → Path → Edit → New).

**macOS:**
```bash
brew install maven
```

**Verify:**
```bash
mvn -version
```
It must show Maven 3.8+ **and** "Java version: 17…". If it shows another Java version, fix `JAVA_HOME`.

### 1.3 MySQL 8.0

**Windows:** download **MySQL Installer** from <https://dev.mysql.com/downloads/installer/>. Choose
"Server only" or "Developer Default" (includes Workbench). Keep port **3306**, choose
"Use Strong Password Encryption", and **remember the root password**.

**macOS:**
```bash
brew install mysql@8.0
brew services start mysql@8.0
mysql_secure_installation      # set the root password
```
(If `mysql` is "command not found", run the `echo 'export PATH=...'` line that Homebrew printed.)

**Verify:**
```bash
mysql --version
mysql -u root -p -e "SELECT VERSION();"
```
On Windows, if `mysql` is not recognised, use the **MySQL 8.0 Command Line Client** from the Start
menu, or add `C:\Program Files\MySQL\MySQL Server 8.0\bin` to Path.

### 1.4 Apache Tomcat 9

Download the **9.0.x "Core" zip** (Windows) or **tar.gz** (macOS) from
<https://tomcat.apache.org/download-90.cgi> and extract it, e.g. to
`C:\tools\apache-tomcat-9.0.x` or `~/tools/apache-tomcat-9.0.x`.
In this guide that folder is called **`<TOMCAT>`**.

macOS only — make the scripts executable once:
```bash
chmod +x ~/tools/apache-tomcat-9.0.*/bin/*.sh
```

### 1.5 Git

Windows: <https://git-scm.com/download/win> (also installs **Git Bash**). macOS: `xcode-select --install`.

---

## 2. Get the project

```bash
git clone git@github.com:Nikhilnmv/PBLJ-Laxita.git
cd PBLJ-Laxita
```
(Use `https://github.com/Nikhilnmv/PBLJ-Laxita.git` if SSH keys are not set up.)

---

## 3. Create the database

### 3.1 Import the schema and the sample data

**Option A – terminal (macOS, Linux, Windows Command Prompt):**
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample_data.sql
```
In **Windows PowerShell** the `<` sign does not work; use the MySQL client's `source` command instead:
```bash
mysql -u root -p
```
```sql
SOURCE database/schema.sql;
SOURCE database/sample_data.sql;
EXIT;
```

**Option B – MySQL Workbench:** File → Open SQL Script → `database/schema.sql` → click the lightning
(Execute) icon. Repeat for `database/sample_data.sql`.

`schema.sql` creates the database `student_registration_db` with the tables `students` and `admin`.
`sample_data.sql` inserts 16 **fictional** students. Both scripts can safely be run more than once.

### 3.2 Create a MySQL user for the application (recommended)

The application should not log in as `root`. Open the MySQL client (`mysql -u root -p`) and run
(choose your own password):

```sql
CREATE USER 'srs_app'@'localhost' IDENTIFIED BY 'Choose-A-Strong-Password1';
GRANT SELECT, INSERT, UPDATE, DELETE ON student_registration_db.* TO 'srs_app'@'localhost';
FLUSH PRIVILEGES;
```

This user can only read and change data in this one database — it cannot drop tables or touch
other databases (the *principle of least privilege*).

### 3.3 Check

```sql
USE student_registration_db;
SHOW TABLES;                       -- admin, students
SELECT COUNT(*) FROM students;     -- 16
```

---

## 4. Configure the database connection

The application reads the database settings in this order:

1. Environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD`, if they are set;
2. otherwise the file `src/main/resources/db.properties`.

The **file is the easiest option.** Create it from the example:

**Windows (Command Prompt):**
```bash
copy src\main\resources\db.properties.example src\main\resources\db.properties
```
**macOS / Linux / Git Bash:**
```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Open `src/main/resources/db.properties` and set the password you chose in step 3.2:

```properties
db.url=jdbc:mysql://localhost:3306/student_registration_db?useSSL=false&allowPublicKeyRetrieval=true
db.user=srs_app
db.password=Choose-A-Strong-Password1
```

`db.properties` is listed in `.gitignore`, so it is **never committed to GitHub**. Run `git status` —
it must not appear in the list of changed files.

> **Using environment variables instead** (for example on a shared computer):
> macOS: `export DB_URL='jdbc:mysql://localhost:3306/student_registration_db?useSSL=false&allowPublicKeyRetrieval=true'`
> (and the same for `DB_USER`, `DB_PASSWORD`) in the terminal **before** starting Tomcat.
> Windows: `set DB_PASSWORD=...` in the same Command Prompt before running `startup.bat`.
> Tomcat also reads `<TOMCAT>/bin/setenv.sh` (macOS) / `setenv.bat` (Windows) if you create it.

---

## 5. Create the administrator account

The repository contains **no admin password** (it would be visible to anyone on GitHub).
Create your own admin with the setup tool:

```bash
mvn compile exec:java
```

It asks for a username and a password (8–72 characters with at least one letter and one digit),
asks you to confirm the password, stores a **BCrypt hash** in the `admin` table, and prints
`SUCCESS: administrator 'admin' created.` In a normal terminal the password is not shown while
you type.

From an IDE instead: open `src/main/java/com/student/util/AdminSetup.java` and click **Run** next to
`main`. (IDE consoles cannot hide input, so the password is visible there.)

Forgot the password? Run the tool again with the same username and answer `yes` to
"Reset its password?".

---

## 6. Build the application

```bash
mvn clean package
```

Maven downloads the libraries (first time only), compiles the code, runs the unit tests and
creates **`target/student-registration.war`**. The last lines must say `BUILD SUCCESS`.

> If you change `db.properties` later, run `mvn clean package` again — the file is copied into the WAR.

---

## 7. Run on Tomcat

### Option A – copy the WAR into Tomcat (works everywhere)

1. Copy `target/student-registration.war` into `<TOMCAT>/webapps/`.
2. Start Tomcat:
   - Windows: `<TOMCAT>\bin\startup.bat`
   - macOS: `<TOMCAT>/bin/startup.sh`
3. Wait about 10 seconds, then open **<http://localhost:8080/student-registration/>**.
4. Stop Tomcat with `shutdown.bat` / `shutdown.sh`.

Check `<TOMCAT>/logs/catalina.<date>.log` (macOS: also `catalina.out`). A successful start contains:
```
Student Registration System: connected to MySQL (8.0.xx) at jdbc:mysql://localhost:3306/student_registration_db...
```

### Option B – IntelliJ IDEA Ultimate

1. File → Open → select the project folder (it is detected as a Maven project).
2. Run → Edit Configurations → **+** → **Tomcat Server → Local** → Application server: choose `<TOMCAT>`.
3. **Deployment** tab → **+** → Artifact → `student-registration:war exploded`;
   set **Application context** to `/student-registration`.
4. Click Run. The browser opens automatically.

**IntelliJ IDEA Community** has no Tomcat support. Either install the free **Smart Tomcat** plugin
(Settings → Plugins), or use Option A.

### Option C – Eclipse IDE for Enterprise Java and Web Developers

1. File → Import → Maven → **Existing Maven Projects** → select the project folder.
2. Window → Show View → **Servers** → "No servers available, click to create" → **Apache → Tomcat v9.0 Server** → point to `<TOMCAT>`.
3. Right-click the project → **Run As → Run on Server**.

---

## 8. Use the application

| Page | URL |
|---|---|
| Home | <http://localhost:8080/student-registration/> |
| Student registration | <http://localhost:8080/student-registration/register> |
| Admin login | <http://localhost:8080/student-registration/login> |
| Admin dashboard | <http://localhost:8080/student-registration/admin/dashboard> |
| All students | <http://localhost:8080/student-registration/admin/students> |

Log in with the admin account you created in step 5.

---

## 9. Run the tests

```bash
mvn test                              # 28 unit tests (no database needed)
mvn test -Dintegration=true           # + 15 integration tests against your MySQL database
```

With the application running on Tomcat, the automated HTTP tests (Git Bash on Windows, Terminal on macOS):
```bash
ADMIN_PASSWORD='your-admin-password' ./scripts/functional-test.sh
```
The integration and HTTP tests create their own test records and delete them again.
See [TEST_PLAN.md](TEST_PLAN.md) for details.

---

## 10. Start fresh (reset the database)

```sql
DROP DATABASE student_registration_db;
```
Then repeat step 3.1 (schema + sample data), step 3.2's `GRANT` line, and step 5 (admin account).

---

## 11. Troubleshooting

| Problem / message | Cause | Fix |
|---|---|---|
| `Access denied for user 'srs_app'@'localhost'` | Wrong user or password in `db.properties` | Correct the file, then `mvn clean package` and redeploy |
| `Public Key Retrieval is not allowed` | URL is missing `allowPublicKeyRetrieval=true` | Copy the URL exactly from `db.properties.example` |
| `Communications link failure` | MySQL is not running, or not on port 3306 | Windows: Services → MySQL80 → Start. macOS: `brew services start mysql@8.0` |
| `Unknown database 'student_registration_db'` | Schema not imported | Run step 3.1 |
| `Database is not configured` in the log | `db.properties` missing from the WAR | Create it (step 4), then rebuild |
| Every page shows **404** | WAR not deployed, wrong URL, or **Tomcat 10/11** used | Use Tomcat 9; check that `<TOMCAT>/webapps/student-registration/` exists; read `logs/localhost.<date>.log` |
| `UnsupportedClassVersionError … class file version 61.0` | Tomcat runs on Java 8 or 11 | Set `JAVA_HOME` to JDK 17 and restart Tomcat |
| `Address already in use: 8080` | Another program uses port 8080 | Stop it, or change `port="8080"` in `<TOMCAT>/conf/server.xml` |
| Login always says "Invalid username or password" | No admin account yet, or typing mistake | Run step 5 (you can reset the password there) |
| Blank page / "Something went wrong" | Database stopped while the app was running | Start MySQL; the app reconnects automatically |
| Code changes don't appear | Old WAR still deployed | `mvn clean package`, copy the new WAR, refresh with Ctrl+F5 |
| `mvn` not recognised | Maven `bin` folder not on Path | Repeat step 1.2 and open a **new** terminal |
| `mvn -version` shows the wrong Java | `JAVA_HOME` points to another JDK | Set `JAVA_HOME` to the JDK 17 folder |

Where are the logs? `<TOMCAT>/logs/`. `catalina.<date>.log` contains start-up messages,
`localhost.<date>.log` contains application errors with full details (users never see these).
