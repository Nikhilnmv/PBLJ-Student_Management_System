package com.student.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Unit tests for CSV escaping. */
class CsvUtilTest {

    @Test
    void plainValuesAreUnchanged() {
        assertEquals("Aarav Mehta", CsvUtil.escape("Aarav Mehta"));
        assertEquals("", CsvUtil.escape(null));
    }

    @Test
    void valuesWithCommasQuotesOrNewLinesAreQuoted() {
        assertEquals("\"12 Park Road, Pune\"", CsvUtil.escape("12 Park Road, Pune"));
        assertEquals("\"He said \"\"hi\"\"\"", CsvUtil.escape("He said \"hi\""));
        assertEquals("\"Line 1\nLine 2\"", CsvUtil.escape("Line 1\nLine 2"));
    }

    @Test
    void spreadsheetFormulasAreDisarmed() {
        assertEquals("'=SUM(A1:A9)", CsvUtil.escape("=SUM(A1:A9)"));
        assertEquals("'+91", CsvUtil.escape("+91"));
        assertEquals("'@cmd", CsvUtil.escape("@cmd"));
        assertEquals("\"'=HYPERLINK(\"\"x\"\",\"\"y\"\")\"", CsvUtil.escape("=HYPERLINK(\"x\",\"y\")"));
    }

    @Test
    void lineJoinsEscapedValuesWithCommas() {
        assertEquals("SRS-00001,\"Pune, MH\",Active", CsvUtil.toCsvLine("SRS-00001", "Pune, MH", "Active"));
    }
}
