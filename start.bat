@echo off
REM =====================================================================
REM  Student Registration System - one-click start (Windows)
REM
REM  Double-click this file (or run  start.bat  in Command Prompt).
REM
REM  1. Checks that Java is installed.
REM  2. First run only: asks for your MySQL username and password and saves
REM     them in src\main\resources\db.properties (never uploaded to GitHub).
REM  3. Builds the project and starts it on Apache Tomcat 9, which Maven
REM     downloads automatically (no Maven or Tomcat installation needed).
REM  4. Opens http://localhost:8080/student-registration/ when it is ready.
REM
REM  Stop the application by pressing Ctrl+C in this window (or closing it).
REM =====================================================================
setlocal
cd /d "%~dp0"
set "APP_URL=http://localhost:8080/student-registration/"
set "CONFIG=src\main\resources\db.properties"

echo ==============================================
echo  Student Registration System
echo ==============================================

REM ---- 1. Java -----------------------------------------------------------
where java >nul 2>nul
if errorlevel 1 (
    echo Java was not found. Install JDK 17 from https://adoptium.net
    echo During installation choose "Set JAVA_HOME variable" and "Add to PATH",
    echo then open a NEW window and run start.bat again.
    pause
    exit /b 1
)

REM ---- 2. Database settings (first run only) ------------------------------
if not exist "%CONFIG%" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "scripts\configure-database.ps1"
    if errorlevel 1 (
        echo Could not save the database settings.
        pause
        exit /b 1
    )
)

REM ---- 3. Open the browser automatically when the application is ready ----
start "Waiting for the application" /min powershell -NoProfile -ExecutionPolicy Bypass -File "scripts\open-when-ready.ps1"

REM ---- 4. Build and run ----------------------------------------------------
echo.
echo Building and starting the application...
echo The FIRST run downloads Maven, the libraries and Tomcat 9 (about 35 MB) - please wait a few minutes.
echo Your browser opens automatically when it is ready: %APP_URL%
echo Press Ctrl+C in this window to stop the application.
echo.
call mvnw.cmd -q clean package cargo:run -DskipTests
echo.
echo The application has stopped. If you see an error above, read docs\START_HERE.md (section "If something goes wrong").
pause
