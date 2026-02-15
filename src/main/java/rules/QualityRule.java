package rules;

import metrics.MethodMetrics;

public interface QualityRule {
    QualityIssue evaluate(MethodMetrics metrics);
}
