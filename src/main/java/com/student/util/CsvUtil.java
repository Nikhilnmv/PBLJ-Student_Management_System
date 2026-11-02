package com.student.util;

/**
 * Helpers for writing CSV (Comma-Separated Values) files that open
 * correctly in Microsoft Excel, LibreOffice and Google Sheets.
 */
public final class CsvUtil {

    private CsvUtil() {
    }

    /**
     * Makes one value safe to put in a CSV cell.
     *
     * 1. Values starting with = + - @ are prefixed with ' so a spreadsheet
     *    does not run them as formulas ("CSV injection").
     * 2. Values containing a comma, quote or line break are wrapped in double
     *    quotes, and any quote inside is doubled ("" ), as the CSV format requires.
     */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        String result = value;
        if (!result.isEmpty() && "=+-@\t\r".indexOf(result.charAt(0)) >= 0) {
            result = "'" + result;
        }
        if (result.contains(",") || result.contains("\"") || result.contains("\n") || result.contains("\r")) {
            result = "\"" + result.replace("\"", "\"\"") + "\"";
        }
        return result;
    }

    /** Joins several values into one CSV line, escaping each value. */
    public static String toCsvLine(String... values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escape(values[i]));
        }
        return line.toString();
    }
}
