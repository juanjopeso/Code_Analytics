package graph;

public class CFGEdge {
    public final CFGNode from;
    public final CFGNode to;

    public CFGEdge(CFGNode from, CFGNode to) {
        this.from = from;
        this.to = to;
    }
}
