package metrics;

import static org.junit.jupiter.api.Assertions.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LocMetric}.
 *
 * Naming convention: shouldBehavior_WhenCondition
 */
@DisplayName("LocMetric")
class LocMetricTest {

    private LocMetric metric;

    @BeforeEach
    void setUp() {
        metric = new LocMetric();
    }

    // ── Tests existentes (mejorados) ──────────────────────────────────────────

    @Test
    @DisplayName("should count lines of a normal method correctly")
    void shouldCalculateLoc_WhenMethodHasMultipleStatements() {
        String code = """
                class A {
                    void test() {
                        int a = 1;
                        int b = 2;
                        int c = a + b;
                    }
                }
                """;

        MethodDeclaration method = parseFirstMethod(code);
        assertEquals(5, metric.calculate(method));
    }

    // ── Tests nuevos ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("should return 1 for a single-line empty method body")
    void shouldReturnOne_WhenMethodBodyHasOneStatement() {
        String code = """
                class A {
                    void empty() { return; }
                }
                """;

        MethodDeclaration method = parseFirstMethod(code);
        int loc = metric.calculate(method);
        assertTrue(loc >= 1, "Single-statement method should have at least 1 LOC");
    }

    @Test
    @DisplayName("should return 0 for abstract method (no body / range unavailable)")
    void shouldReturnZero_WhenMethodHasNoRange() {
        // Abstract methods parsed from interface declarations have no body range
        String code = """
                interface I {
                    void noBody();
                }
                """;

        MethodDeclaration method = parseFirstMethod(code);
        assertEquals(0, metric.calculate(method),
                "A method without a parseable range should return 0");
    }

    @Test
    @DisplayName("should detect a long method above the 50-line threshold")
    void shouldReportHighLoc_WhenMethodExceedsFiftyLines() {
        // Generates a method with 55 assignment statements
        StringBuilder builder = new StringBuilder("class A { void longMethod() {\n");
        for (int i = 0; i < 55; i++) {
            builder.append("    int v").append(i).append(" = ").append(i).append(";\n");
        }
        builder.append("}}");

        MethodDeclaration method = parseFirstMethod(builder.toString());
        assertTrue(metric.calculate(method) > 50,
                "Method with 55 statements must exceed 50 LOC");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private MethodDeclaration parseFirstMethod(String code) {
        return StaticJavaParser.parse(code)
                               .findFirst(MethodDeclaration.class)
                               .orElseThrow(() -> new AssertionError("No method found in snippet"));
    }
}