#!/usr/bin/env bash
# =====================================================================
#  Student Registration System - automated HTTP functional tests
#
#  Sends real HTTP requests (with curl) to the RUNNING application and
#  checks the responses: registration, validation, login, access control,
#  CSRF protection, search, filters, edit, status, delete, CSV export,
#  SQL injection and XSS handling.
#
#  Requirements: the app is deployed on Tomcat, MySQL is running, an admin
#  account exists (see docs/SETUP_AND_EXECUTION.md), and curl is installed.
#  Works in Linux/macOS terminals and in Git Bash on Windows.
#
#  Usage:
#      ADMIN_PASSWORD='your-admin-password' ./scripts/functional-test.sh
#  Optional: ADMIN_USER (default admin), BASE_URL
#            (default http://localhost:8080/student-registration)
#
#  Test students use roll numbers starting with "FT" and example.com
#  emails. They are deleted again at the end of the run.
# =====================================================================

BASE_URL="${BASE_URL:-http://localhost:8080/student-registration}"
ADMIN_USER="${ADMIN_USER:-admin}"
if [ -z "$ADMIN_PASSWORD" ]; then
    echo "Please set ADMIN_PASSWORD, e.g.  ADMIN_PASSWORD='secret' $0"
    exit 2
fi

WORK_DIR="$(mktemp -d)"
ADMIN_COOKIES="$WORK_DIR/admin-cookies.txt"
PAGE="$WORK_DIR/page.html"
RUN_ID="$(date +%s | tail -c 6)"          # makes roll numbers unique per run
PASS_COUNT=0
FAIL_COUNT=0
CREATED_IDS=()

# ---------- helpers ----------
pass() { PASS_COUNT=$((PASS_COUNT + 1)); printf '  PASS  %-11s %s\n' "$1" "$2"; }
fail() { FAIL_COUNT=$((FAIL_COUNT + 1)); printf '  FAIL  %-11s %s  (got: %s)\n' "$1" "$2" "$3"; }

# expect_equal <test-id> <description> <actual> <expected>
expect_equal() { if [ "$3" = "$4" ]; then pass "$1" "$2"; else fail "$1" "$2" "$3"; fi; }

# expect_in_page <test-id> <description> <text that must appear in $PAGE>
expect_in_page() { if grep -qF -- "$3" "$PAGE"; then pass "$1" "$2"; else fail "$1" "$2" "text not found: $3"; fi; }

# expect_not_in_page <test-id> <description> <text that must NOT appear in $PAGE>
expect_not_in_page() { if grep -qF -- "$3" "$PAGE"; then fail "$1" "$2" "unexpected text: $3"; else pass "$1" "$2"; fi; }

# status <curl args...>  -> prints the HTTP status code (response body saved to $PAGE)
status() { curl -s -o "$PAGE" -w '%{http_code}' "$@"; }

# location <curl args...> -> prints "<status> <redirect path>" without following redirects
location() { curl -s -o "$PAGE" -w '%{http_code} %{redirect_url}' "$@" | sed "s#$BASE_URL##"; }

# make_student <suffix> [name=value ...]
# Fills the array FIELDS with the form fields of one test student.
# Extra name=value pairs replace a default field (e.g. "email=bad") or are added
# (e.g. "status=Active", "csrfToken=..."). Works with bash 3.2 (macOS) and newer.
make_student() {
    local suffix="$1"; shift
    local defaults=("fullName=Functional Test $suffix" "rollNumber=FT${RUN_ID}${suffix}"
                    "email=ft.${RUN_ID}.${suffix}@example.com" "phone=9811100000" "gender=Other"
                    "dob=2004-04-04" "department=Civil Engineering" "course=B.E." "semester=6"
                    "address=1 Test Road, Test City")
    local pair override
    FIELDS=()
    for pair in "${defaults[@]}"; do
        for override in "$@"; do
            [ "${override%%=*}" = "${pair%%=*}" ] && pair="$override"
        done
        FIELDS+=(--data-urlencode "$pair")
    done
    for override in "$@"; do
        case " fullName rollNumber email phone gender dob department course semester address " in
            *" ${override%%=*} "*) ;;                       # already used above
            *) FIELDS+=(--data-urlencode "$override") ;;   # an extra field
        esac
    done
}

# Reads the number from a registration ID such as SRS-00042 in $PAGE (prints 0 if none)
registration_number() {
    local reg_id
    reg_id=$(grep -oE 'SRS-[0-9]+' "$PAGE" | head -1 | sed 's/SRS-0*//')
    echo "${reg_id:-0}"
}

csrf_token() { grep -oE 'name="csrfToken" value="[^"]+"' "$PAGE" | head -1 | sed 's/.*value="//; s/"$//'; }

cleanup() {
    if [ ${#CREATED_IDS[@]} -gt 0 ] && [ -n "$TOKEN" ]; then
        for id in "${CREATED_IDS[@]}"; do
            curl -s -o /dev/null -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/students/delete" \
                 --data "id=$id&csrfToken=$TOKEN"
        done
    fi
    rm -rf "$WORK_DIR"
}
trap cleanup EXIT

echo "Testing $BASE_URL (run $RUN_ID)"
code=$(status "$BASE_URL/")
if [ "$code" != "200" ]; then
    echo "The application is not reachable at $BASE_URL (HTTP $code). Is Tomcat running?"
    exit 2
fi

# =====================================================================
echo; echo "1. Student registration"
# =====================================================================
REG_COOKIES="$WORK_DIR/student-cookies.txt"
expect_equal TC-REG-01a "Registration form opens" "$(status -c "$REG_COOKIES" "$BASE_URL/register")" "200"

make_student A "status=Inactive"
result=$(location -b "$REG_COOKIES" -c "$REG_COOKIES" -X POST "$BASE_URL/register" "${FIELDS[@]}")
expect_equal TC-REG-01b "Valid registration redirects to success page" "$result" "302 /register/success"
status -b "$REG_COOKIES" -c "$REG_COOKIES" "$BASE_URL/register/success" > /dev/null
expect_in_page TC-REG-01c "Success page shows a registration ID" "Your Registration ID"
NEW_ID=$(registration_number)
[ "$NEW_ID" -gt 0 ] && CREATED_IDS+=("$NEW_ID")
expect_equal TC-REG-10 "Refreshing the success page does not resubmit" \
    "$(location -b "$REG_COOKIES" "$BASE_URL/register/success")" "302 /register"

expect_equal TC-REG-02a "Empty form is rejected (stays on form)" "$(status -X POST "$BASE_URL/register" --data '')" "200"
expect_in_page TC-REG-02b "Empty form lists required-field errors" "Full name is required."

make_student B "email=not-an-email"
status -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
expect_in_page TC-REG-03 "Invalid email is rejected" "Please enter a valid email address"
make_student B "phone=12345"
status -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
expect_in_page TC-REG-04 "Invalid phone number is rejected" "Phone number must be 10 digits"
make_student B "rollNumber=ft${RUN_ID}a"
status -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
expect_in_page TC-REG-05 "Duplicate roll number is rejected (case-insensitive)" "This roll number is already registered."
make_student B "email=FT.${RUN_ID}.A@EXAMPLE.COM"
status -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
expect_in_page TC-REG-06 "Duplicate email is rejected (case-insensitive)" "This email address is already registered."
make_student B "semester=9"
status -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
expect_in_page TC-REG-07 "Semester outside 1-8 is rejected" "Semester must be between 1 and 8."
make_student B "dob=2099-01-01"
status -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
expect_in_page TC-REG-08 "Date of birth in the future is rejected" "Age must be between"
make_student B "department=Hacking 101"
status -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
expect_in_page TC-REG-12 "Department not in the list is rejected" "Please select a department from the list."

# =====================================================================
echo; echo "2. Authentication and access control"
# =====================================================================
expect_equal TC-AUTH-05 "Dashboard without login redirects to login" \
    "$(location "$BASE_URL/admin/dashboard")" "302 /login?required=true"
for path in /admin/students "/admin/students/view?id=1" /admin/students/add /admin/students/export; do
    expect_equal TC-SEC-04 "Protected without login: $path" "$(location "$BASE_URL$path")" "302 /login?required=true"
done

status -X POST "$BASE_URL/login" --data-urlencode "username=$ADMIN_USER" --data-urlencode "password=wrong-password-1" > /dev/null
expect_in_page TC-AUTH-02 "Wrong password is rejected" "Invalid username or password."
status -X POST "$BASE_URL/login" --data-urlencode "username=no_such_admin" --data-urlencode "password=whatever1" > /dev/null
expect_in_page TC-AUTH-03 "Unknown username gets the same message" "Invalid username or password."
status -X POST "$BASE_URL/login" --data "username=&password=" > /dev/null
expect_in_page TC-AUTH-04 "Empty login fields are rejected" "Please enter both username and password."
status -X POST "$BASE_URL/login" --data-urlencode "username=$ADMIN_USER' OR '1'='1" --data-urlencode "password=x" > /dev/null
expect_in_page TC-SEC-02a "SQL injection in username does not log in" "Invalid username or password."
status -X POST "$BASE_URL/login" --data-urlencode "username=$ADMIN_USER" --data-urlencode "password=' OR '1'='1" > /dev/null
expect_in_page TC-SEC-02b "SQL injection in password does not log in" "Invalid username or password."

curl -s -o /dev/null -c "$ADMIN_COOKIES" "$BASE_URL/register"
SESSION_BEFORE=$(grep JSESSIONID "$ADMIN_COOKIES" | awk '{print $7}')
result=$(location -b "$ADMIN_COOKIES" -c "$ADMIN_COOKIES" -X POST "$BASE_URL/login" \
         --data-urlencode "username=$ADMIN_USER" --data-urlencode "password=$ADMIN_PASSWORD")
expect_equal TC-AUTH-01 "Correct credentials redirect to dashboard" "$result" "302 /admin/dashboard"
if [ "$result" != "302 /admin/dashboard" ]; then
    echo "Cannot continue without a successful admin login. Check ADMIN_USER / ADMIN_PASSWORD."
    exit 1
fi
SESSION_AFTER=$(grep JSESSIONID "$ADMIN_COOKIES" | awk '{print $7}')
if [ "$SESSION_BEFORE" != "$SESSION_AFTER" ]; then pass TC-AUTH-08 "Session ID changes at login"; else fail TC-AUTH-08 "Session ID changes at login" "same id"; fi

headers=$(curl -s -D - -o "$PAGE" -b "$ADMIN_COOKIES" "$BASE_URL/admin/dashboard")
TOKEN=$(csrf_token)
expect_in_page TC-DASH-01a "Dashboard opens after login" "Total Students"
if echo "$headers" | grep -qi '^Cache-Control: no-cache, no-store'; then pass TC-AUTH-07 "Admin pages are not cached"; else fail TC-AUTH-07 "Admin pages are not cached" "header missing"; fi
if echo "$headers" | grep -qi '^X-Frame-Options: DENY'; then pass TC-SEC-09 "Anti-clickjacking header present"; else fail TC-SEC-09 "Anti-clickjacking header present" "header missing"; fi
expect_equal TC-AUTH-10 "Login page redirects when already logged in" \
    "$(location -b "$ADMIN_COOKIES" "$BASE_URL/login")" "302 /admin/dashboard"

# =====================================================================
echo; echo "3. Student management"
# =====================================================================
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=FT${RUN_ID}A" > /dev/null
expect_in_page TC-REG-01d "Registered student is stored and searchable by roll number" "Functional Test A"
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students/view?id=$NEW_ID" > /dev/null
expect_in_page TC-REG-09 "Forged 'status=Inactive' was ignored (stored as Active)" 'status-badge status-active'
expect_in_page TC-REG-11 "Roll number stored in upper case" "FT${RUN_ID}A"

status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students" > /dev/null
expect_in_page TC-MGT-01 "Student list shows records" 'class="student-cell"'
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=Functional+Test+A" > /dev/null
expect_in_page TC-MGT-02 "Search by name" "FT${RUN_ID}A"
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=FT${RUN_ID}A" > /dev/null
expect_in_page TC-MGT-03 "Search by roll number" "Functional Test A"
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=ft.${RUN_ID}.a%40example" > /dev/null
expect_in_page TC-MGT-04 "Search by email" "FT${RUN_ID}A"
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=FT${RUN_ID}&department=Civil+Engineering&semester=6" > /dev/null
expect_in_page TC-MGT-08 "Combined keyword + department + semester filter" "FT${RUN_ID}A"
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=FT${RUN_ID}&semester=2" > /dev/null
expect_in_page TC-MGT-06 "Semester filter excludes non-matching students" "No students match your search"
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=FT${RUN_ID}&department=Information+Technology" > /dev/null
expect_in_page TC-MGT-05 "Department filter excludes non-matching students" "No students match your search"
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=FT${RUN_ID}&status=Inactive" > /dev/null
expect_in_page TC-MGT-07 "Status filter excludes non-matching students" "No students match your search"
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=%27+OR+%271%27%3D%271" > /dev/null
expect_in_page TC-SEC-03 "SQL injection in search returns no rows" "No students match your search"

status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students/view?id=$NEW_ID" > /dev/null
expect_in_page TC-MGT-09 "Profile shows the student's registration ID" "SRS-$(printf '%05d' "$NEW_ID")"
expect_equal TC-MGT-10 "Profile with unknown ID gives 404" \
    "$(status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students/view?id=999999999")" "404"
expect_not_in_page TC-SEC-10 "Error page shows no Java stack trace" "Exception"

# Add a student as admin
make_student C "status=Active" "csrfToken=$TOKEN"
result=$(location -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/students/add" "${FIELDS[@]}")
ADDED_ID=$(echo "$result" | grep -oE 'id=[0-9]+' | cut -d= -f2)
[ -n "$ADDED_ID" ] && CREATED_IDS+=("$ADDED_ID")
expect_equal TC-MGT-11 "Admin can add a student" "$(echo "$result" | cut -d' ' -f1)" "302"

make_student D
expect_equal TC-SEC-06a "Add without CSRF token is refused" \
    "$(status -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/students/add" "${FIELDS[@]}")" "403"

# Edit
make_student C "fullName=Functional Test Edited" "semester=7" "status=Active" "id=$ADDED_ID" "csrfToken=$TOKEN"
result=$(location -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/students/edit" "${FIELDS[@]}")
expect_equal TC-MGT-12a "Edit saves and returns to profile" "$result" "302 /admin/students/view?id=$ADDED_ID"
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students/view?id=$ADDED_ID" > /dev/null
expect_in_page TC-MGT-12b "Edited name is shown" "Functional Test Edited"
expect_in_page TC-MGT-12c "Edited semester is shown" "Semester 7"
make_student C "rollNumber=FT${RUN_ID}A" "status=Active" "id=$ADDED_ID" "csrfToken=$TOKEN"
status -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/students/edit" "${FIELDS[@]}" > /dev/null
expect_in_page TC-MGT-13 "Edit to another student's roll number is rejected" "This roll number is already registered."

# Status
location -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/students/status" --data "id=$ADDED_ID&status=Inactive&csrfToken=$TOKEN" > /dev/null
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students/view?id=$ADDED_ID" > /dev/null
expect_in_page TC-MGT-14 "Status changed to Inactive" 'status-badge status-inactive'

# CSV export
csv_headers=$(curl -s -D - -o "$WORK_DIR/export.csv" -b "$ADMIN_COOKIES" "$BASE_URL/admin/students/export?keyword=FT${RUN_ID}")
if echo "$csv_headers" | grep -qi 'Content-Disposition: attachment; filename="students-'; then pass TC-MGT-17a "CSV is downloaded as a file"; else fail TC-MGT-17a "CSV is downloaded as a file" "no attachment header"; fi
expect_equal TC-MGT-17b "CSV contains header + 2 filtered rows" "$(wc -l < "$WORK_DIR/export.csv" | tr -d ' ')" "3"

# Delete
expect_equal TC-MGT-16 "Delete via GET is not allowed" \
    "$(status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students/delete?id=$ADDED_ID")" "405"
expect_equal TC-SEC-06b "Delete without CSRF token is refused" \
    "$(status -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/students/delete" --data "id=$ADDED_ID")" "403"
expect_equal TC-MGT-15a "Delete with token redirects to list" \
    "$(location -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/students/delete" --data "id=$ADDED_ID&csrfToken=$TOKEN")" "302 /admin/students"
expect_equal TC-MGT-15b "Deleted student no longer exists" \
    "$(status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students/view?id=$ADDED_ID")" "404"
# This student is gone now, so remove it from the clean-up list
remaining=(); for id in "${CREATED_IDS[@]}"; do [ "$id" != "$ADDED_ID" ] && remaining+=("$id"); done
CREATED_IDS=("${remaining[@]}")

# =====================================================================
echo; echo "4. Security: SQL injection and XSS in registration"
# =====================================================================
make_student E "fullName=Robert'); DROP TABLE students;--"
status -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
expect_in_page TC-SEC-01a "SQL in the name field is rejected by validation" "Name must be 2-100 characters"

XSS_COOKIES="$WORK_DIR/xss-cookies.txt"
make_student F "address=<script>alert('xss')</script> 1 Main St'); DROP TABLE students; --"
location -b "$XSS_COOKIES" -c "$XSS_COOKIES" -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
status -b "$XSS_COOKIES" "$BASE_URL/register/success" > /dev/null
XSS_ID=$(registration_number)
[ "$XSS_ID" -gt 0 ] && CREATED_IDS+=("$XSS_ID")
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students/view?id=$XSS_ID" > /dev/null
expect_in_page TC-SEC-01b "SQL text in address is stored as plain text (table not dropped)" "DROP TABLE students; --"
expect_in_page TC-SEC-07a "HTML in address is displayed escaped" "&lt;script&gt;"
expect_not_in_page TC-SEC-07b "No executable <script> from user input" "<script>alert("

# Duplicate submission: the same form posted twice
make_student G
first=$(location -X POST "$BASE_URL/register" "${FIELDS[@]}")
make_student G
status -X POST "$BASE_URL/register" "${FIELDS[@]}" > /dev/null
expect_equal TC-SEC-08a "First submission is saved" "$first" "302 /register/success"
expect_in_page TC-SEC-08b "Identical second submission is rejected as duplicate" "This roll number is already registered."
status -b "$ADMIN_COOKIES" "$BASE_URL/admin/students?keyword=FT${RUN_ID}G" > /dev/null
G_ID=$(grep -oE 'view\?id=[0-9]+' "$PAGE" | head -1 | cut -d= -f2)
[ -n "$G_ID" ] && CREATED_IDS+=("$G_ID")

# =====================================================================
echo; echo "5. Logout"
# =====================================================================
expect_equal TC-SEC-06c "Logout without CSRF token is refused" \
    "$(status -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/logout")" "403"
# Delete the test students now, while still logged in, and check the dashboard total drops
dashboard_total() { status -b "$ADMIN_COOKIES" "$BASE_URL/admin/dashboard" > /dev/null; grep -oE 'stat-value">[0-9]+' "$PAGE" | head -1 | grep -oE '[0-9]+$'; }
TOTAL_BEFORE=$(dashboard_total)
cleanup_ids=("${CREATED_IDS[@]}"); CREATED_IDS=()
for id in "${cleanup_ids[@]}"; do
    curl -s -o /dev/null -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/students/delete" --data "id=$id&csrfToken=$TOKEN"
done
expect_equal TC-DASH-02 "Dashboard total drops by ${#cleanup_ids[@]} after deleting test students" \
    "$(dashboard_total)" "$((TOTAL_BEFORE - ${#cleanup_ids[@]}))"
expect_equal TC-AUTH-06a "Logout redirects to login page" \
    "$(location -b "$ADMIN_COOKIES" -X POST "$BASE_URL/admin/logout" --data "csrfToken=$TOKEN")" "302 /login?logout=true"
expect_equal TC-AUTH-06b "Old session cannot open the dashboard after logout" \
    "$(location -b "$ADMIN_COOKIES" "$BASE_URL/admin/dashboard")" "302 /login?required=true"

# =====================================================================
echo
echo "Result: $PASS_COUNT passed, $FAIL_COUNT failed"
[ "$FAIL_COUNT" -eq 0 ]
