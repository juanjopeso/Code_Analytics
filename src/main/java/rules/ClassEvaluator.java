package rules;

import metrics.ClassMetrics;
import java.util.*;

public class ClassEvaluator {

    private final List<ClassRule> rules = new ArrayList<>();

    public ClassEvaluator() {
        rules.add(new GodClassRule());
    }

    public EvaluationResult evaluate(ClassMetrics classMetrics) {

        int score = 100;
        List<QualityIssue> issues = new ArrayList<>();

        for (ClassRule rule : rules) {
            QualityIssue issue = rule.evaluate(classMetrics);
            if (issue != null) {
                issues.add(issue);
                score -= issue.getPenalty();
            }
        }

        if (score < 0) score = 0;

        return new EvaluationResult(
            classMetrics.getClassName(),
            score,
            issues
        );
    }
}
