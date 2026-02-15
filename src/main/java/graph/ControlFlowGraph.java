package graph;

import java.util.*;

public class ControlFlowGraph {

    private final Set<CFGNode> nodes = new HashSet<>();
    private final List<CFGEdge> edges = new ArrayList<>();

    public CFGNode addNode(String label) {
        CFGNode node = new CFGNode(label);
        nodes.add(node);
        return node;
    }

    public void addEdge(CFGNode from, CFGNode to) {
        edges.add(new CFGEdge(from, to));
    }

    public List<CFGEdge> getEdges() {
        return edges;
    }

    public void print() {
        System.out.println("    CFG:");
        for (CFGEdge e : edges) {
            System.out.println("      " + e.from + " -> " + e.to);
        }
    }
    public void printIndented(String indent) {
    for (CFGEdge e : edges) {
        System.out.println(indent + e.from + " -> " + e.to);
    }
}

}
