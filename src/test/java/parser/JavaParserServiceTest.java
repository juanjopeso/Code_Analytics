package parser;

import static org.junit.jupiter.api.Assertions.*;

import ia.AIRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Tests for {@link JavaParserService}.
 *
 * Uses a stub AI service so no network calls are made.
 * A temporary directory with sample .java files is created per test.
 */
@DisplayName("JavaParserService")
class JavaParserServiceTest {

    /** Stub that returns a deterministic AI response — no HTTP call. */
    private static final AIRecommendationService STUB_AI =
            ctx -> "Sugerencia simulada de IA.";

    private JavaParserService parser;

    @BeforeEach
    void setUp() {
        parser = new JavaParserService(STUB_AI);
    }

    // ── parseFile ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should return empty list when file path does not exist")
    void shouldReturnEmpty_WhenFileNotFound() {
        List<JavaParserService.ClassAnalysisResult> results =
                parser.parseFile("/nonexistent/path/Ghost.java");

        assertNotNull(results, "Result list must never be null");
        assertTrue(results.isEmpty(), "Non-existent file should yield no results");
    }

    @Test
    @DisplayName("should parse a simple class and return one ClassAnalysisResult")
    void shouldReturnOneResult_WhenFileHasOneClass(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("Simple.java");
        Files.writeString(file, """
                public class Simple {
                    public int add(int a, int b) {
                        return a + b;
                    }
                }
                """);

        List<JavaParserService.ClassAnalysisResult> results = parser.parseFile(file.toString());

        assertEquals(1, results.size(), "One class in the file should yield one result");
        assertEquals("Simple", results.get(0).className());
    }

    @Test
    @DisplayName("should report a clean score for a trivial method")
    void shouldReportHighScore_WhenMethodIsClean(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("Clean.java");
        Files.writeString(file, """
                public class Clean {
                    public int square(int x) {
                        return x * x;
                    }
                }
                """);

        JavaParserService.ClassAnalysisResult result =
                parser.parseFile(file.toString()).get(0);

        assertTrue(result.score() >= 80, "A clean class should score >= 80, got " + result.score());
        assertEquals(0, result.badMethodCount(), "Clean class should have 0 bad methods");
    }

    @Test
    @DisplayName("should detect issues in a complex method")
    void shouldDetectIssues_WhenMethodIsComplex(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("Complex.java");
        // Build a method that is long and has high CC
        StringBuilder sb = new StringBuilder("public class Complex { public void doWork(int n) {\n");
        for (int i = 0; i < 60; i++) sb.append("  if (n > ").append(i).append(") { n--; }\n");
        sb.append("}}");
        Files.writeString(file, sb.toString());

        JavaParserService.ClassAnalysisResult result =
                parser.parseFile(file.toString()).get(0);

        assertTrue(result.badMethodCount() >= 1, "A method with 60 ifs must be flagged");
        assertTrue(result.score() < 100,         "Score must be reduced for problematic method");
    }

    @Test
    @DisplayName("should attach AI suggestion to issues when stub AI is wired in")
    void shouldAttachAISuggestion_WhenIssuesExist(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("WithIssues.java");
        StringBuilder sb = new StringBuilder("public class WithIssues { public void big() {\n");
        for (int i = 0; i < 60; i++) sb.append("  int x").append(i).append(" = ").append(i).append(";\n");
        sb.append("}}");
        Files.writeString(file, sb.toString());

        JavaParserService.ClassAnalysisResult classResult =
                parser.parseFile(file.toString()).get(0);

        // Find the first method that has at least one issue
        classResult.methods().stream()
                .filter(m -> !m.evaluation().getIssues().isEmpty())
                .findFirst()
                .ifPresent(m -> {
                    String suggestion = m.evaluation().getIssues().get(0).getSuggestion();
                    assertEquals("Sugerencia simulada de IA.", suggestion,
                            "The stub AI suggestion should be attached to the issue");
                });
    }

    // ── ClassAnalysisResult helper methods ────────────────────────────────────

    @Test
    @DisplayName("ClassAnalysisResult.isGodClass() should return false for clean class")
    void shouldNotBeGodClass_WhenBadMethodCountIsLow(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("Small.java");
        Files.writeString(file, """
                public class Small {
                    public int a() { return 1; }
                    public int b() { return 2; }
                }
                """);

        JavaParserService.ClassAnalysisResult result =
                parser.parseFile(file.toString()).get(0);

        assertFalse(result.isGodClass(), "A class with 2 simple methods should not be a God Class");
    }

    @Test
    @DisplayName("ClassAnalysisResult.level() should return BUENO for score >= 80")
    void shouldReturnBueno_WhenScoreIsHighEnough(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("Good.java");
        Files.writeString(file, """
                public class Good {
                    public String greet(String name) {
                        return "Hello, " + name;
                    }
                }
                """);

        JavaParserService.ClassAnalysisResult result =
                parser.parseFile(file.toString()).get(0);

        assertEquals("BUENO", result.level(), "Score >= 80 should map to BUENO level");
    }
}