package metrics;

import static org.junit.jupiter.api.Assertions.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CyclomaticComplexityMetric")
class CyclomaticComplexityMetricTest {

    private CyclomaticComplexityMetric metric;

    @BeforeEach
    void setUp() {
        metric = new CyclomaticComplexityMetric();
    }

    // ── Tests existentes (mejorados) ──────────────────────────────────────────

    @Test
    @DisplayName("should return complexity >= 2 for a method with one if-else")
    void shouldCalculateComplexity_WhenMethodHasOneIfElse() {
        String code = """
                class A {
                    void test(int x) {
                        if (x > 0) {
                            System.out.println(x);
                        } else {
                            System.out.println(-x);
                        }
                    }
                }
                """;

        int cc = metric.calculate(parseFirstMethod(code));
        assertTrue(cc >= 2, "A method with one if-else should have CC >= 2");
    }

    // ── Tests nuevos ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("should return base complexity of 1 for a trivial method")
    void shouldReturnOne_WhenMethodHasNoBranches() {
        String code = """
                class A {
                    int add(int a, int b) {
                        return a + b;
                    }
                }
                """;

        assertEquals(1, metric.calculate(parseFirstMethod(code)),
                "A straight-line method has CC = 1 (base complexity)");
    }

    @Test
    @DisplayName("should accumulate complexity for nested if inside for loop")
    void shouldAccumulateComplexity_WhenBranchesAreNested() {
        String code = """
                class A {
                    void nested(int n) {
                        for (int i = 0; i < n; i++) {
                            if (i % 2 == 0) {
                                System.out.println(i);
                            }
                        }
                    }
                }
                """;

        // 1 base + 1 for + 1 if = 3
        int cc = metric.calculate(parseFirstMethod(code));
        assertTrue(cc >= 3, "Nested branch+loop should produce CC >= 3, got " + cc);
    }

    @Test
    @DisplayName("should count each catch clause as +1 complexity")
    void shouldCountCatchClause_AsComplexityBranch() {
        String code = """
                class A {
                    void tryCatch() {
                        try {
                            int x = 1 / 0;
                        } catch (ArithmeticException e) {
                            System.out.println("caught");
                        }
                    }
                }
                """;

        // 1 base + 1 catch = 2
        int cc = metric.calculate(parseFirstMethod(code));
        assertTrue(cc >= 2, "A try-catch block should have CC >= 2, got " + cc);
    }

    @Test
    @DisplayName("should accumulate all branch types in a risky method")
    void shouldProduceHighComplexity_WhenMethodHasManyBranches() {
        // A method with 3 if, 2 for, 1 while, 1 catch = at least 8 branches + base
        String code = """
                class A {
                    void risky(int n) {
                        for (int i = 0; i < n; i++) {
                            if (i > 0) { }
                        }
                        for (int j = 0; j < n; j++) {
                            if (j % 2 == 0) { }
                        }
                        while (n > 0) { n--; }
                        if (n == 0) { }
                        try { int x = 1 / 0; } catch (Exception e) { }
                    }
                }
                """;

        int cc = metric.calculate(parseFirstMethod(code));
        assertTrue(cc >= 7, "Method with multiple branches should have CC >= 7, got " + cc);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private MethodDeclaration parseFirstMethod(String code) {
        return StaticJavaParser.parse(code)
                               .findFirst(MethodDeclaration.class)
                               .orElseThrow(() -> new AssertionError("No method found in snippet"));
    }
}