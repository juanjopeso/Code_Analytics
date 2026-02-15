package metrics;

public class MethodMetrics {

    private final String methodName;
    private final int loc;
    private final int cyclomaticComplexity;

    public MethodMetrics(String methodName, int loc, int cyclomaticComplexity) {
        this.methodName = methodName;
        this.loc = loc;
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLoc() {
        return loc;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }
}
