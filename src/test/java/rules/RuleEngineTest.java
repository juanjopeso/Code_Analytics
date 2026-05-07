package rules;

import static org.junit.jupiter.api.Assertions.*;
import metrics.MethodMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("RuleEngine")
class RuleEngineTest {

    private RuleEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RuleEngine();
    }

    // ── Tests existentes (mejorados) ──────────────────────────────────────────

    @Test
    @DisplayName("should detect issues and reduce score for a problematic method")
    void shouldEvaluateMethodAndReturnIssues() {
        MethodMetrics metrics = new MethodMetrics("processOrder", 100, 20);
        EvaluationResult result = engine.evaluate(metrics);

        assertFalse(result.getIssues().isEmpty(), "A method with LOC=100 and CC=20 must have issues");
        assertTrue(result.getScore() < 100,       "Score must be reduced when issues are found");
    }

    @Test
    @DisplayName("should give a high score with no issues for a clean method")
    void shouldReturnGoodScoreForCleanMethod() {
        MethodMetrics metrics = new MethodMetrics("add", 5, 1);
        EvaluationResult result = engine.evaluate(metrics);

        assertTrue(result.getIssues().isEmpty(), "A trivial method should have no issues");
        assertTrue(result.getScore() >= 80,      "A trivial method should score at least 80");
    }

    // ── Tests nuevos ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("should never produce a negative score")
    void shouldClampScoreToZero_WhenPenaltiesExceed100() {
        // Enormous LOC and CC to guarantee cumulative penalties exceed 100
        MethodMetrics metrics = new MethodMetrics("monster", 500, 50);
        EvaluationResult result = engine.evaluate(metrics);

        assertTrue(result.getScore() >= 0, "Score must never go below 0");
    }

    @Test
    @DisplayName("should assign exactly 100 score when method is perfect")
    void shouldReturn100_WhenMethodHasNoIssuesAtAll() {
        MethodMetrics metrics = new MethodMetrics("simple", 3, 1);
        EvaluationResult result = engine.evaluate(metrics);

        assertEquals(100, result.getScore(), "A method with no issues should score exactly 100");
    }

    @Test
    @DisplayName("should flag LongMethod when LOC exceeds 50")
    void shouldFlagLongMethod_WhenLocExceedsFifty() {
        MethodMetrics metrics = new MethodMetrics("bigMethod", 60, 2);
        EvaluationResult result = engine.evaluate(metrics);

        boolean hasLongMethod = result.getIssues().stream()
                .anyMatch(i -> "Long Method".equals(i.getType()));
        assertTrue(hasLongMethod, "LOC=60 should trigger Long Method rule");
    }

    @Test
    @DisplayName("should flag HighComplexity when CC >= 8")
    void shouldFlagHighComplexity_WhenCCIsEightOrMore() {
        MethodMetrics metrics = new MethodMetrics("complexMethod", 10, 8);
        EvaluationResult result = engine.evaluate(metrics);

        boolean hasHighCC = result.getIssues().stream()
                .anyMatch(i -> "High Complexity".equals(i.getType()));
        assertTrue(hasHighCC, "CC=8 should trigger High Complexity rule");
    }

    @Test
    @DisplayName("should flag ModerateComplexity when CC is between 5 and 7")
    void shouldFlagModerateComplexity_WhenCCIsBetweenFiveAndSeven() {
        MethodMetrics metrics = new MethodMetrics("moderate", 10, 6);
        EvaluationResult result = engine.evaluate(metrics);

        boolean hasModerate = result.getIssues().stream()
                .anyMatch(i -> "Moderate Complexity".equals(i.getType()));
        assertTrue(hasModerate, "CC=6 should trigger Moderate Complexity rule");
    }

    @Test
    @DisplayName("should flag RiskyMethod when both LOC > 30 and CC >= 5")
    void shouldFlagRiskyMethod_WhenBothConditionsAreMet() {
        MethodMetrics metrics = new MethodMetrics("risky", 40, 6);
        EvaluationResult result = engine.evaluate(metrics);

        boolean hasRisky = result.getIssues().stream()
                .anyMatch(i -> "Risky Method".equals(i.getType()));
        assertTrue(hasRisky, "LOC=40 and CC=6 should trigger Risky Method rule");
    }

    @Test
    @DisplayName("should not flag HighComplexity when CC is exactly 7")
    void shouldNotFlagHighComplexity_WhenCCIsBelowThreshold() {
        MethodMetrics metrics = new MethodMetrics("borderline", 10, 7);
        EvaluationResult result = engine.evaluate(metrics);

        boolean hasHighCC = result.getIssues().stream()
                .anyMatch(i -> "High Complexity".equals(i.getType()));
        assertFalse(hasHighCC, "CC=7 should NOT trigger High Complexity (threshold is 8)");
    }

    @Test
    @DisplayName("should accumulate multiple issues for a god-candidate method")
    void shouldAccumulateMultipleIssues_WhenMethodViolatesAllRules() {
        MethodMetrics metrics = new MethodMetrics("doEverything", 80, 12);
        EvaluationResult result = engine.evaluate(metrics);

        List<rules.QualityIssue> issues = result.getIssues();
        assertTrue(issues.size() >= 2,
                "A method with LOC=80 and CC=12 should have at least 2 issues, got " + issues.size());
    }

    @Test
    @DisplayName("score should be lower when both LOC and CC are high vs only one")
    void shouldProduceLowerScore_WhenBothLocAndCCAreHigh() {
        EvaluationResult highBoth  = engine.evaluate(new MethodMetrics("both", 80, 12));
        EvaluationResult onlyHiCC  = engine.evaluate(new MethodMetrics("ccOnly", 10, 12));

        assertTrue(highBoth.getScore() <= onlyHiCC.getScore(),
                "Both LOC+CC high should score <= CC only high");
    }
}