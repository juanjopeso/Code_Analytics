package metrics;

public class ClassMetrics {

    private final String className;
    private final int totalLoc;
    private final int methodCount;
    private final int totalCC;

    public ClassMetrics(String className, int totalLoc, int methodCount, int totalCC) {
        this.className = className;
        this.totalLoc = totalLoc;
        this.methodCount = methodCount;
        this.totalCC = totalCC;
    }

    public String getClassName() {
        return className;
    }

    public int getTotalLoc() {
        return totalLoc;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public int getTotalCC() {
        return totalCC;
    }
}
