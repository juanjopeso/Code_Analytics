package ia;

import rules.QualityIssue;
import metrics.MethodMetrics;

public class RecommendationBuilder {

    public static String buildPrompt(QualityIssue issue, MethodMetrics metrics) {

        String methodName = metrics.getMethodName();
        int loc = metrics.getLoc();
        int cc = metrics.getCyclomaticComplexity();

        switch (issue.getType()) {

            case "Long Method":
                return "El método " + methodName +
                        " tiene " + loc + " líneas. " +
                        "Propón cómo dividirlo en submétodos con nombres concretos.";

            case "High Complexity":
                return "El método " + methodName +
                        " tiene complejidad ciclomática de " + cc +
                        ". Sugiere cómo reducir condicionales usando principios SOLID.";

            case "Moderate Complexity":
                return "El método " + methodName +
                        " presenta complejidad moderada (" + cc + "). " +
                        "¿Cómo mejorarías su estructura interna?";

            case "Risky Method":
                return "El método " + methodName +
                        " es largo y complejo. " +
                        "Propón una refactorización estructural concreta.";

            default:
                return "Analiza el método " + methodName +
                        " y propone mejoras técnicas específicas.";
        }
    }
}
