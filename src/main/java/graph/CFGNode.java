package graph;

public class CFGNode {

    private final String label;

    public CFGNode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
