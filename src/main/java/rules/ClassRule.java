package rules;

import metrics.ClassMetrics;

public interface ClassRule {
    QualityIssue evaluate(ClassMetrics metrics);
}
