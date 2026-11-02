package com.student.dao;

import com.student.model.Admin;
import com.student.model.Student;
import com.student.model.StudentFilter;
import com.student.util.DBConnection;
import com.student.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * INTEGRATION tests: run StudentDAO and AdminDAO against the REAL MySQL database.
 *
 * They are skipped by a normal "mvn test". To run them, MySQL must be running,
 * schema.sql imported and db.properties (or DB_* variables) configured, then:
 *
 *     mvn test -Dintegration=true
 *
 * Every test record uses a roll number starting with "ITEST" (and admin
 * usernames starting with "itest_"), and they are all deleted after each
 * test, so your real data is never changed.
 */
@EnabledIfSystemProperty(named = "integration", matches = "true")
class StudentDAOIntegrationTest {

    private final StudentDAO studentDAO = new StudentDAO();
    private final AdminDAO adminDAO = new AdminDAO();

    /** Builds a valid test student; roll and email must be unique per test. */
    private Student testStudent(String rollSuffix, String department, int semester) {
        return new Student("ITEST" + rollSuffix, "Itest Person " + rollSuffix,
                "itest." + rollSuffix.toLowerCase() + "@example.com", "9000099999", "Female",
                LocalDate.of(2005, 6, 15), department, "B.E.", semester, "1 Test Road, Test City");
    }

    @AfterEach
    void deleteTestRows() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM students WHERE roll_number LIKE 'ITEST%'");
            statement.executeUpdate("DELETE FROM admin WHERE username LIKE 'itest\\_%'");
        }
    }

    @Test
    @DisplayName("INSERT returns a generated ID and SELECT reads the same values back")
    void addAndReadStudent() throws Exception {
        Student student = testStudent("001", "Civil Engineering", 2);
        int id = studentDAO.addStudent(student);
        assertTrue(id > 0);

        Student saved = studentDAO.getStudentById(id);
        assertNotNull(saved);
        assertEquals("ITEST001", saved.getRollNumber());
        assertEquals("Itest Person 001", saved.getFullName());
        assertEquals(LocalDate.of(2005, 6, 15), saved.getDob());
        assertEquals("Civil Engineering", saved.getDepartment());
        assertEquals(2, saved.getSemester());
        assertEquals("Active", saved.getStatus());
        assertNotNull(saved.getCreatedAt(), "created_at is filled in by MySQL");
    }

    @Test
    void unknownIdReturnsNull() throws SQLException {
        assertNull(studentDAO.getStudentById(Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("The UNIQUE constraint rejects a duplicate roll number")
    void duplicateRollNumberIsRejectedByDatabase() throws Exception {
        studentDAO.addStudent(testStudent("002", "Civil Engineering", 1));
        Student duplicate = testStudent("002", "Civil Engineering", 1);
        duplicate.setEmail("different.email@example.com");

        DuplicateStudentException e = assertThrows(DuplicateStudentException.class,
                () -> studentDAO.addStudent(duplicate));
        assertEquals("rollNumber", e.getField());
    }

    @Test
    @DisplayName("The UNIQUE constraint rejects a duplicate email (case-insensitive)")
    void duplicateEmailIsRejectedByDatabase() throws Exception {
        studentDAO.addStudent(testStudent("003", "Civil Engineering", 1));
        Student duplicate = testStudent("004", "Civil Engineering", 1);
        duplicate.setEmail("ITEST.003@EXAMPLE.COM");  // same email, different letter case

        DuplicateStudentException e = assertThrows(DuplicateStudentException.class,
                () -> studentDAO.addStudent(duplicate));
        assertEquals("email", e.getField());
    }

    @Test
    void duplicateChecksIgnoreTheStudentBeingEdited() throws Exception {
        int id = studentDAO.addStudent(testStudent("005", "Civil Engineering", 1));
        assertTrue(studentDAO.isRollNumberTaken("ITEST005", 0));
        assertFalse(studentDAO.isRollNumberTaken("ITEST005", id));
        assertTrue(studentDAO.isEmailTaken("itest.005@example.com", 0));
        assertFalse(studentDAO.isEmailTaken("itest.005@example.com", id));
        assertFalse(studentDAO.isRollNumberTaken("ITEST999", 0));
    }

    @Test
    @DisplayName("The CHECK constraint rejects semester 9 even if Java validation is skipped")
    void checkConstraintRejectsInvalidSemester() {
        Student invalid = testStudent("006", "Civil Engineering", 9);
        assertThrows(SQLException.class, () -> studentDAO.addStudent(invalid));
    }

    @Test
    void updateStudentChangesTheRow() throws Exception {
        int id = studentDAO.addStudent(testStudent("007", "Civil Engineering", 1));
        Student student = studentDAO.getStudentById(id);
        student.setFullName("Itest Updated Name");
        student.setSemester(4);
        student.setStatus("Inactive");

        assertTrue(studentDAO.updateStudent(student));
        Student updated = studentDAO.getStudentById(id);
        assertEquals("Itest Updated Name", updated.getFullName());
        assertEquals(4, updated.getSemester());
        assertEquals("Inactive", updated.getStatus());
    }

    @Test
    void updatingAMissingStudentReturnsFalse() throws Exception {
        Student ghost = testStudent("008", "Civil Engineering", 1);
        ghost.setStudentId(Integer.MAX_VALUE);
        assertFalse(studentDAO.updateStudent(ghost));
    }

    @Test
    void updateStatusAndDelete() throws Exception {
        int id = studentDAO.addStudent(testStudent("009", "Civil Engineering", 1));
        assertTrue(studentDAO.updateStatus(id, "Inactive"));
        assertEquals("Inactive", studentDAO.getStudentById(id).getStatus());

        assertTrue(studentDAO.deleteStudent(id));
        assertNull(studentDAO.getStudentById(id));
        assertFalse(studentDAO.deleteStudent(id), "deleting twice finds nothing");
    }

    @Test
    void searchAndFilters() throws Exception {
        studentDAO.addStudent(testStudent("010", "Electrical Engineering", 2));
        studentDAO.addStudent(testStudent("011", "Electrical Engineering", 6));
        studentDAO.addStudent(testStudent("012", "Civil Engineering", 2));

        StudentFilter byKeyword = new StudentFilter("Itest Person", null, null, null);
        assertEquals(3, studentDAO.countStudents(byKeyword));

        StudentFilter byRoll = new StudentFilter("ITEST011", null, null, null);
        assertEquals("ITEST011", studentDAO.searchStudents(byRoll).get(0).getRollNumber());

        StudentFilter byEmail = new StudentFilter("itest.012@", null, null, null);
        assertEquals(1, studentDAO.countStudents(byEmail));

        StudentFilter keywordAndDepartment = new StudentFilter("Itest", "Electrical Engineering", null, null);
        assertEquals(2, studentDAO.countStudents(keywordAndDepartment));

        StudentFilter keywordAndSemester = new StudentFilter("Itest", null, 2, null);
        assertEquals(2, studentDAO.countStudents(keywordAndSemester));

        StudentFilter allFour = new StudentFilter("Itest", "Electrical Engineering", 6, "Active");
        List<Student> found = studentDAO.searchStudents(allFour);
        assertEquals(1, found.size());
        assertEquals("ITEST011", found.get(0).getRollNumber());
    }

    @Test
    void pagingReturnsTheRightSlice() throws Exception {
        for (int i = 20; i < 25; i++) {
            studentDAO.addStudent(testStudent("0" + i, "Civil Engineering", 1));
        }
        StudentFilter filter = new StudentFilter("Itest Person", null, null, null);
        assertEquals(2, studentDAO.searchStudents(filter, 0, 2).size());
        assertEquals(2, studentDAO.searchStudents(filter, 2, 2).size());
        assertEquals(1, studentDAO.searchStudents(filter, 4, 2).size());
        assertEquals(5, studentDAO.searchStudents(filter).size());
    }

    @Test
    @DisplayName("SQL injection text is treated as plain text by PreparedStatement")
    void sqlInjectionIsTreatedAsText() throws Exception {
        int totalBefore = studentDAO.countAllStudents();
        StudentFilter attack = new StudentFilter("' OR '1'='1", null, null, null);
        assertEquals(0, studentDAO.countStudents(attack), "must NOT return every row");

        // A name containing an apostrophe is stored and found correctly
        Student student = testStudent("030", "Civil Engineering", 1);
        student.setFullName("Itest O'Brien");
        int id = studentDAO.addStudent(student);
        assertEquals("Itest O'Brien", studentDAO.getStudentById(id).getFullName());
        assertEquals(1, studentDAO.countStudents(new StudentFilter("O'Brien", null, null, null)));

        // The students table still exists and still has the old rows plus one
        assertEquals(totalBefore + 1, studentDAO.countAllStudents());
    }

    @Test
    void likeWildcardsAreSearchedLiterally() throws Exception {
        studentDAO.addStudent(testStudent("040", "Civil Engineering", 1));
        assertEquals(0, studentDAO.countStudents(new StudentFilter("%", null, null, null)));
        assertEquals(0, studentDAO.countStudents(new StudentFilter("ITEST_40", null, null, null)));
    }

    @Test
    @DisplayName("Dashboard statistics change when a student is added")
    void statisticsReflectNewRows() throws Exception {
        int total = studentDAO.countAllStudents();
        int active = studentDAO.countStudentsByStatus("Active");
        int inactive = studentDAO.countStudentsByStatus("Inactive");
        assertEquals(total, active + inactive);

        Student student = testStudent("050", "Civil Engineering", 1);
        student.setStatus("Inactive");
        int id = studentDAO.addStudent(student);

        assertEquals(total + 1, studentDAO.countAllStudents());
        assertEquals(inactive + 1, studentDAO.countStudentsByStatus("Inactive"));
        assertEquals(active, studentDAO.countStudentsByStatus("Active"));

        Map<String, Integer> byDepartment = studentDAO.getDepartmentCounts();
        assertTrue(byDepartment.get("Civil Engineering") >= 1);
        assertEquals(studentDAO.countDepartments(), byDepartment.size());

        assertEquals(id, studentDAO.getRecentStudents(1).get(0).getStudentId(), "newest first");
    }

    @Test
    @DisplayName("AdminDAO stores and finds an admin; the password hash verifies")
    void adminCreateFindAndReset() throws Exception {
        assertNull(adminDAO.findByUsername("itest_admin"));
        int adminsBefore = adminDAO.countAdmins();
        adminDAO.createAdmin("itest_admin", PasswordUtil.hashPassword("Initial123"));
        assertEquals(adminsBefore + 1, adminDAO.countAdmins());

        Admin admin = adminDAO.findByUsername("itest_admin");
        assertNotNull(admin);
        assertTrue(PasswordUtil.checkPassword("Initial123", admin.getPasswordHash()));

        assertTrue(adminDAO.updatePassword("itest_admin", PasswordUtil.hashPassword("Changed456")));
        Admin reset = adminDAO.findByUsername("itest_admin");
        assertFalse(PasswordUtil.checkPassword("Initial123", reset.getPasswordHash()));
        assertTrue(PasswordUtil.checkPassword("Changed456", reset.getPasswordHash()));

        assertThrows(SQLException.class,
                () -> adminDAO.createAdmin("itest_admin", PasswordUtil.hashPassword("Other789")),
                "username is UNIQUE");
        assertNull(adminDAO.findByUsername("itest_admin' OR '1'='1"), "SQL injection finds nothing");
    }
}
