package rules;

import metrics.MethodMetrics;
import java.util.ArrayList;
import java.util.List;

public class RiskyMethodRule implements MethodRule {

    @Override
    public List<QualityIssue> check(MethodMetrics metrics) {

        List<QualityIssue> issues = new ArrayList<>();

        int loc = metrics.getLoc();
        int cc = metrics.getCyclomaticComplexity();

        if (loc > 30 && cc >= 5) {
            issues.add(new QualityIssue(
                    "Risky Method",
                    "Método largo y complejo",
                    15,
                    "Se recomienda refactorización estructural."
            ));
        }

        return issues;
    }
}
