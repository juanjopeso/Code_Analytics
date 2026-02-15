package rules;

import metrics.MethodMetrics;
import java.util.ArrayList;
import java.util.List;

public class HighComplexityRule implements MethodRule {

    @Override
    public List<QualityIssue> check(MethodMetrics metrics) {

        List<QualityIssue> issues = new ArrayList<>();

        int cc = metrics.getCyclomaticComplexity();

        if (cc >= 8) {
            issues.add(new QualityIssue(
                    "High Complexity",
                    "Complejidad ciclomática muy alta (" + cc + ")",
                    40,
                    "Reducir condicionales anidados o aplicar Strategy Pattern."
            ));
        }
        else if (cc >= 5) {
            issues.add(new QualityIssue(
                    "Moderate Complexity",
                    "Complejidad ciclomática moderada (" + cc + ")",
                    25,
                    "Simplificar estructuras if-else."
            ));
        }

        return issues;
    }
}
