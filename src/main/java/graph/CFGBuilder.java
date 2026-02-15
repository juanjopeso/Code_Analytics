package graph;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;

public class CFGBuilder {

    public ControlFlowGraph build(MethodDeclaration method) {
        ControlFlowGraph cfg = new ControlFlowGraph();

        CFGNode start = cfg.addNode("START");
        CFGNode end = cfg.addNode("END");

        if (!method.getBody().isPresent()) {
            cfg.addEdge(start, end);
            return cfg;
        }

        CFGNode last = start;

        for (Statement stmt : method.getBody().get().getStatements()) {

            if (stmt instanceof IfStmt) {
                CFGNode ifNode = cfg.addNode("IF");
                cfg.addEdge(last, ifNode);

                CFGNode thenNode = cfg.addNode("THEN");
                CFGNode elseNode = cfg.addNode("ELSE");

                cfg.addEdge(ifNode, thenNode);
                cfg.addEdge(ifNode, elseNode);

                last = cfg.addNode("JOIN");

            } else if (stmt instanceof ForStmt || stmt instanceof WhileStmt) {
                CFGNode loop = cfg.addNode("LOOP");
                cfg.addEdge(last, loop);
                cfg.addEdge(loop, loop);
                last = loop;

            } else {
                CFGNode stmtNode = cfg.addNode("STMT");
                cfg.addEdge(last, stmtNode);
                last = stmtNode;
            }
        }

        cfg.addEdge(last, end);
        return cfg;
    }
}
