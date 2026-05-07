package rules;

import static org.junit.jupiter.api.Assertions.*;
import metrics.MethodMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@DisplayName("HighComplexityRule")
class HighComplexityRuleTest {

    private HighComplexityRule rule;

    @BeforeEach
    void setUp() {
        rule = new HighComplexityRule();
    }

    // ── Tests existentes (mejorados) ──────────────────────────────────────────

    @Test
    @DisplayName("should detect High Complexity when CC = 15")
    void shouldDetectHighComplexity() {
        List<QualityIssue> issues = rule.check(new MethodMetrics("test", 10, 15));

        assertFalse(issues.isEmpty());
        assertEquals("High Complexity", issues.get(0).getType());
    }

    @Test
    @DisplayName("should allow a method with low complexity (CC = 2)")
    void shouldAllowLowComplexity() {
        List<QualityIssue> issues = rule.check(new MethodMetrics("test", 10, 2));

        assertTrue(issues.isEmpty());
    }

    // ── Tests nuevos ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("should trigger High Complexity at the exact threshold (CC = 8)")
    void shouldTriggerAtExactThreshold_WhenCCIsEight() {
        List<QualityIssue> issues = rule.check(new MethodMetrics("borderHigh", 10, 8));

        assertFalse(issues.isEmpty(), "CC = 8 must trigger High Complexity");
        assertEquals("High Complexity", issues.get(0).getType());
    }

    @Test
    @DisplayName("should NOT trigger High Complexity just below threshold (CC = 7)")
    void shouldNotTriggerHighComplexity_WhenCCIsSevenOrLess() {
        List<QualityIssue> issues = rule.check(new MethodMetrics("belowHigh", 10, 7));

        boolean hasHigh = issues.stream()
                .anyMatch(i -> "High Complexity".equals(i.getType()));
        assertFalse(hasHigh, "CC = 7 must NOT trigger High Complexity");
    }

    @Test
    @DisplayName("should trigger Moderate Complexity when CC = 5")
    void shouldTriggerModerateComplexity_WhenCCIsFive() {
        List<QualityIssue> issues = rule.check(new MethodMetrics("moderate", 10, 5));

        assertFalse(issues.isEmpty(), "CC = 5 must produce at least one issue");
        boolean hasModerate = issues.stream()
                .anyMatch(i -> "Moderate Complexity".equals(i.getType()));
        assertTrue(hasModerate, "CC = 5 should be Moderate Complexity");
    }

    @Test
    @DisplayName("should return empty list when CC is exactly 4 (below moderate threshold)")
    void shouldReturnEmpty_WhenCCIsBelowModerateThreshold() {
        List<QualityIssue> issues = rule.check(new MethodMetrics("clean", 10, 4));

        assertTrue(issues.isEmpty(), "CC = 4 should produce no issues");
    }

    @Test
    @DisplayName("issue should contain a non-empty description for High Complexity")
    void shouldProvideNonEmptyDescription_ForHighComplexityIssue() {
        List<QualityIssue> issues = rule.check(new MethodMetrics("test", 10, 10));

        assertFalse(issues.isEmpty());
        String description = issues.get(0).getDescription();
        assertNotNull(description,      "Description must not be null");
        assertFalse(description.isBlank(), "Description must not be blank");
    }

    @Test
    @DisplayName("penalty should be positive for High Complexity issue")
    void shouldHavePositivePenalty_ForHighComplexityIssue() {
        List<QualityIssue> issues = rule.check(new MethodMetrics("test", 10, 10));

        assertFalse(issues.isEmpty());
        assertTrue(issues.get(0).getPenalty() > 0, "Penalty must be positive");
    }

    @Test
    @DisplayName("High Complexity penalty should be greater than Moderate Complexity penalty")
    void highComplexityPenaltyShouldExceedModeratePenalty() {
        int highPenalty = rule.check(new MethodMetrics("h", 10, 9)).stream()
                .mapToInt(QualityIssue::getPenalty).sum();
        int moderatePenalty = rule.check(new MethodMetrics("m", 10, 6)).stream()
                .mapToInt(QualityIssue::getPenalty).sum();

        assertTrue(highPenalty > moderatePenalty,
                "High Complexity (" + highPenalty + ") must penalize more than Moderate (" + moderatePenalty + ")");
    }
}