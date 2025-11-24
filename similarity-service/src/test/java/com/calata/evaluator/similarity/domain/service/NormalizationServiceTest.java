package com.calata.evaluator.similarity.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizationServiceTest {

    private NormalizationService service;

    @BeforeEach
    void setUp() {
        service = new NormalizationService();
    }

    // ---------- JAVA: block comments ----------

    @Test
    void normalize_java_shouldRemoveBlockComments() {
        String code = """
                /* This is a comment */
                int x = 10;
                """;

        String result = service.normalize(code, "JAVA");

        assertEquals("int x = 10;", result);
    }

    // ---------- JAVA: line comments ----------

    @Test
    void normalize_java_lineComments_shouldRemoveOnlyLastComment() {
        String code = """
                int x = 10; // inline comment
                int y = 20; // another comment
                """;

        String result = service.normalize(code, "JAVA");

        // Se conserva el primer comentario, se elimina el segundo
        assertEquals("int x = 10; // inline comment int y = 20;", result);
    }

    // ---------- JAVA: strings ----------

    @Test
    void normalize_java_shouldNormalizeStringsTo_S() {
        String code = """
                String s = "hello";
                System.out.println("world!");
                """;

        String result = service.normalize(code, "JAVA");

        assertEquals("string s = \"s\"; system.out.println(\"s\");", result);
    }

    // ---------- PYTHON: comentarios (solo se elimina el último) ----------

    @Test
    void normalize_python_lineComments_shouldRemoveOnlyLastComment() {
        String code = """
                x = 10   # comment here
                y = 20   # another
                """;

        String result = service.normalize(code, "PYTHON");

        // Se conserva el primer comentario, se elimina el segundo
        assertEquals("x = 10 # comment here y = 20", result);
    }

    @Test
    void normalize_python_shouldNormalizeStrings() {
        String code = """
                s = "hello"
                print("value:", s)
                """;

        String result = service.normalize(code, "PYTHON");

        assertEquals("s = \"s\" print(\"s\", s)", result);
    }

    // ---------- DEFAULT (lenguaje desconocido) ----------

    @Test
    void normalize_defaultLanguage_shouldOnlyNormalizeWhitespaceAndLowercase() {
        String code = "  ABC   DEF   XYZ  ";
        String result = service.normalize(code, "UNKNOWN");
        assertEquals("abc def xyz", result);
    }

    // ---------- whitespace + lowercase ----------

    @Test
    void normalize_shouldCompressWhitespaceAndLowercaseResult() {
        String code = """
                int     X   =    10 ;
                """;

        String result = service.normalize(code, "JAVA");

        assertEquals("int x = 10 ;", result);
    }

    @Test
    void normalize_shouldTrimAndLowercase() {
        String code = "   INT X = 10;   ";
        String result = service.normalize(code, "JAVA");
        assertEquals("int x = 10;", result);
    }

    // ---------- Combined Case ----------

    @Test
    void normalize_shouldHandleCombinedJavaScenario() {
        String code = """
                /* comment */
                String a = "Hello";   // hi
                int x = 20; /* block comment */
                """;

        String result = service.normalize(code, "JAVA");

        // Solo se eliminan comentarios de bloque; el inline se mantiene
        assertEquals("string a = \"s\"; // hi int x = 20;", result);
    }
}
