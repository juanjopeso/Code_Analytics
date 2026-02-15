package rules;

import metrics.MethodMetrics;
import java.util.ArrayList;
import java.util.List;

public class RuleEngine {

    private final List<MethodRule> rules;

    public RuleEngine() {
        rules = new ArrayList<>();
        rules.add(new LongMethodRule());
        rules.add(new HighComplexityRule());
        rules.add(new ModerateComplexityRule());
        rules.add(new RiskyMethodRule());
    }

    public EvaluationResult evaluate(MethodMetrics metrics) {

        List<QualityIssue> issues = new ArrayList<>();

        // Ejecutar todas las reglas
        for (MethodRule rule : rules) {
            issues.addAll(rule.check(metrics));
        }

        // Calcular score acumulativo
        int score = 100;

        for (QualityIssue issue : issues) {
            score -= issue.getPenalty();
        }

        if (score < 0) {
            score = 0;
        }

        return new EvaluationResult(
                metrics.getMethodName(),
                score,
                issues
        );
    }
}
