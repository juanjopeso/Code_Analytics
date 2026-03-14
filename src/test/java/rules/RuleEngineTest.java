package rules;

import static org.junit.jupiter.api.Assertions.*;
import metrics.MethodMetrics;
import org.junit.jupiter.api.Test;

public class RuleEngineTest {

    @Test
    void shouldEvaluateMethodAndReturnIssues() {
        MethodMetrics metrics = new MethodMetrics("test", 100, 20);

        RuleEngine engine = new RuleEngine();
        EvaluationResult result = engine.evaluate(metrics);

        assertFalse(result.getIssues().isEmpty());
        assertTrue(result.getScore() < 100);
    }

    @Test
    void shouldReturnGoodScoreForCleanMethod() {
        MethodMetrics metrics = new MethodMetrics("test", 5, 1);

        RuleEngine engine = new RuleEngine();
        EvaluationResult result = engine.evaluate(metrics);

        assertTrue(result.getIssues().isEmpty());
        assertTrue(result.getScore() >= 80);
    }
}