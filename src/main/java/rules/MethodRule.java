package rules;

import metrics.MethodMetrics;
import java.util.List;

public interface MethodRule {
    List<QualityIssue> check(MethodMetrics metrics);
}
