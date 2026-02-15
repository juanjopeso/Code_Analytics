package rules;

import metrics.ClassMetrics;

public class GodClassRule implements ClassRule {

    @Override
    public QualityIssue evaluate(ClassMetrics metrics) {

        int flags = 0;

        if (metrics.getTotalLoc() > 200) flags++;
        if (metrics.getMethodCount() > 10) flags++;
        if (metrics.getTotalCC() > 20) flags++;

        if (flags >= 2) {
            return new QualityIssue(
                "GOD_CLASS",
                "Clase con demasiadas responsabilidades",
                40,
                "Dividir la clase en componentes más pequeños (SRP)"
            );
        }

        return null;
    }
}
