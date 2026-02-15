package rules;

import metrics.MethodMetrics;
import java.util.ArrayList;
import java.util.List;

public class LongMethodRule implements MethodRule {

    @Override
    public List<QualityIssue> check(MethodMetrics metrics) {

        List<QualityIssue> issues = new ArrayList<>();

        int loc = metrics.getLoc();

        if (loc > 50) {
            issues.add(new QualityIssue(
                    "Long Method",
                    "El método tiene más de 50 líneas (" + loc + ")",
                    35,
                    "Dividir el método en submétodos más pequeños (Extract Method)."
            ));
        }
        else if (loc > 30) {
            issues.add(new QualityIssue(
                    "Long Method",
                    "El método tiene más de 30 líneas (" + loc + ")",
                    20,
                    "Aplicar refactorización para mejorar legibilidad."
            ));
        }

        return issues;
    }
}
