package graph;

import java.util.*;

public class CFGGraph {

    private final Set<String> nodes = new HashSet<>();
    private final List<String[]> edges = new ArrayList<>();

    public void addNode(String node) {
        nodes.add(node);
    }

    public void addEdge(String from, String to) {
        nodes.add(from);
        nodes.add(to);
        edges.add(new String[]{from, to});
    }

    public Set<String> getNodes() {
        return nodes;
    }

    public List<String[]> getEdges() {
        return edges;
    }
}
