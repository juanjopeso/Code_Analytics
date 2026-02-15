package metrics;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;

public class CyclomaticComplexityMetric {

    public int calculate(MethodDeclaration method) {
        int complexity = 1; // base

        complexity += method.findAll(IfStmt.class).size();
        complexity += method.findAll(ForStmt.class).size();
        complexity += method.findAll(WhileStmt.class).size();
        complexity += method.findAll(DoStmt.class).size();
        complexity += method.findAll(SwitchEntry.class).size();
        complexity += method.findAll(CatchClause.class).size();

        return complexity;
    }
}
