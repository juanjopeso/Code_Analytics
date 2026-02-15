package report;

public class ReportJSON {

    public static String create(String className, int score, String level) {
        return """
        {
          "class": "%s",
          "score": %d,
          "level": "%s"
        }
        """.formatted(className, score, level);
    }
}
