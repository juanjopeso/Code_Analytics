package rules;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import metrics.MethodMetrics;
import org.junit.jupiter.api.Test;


public class HighComplexityRuleTest {

    @Test
    void shouldDetectHighComplexity() {
        HighComplexityRule rule = new HighComplexityRule();
        MethodMetrics metrics = new MethodMetrics("test", 10, 15);

        List<QualityIssue> issues = rule.check(metrics);

        assertFalse(issues.isEmpty());
        assertEquals("High Complexity", issues.get(0).getType());
    }

    @Test
    void shouldAllowLowComplexity() {
        HighComplexityRule rule = new HighComplexityRule();
        MethodMetrics metrics = new MethodMetrics("test", 10, 2);

        List<QualityIssue> issues = rule.check(metrics); 

        assertTrue(issues.isEmpty()); 
    }
}