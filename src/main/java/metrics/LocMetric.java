package metrics;

import com.github.javaparser.ast.body.MethodDeclaration;

public class LocMetric {

    public int calculate(MethodDeclaration method) {
        if (method.getRange().isPresent()) {
            int begin = method.getRange().get().begin.line;
            int end = method.getRange().get().end.line;
            return end - begin + 1;
        }
        return 0;
    }
}