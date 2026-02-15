package rules;

import java.util.List;

public class EvaluationResult {

    private final String methodName;
    private final int score;
    private  String level;
    private final List<QualityIssue> issues;

    public EvaluationResult(String methodName, int score, List<QualityIssue> issues) {
        this.methodName = methodName;
        this.score = score;
        this.issues = issues;
        this.level = calculateLevel(score);
    }

    private String calculateLevel(int score) {
        if (score >= 85) level = "EXCELENTE";
        else if (score >= 70) level = "BUENO";
        else if (score >= 50) level = "REGULAR";
        else if (score >= 30) level = "MALO";
        else level = "CRITICO";
        return null;
    }

    public int getScore() { return score; }
    public String getLevel() { return level; }
    public List<QualityIssue> getIssues() { return issues; }
}
