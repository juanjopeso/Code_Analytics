package report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import rules.EvaluationResult;
import rules.QualityIssue;
import metrics.MethodMetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class JsonReportExporter {

    private final JsonArray methodsArray = new JsonArray();

    public void addMethodReport(
            String className,
            MethodMetrics metrics,
            EvaluationResult result) {

        JsonObject methodJson = new JsonObject();

        methodJson.addProperty("class", className);
        methodJson.addProperty("method", metrics.getMethodName());
        methodJson.addProperty("loc", metrics.getLoc());
        methodJson.addProperty("cyclomaticComplexity",
                metrics.getCyclomaticComplexity());
        methodJson.addProperty("score", result.getScore());
        methodJson.addProperty("level", result.getLevel());

        JsonArray issuesArray = new JsonArray();

        for (QualityIssue issue : result.getIssues()) {

            JsonObject issueJson = new JsonObject();
            issueJson.addProperty("type", issue.getType());
            issueJson.addProperty("description",
                    issue.getDescription());
            issueJson.addProperty("suggestion",
                    issue.getSuggestion());

            issuesArray.add(issueJson);
        }

        methodJson.add("issues", issuesArray);

        methodsArray.add(methodJson);
    }

    public void export(String fileName) {

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(methodsArray, writer);
        } catch (IOException e) {
            System.out.println("Error exportando JSON: " + e.getMessage());
        }
    }
}
