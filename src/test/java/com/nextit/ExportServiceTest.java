package com.nextit;

import com.nextit.util.ExportService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExportServiceTest {

    @Test
    public void testEscapeCsv() throws Exception {
        Method escapeCsv = ExportService.class.getDeclaredMethod("escapeCsv", String.class);
        escapeCsv.setAccessible(true);

        assertEquals("", escapeCsv.invoke(null, (String) null));
        assertEquals("Simple", escapeCsv.invoke(null, "Simple"));
        assertEquals("\"With, Comma\"", escapeCsv.invoke(null, "With, Comma"));
        assertEquals("\"With \"\"Quotes\"\"\"", escapeCsv.invoke(null, "With \"Quotes\""));
    }

    @Test
    public void testEscapeHtml() throws Exception {
        Method escapeHtml = ExportService.class.getDeclaredMethod("escapeHtml", String.class);
        escapeHtml.setAccessible(true);

        assertEquals("", escapeHtml.invoke(null, (String) null));
        assertEquals("&lt;script&gt;", escapeHtml.invoke(null, "<script>"));
        assertEquals("Grade &amp; Score", escapeHtml.invoke(null, "Grade & Score"));
    }
}

