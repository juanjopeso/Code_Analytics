package rules;

public class QualityIssue {

    private final String type;
    private final String description;
    private final int penalty;
    private String suggestion;

    // Constructor básico
    public QualityIssue(String type, String description, int penalty) {
        this.type = type;
        this.description = description;
        this.penalty = penalty;
    }

    // Constructor con sugerencia
    public QualityIssue(String type, String description, int penalty, String suggestion) {
        this.type = type;
        this.description = description;
        this.penalty = penalty;
        this.suggestion = suggestion;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public int getPenalty() {
        return penalty;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" • ").append(type)
          .append("\n   ").append(description)
          .append("\n   Penalización: -").append(penalty);

        if (suggestion != null && !suggestion.isEmpty()) {
            sb.append("\n   Sugerencia: ").append(suggestion);
        }

        return sb.toString();
    }
}
