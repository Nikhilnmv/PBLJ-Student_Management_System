#!/usr/bin/env bash
# =====================================================================
#  Student Registration System - one-command start (macOS / Linux)
#
#  Usage (in Terminal, inside the project folder):   ./start.sh
#
#  1. Checks that Java 17+ is installed.
#  2. First run only: asks for your MySQL username and password and saves
#     them in src/main/resources/db.properties (never uploaded to GitHub).
#  3. Builds the project and starts it on Apache Tomcat 9, which Maven
#     downloads automatically (no Maven or Tomcat installation needed).
#  4. Opens http://localhost:8080/student-registration/ when it is ready.
#
#  Stop the application with Ctrl+C.
# =====================================================================
cd "$(dirname "$0")" || exit 1
APP_URL="http://localhost:8080/student-registration/"
CONFIG="src/main/resources/db.properties"

echo "=============================================="
echo " Student Registration System"
echo "=============================================="

# ---- 1. Java ---------------------------------------------------------
if ! command -v java >/dev/null 2>&1; then
    echo "Java was not found. Install JDK 17 from https://adoptium.net and run ./start.sh again."
    exit 1
fi
JAVA_MAJOR=$(java -version 2>&1 | awk -F'"' '/version/ {print $2; exit}' | cut -d. -f1)
if ! [ "${JAVA_MAJOR:-0}" -ge 17 ] 2>/dev/null; then
    echo "Java ${JAVA_MAJOR:-?} was found, but Java 17 or newer is required."
    echo "Install JDK 17 from https://adoptium.net and run ./start.sh again."
    exit 1
fi

# ---- 2. Port 8080 must be free ----------------------------------------
if command -v curl >/dev/null 2>&1 && curl -s -o /dev/null --max-time 2 "http://localhost:8080/"; then
    echo "Something is already running on port 8080 (maybe the application is already started,"
    echo "or another Tomcat). Open $APP_URL or stop the other program first."
    exit 1
fi

# ---- 3. Database settings (first run only) ----------------------------
if [ ! -f "$CONFIG" ]; then
    echo
    echo "First run: enter the username and password you use to log in to MySQL"
    echo "(the same ones you use in MySQL Workbench)."
    read -r -p "MySQL username [root]: " DB_USER
    DB_USER=${DB_USER:-root}
    read -r -s -p "MySQL password (not shown while typing): " DB_PASS
    echo
    {
        echo "# Created by start.sh - your local database settings (this file is not uploaded to GitHub)"
        echo "db.url=jdbc:mysql://localhost:3306/student_registration_db?useSSL=false&allowPublicKeyRetrieval=true"
        printf 'db.user=%s\n' "$DB_USER"
        # a backslash has a special meaning in .properties files, so it is doubled
        printf 'db.password=%s\n' "$(printf '%s' "$DB_PASS" | sed 's/\\/\\\\/g')"
    } > "$CONFIG"
    echo "Saved in $CONFIG  (edit or delete this file to change the settings)."
fi

# ---- 4. Open the browser as soon as the application answers -----------
open_browser_when_ready() {
    command -v curl >/dev/null 2>&1 || return
    for _ in $(seq 1 600); do
        if curl -s -f -o /dev/null --max-time 3 "$APP_URL"; then
            echo
            echo "=============================================="
            echo " READY:  $APP_URL"
            echo " (press Ctrl+C here to stop the application)"
            echo "=============================================="
            if command -v open >/dev/null 2>&1; then open "$APP_URL"
            elif command -v xdg-open >/dev/null 2>&1; then xdg-open "$APP_URL" >/dev/null 2>&1
            fi
            return
        fi
        sleep 2
    done
}
open_browser_when_ready &
WAITER_PID=$!
trap 'kill "$WAITER_PID" 2>/dev/null' EXIT

# ---- 5. Build and run --------------------------------------------------
echo
echo "Building and starting the application..."
echo "The FIRST run downloads Maven, the libraries and Tomcat 9 (about 35 MB) - please wait a few minutes."
echo "Your browser opens automatically when it is ready."
echo
./mvnw -q clean package cargo:run -DskipTests
