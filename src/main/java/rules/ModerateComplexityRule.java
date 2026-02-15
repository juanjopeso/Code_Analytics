package rules;

import metrics.MethodMetrics;
import java.util.ArrayList;
import java.util.List;

public class ModerateComplexityRule implements MethodRule {

    @Override
    public List<QualityIssue> check(MethodMetrics metrics) {

        List<QualityIssue> issues = new ArrayList<>();

        int cc = metrics.getCyclomaticComplexity();

        if (cc >= 5 && cc < 8) {
            issues.add(new QualityIssue(
                    "Moderate Complexity",
                    "Complejidad ciclomática moderada (" + cc + ")",
                    25,
                    "Considere simplificar condicionales."
            ));
        }

        return issues;
    }
}
