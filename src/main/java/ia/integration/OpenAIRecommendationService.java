package ia.integration;

import ia.AIRecommendationService;
import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAIRecommendationService implements AIRecommendationService {

    private final String apiKey;
    private final HttpClient client = HttpClient.newHttpClient();

    public OpenAIRecommendationService(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String generateRecommendation(String context) {

        try {

            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", context);

            JsonArray messages = new JsonArray();
            messages.add(message);

            JsonObject body = new JsonObject();
            body.addProperty("model", "gpt-4o-mini");
            body.add("messages", messages);
            body.addProperty("temperature", 0.3);
            body.addProperty("max_tokens", 180);


            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            //System.out.println("RAW RESPONSE:");
            //System.out.println(response.body());


            JsonObject jsonResponse =
                    JsonParser.parseString(response.body()).getAsJsonObject();

            JsonArray choices = jsonResponse.getAsJsonArray("choices");

            if (choices != null && choices.size() > 0) {
                JsonObject messageObj =
                        choices.get(0).getAsJsonObject()
                                .getAsJsonObject("message");

                return messageObj.get("content").getAsString().trim();
            }

            return "No recommendation generated.";

        } catch (IOException | InterruptedException e) {
            return "Error generating AI recommendation: " + e.getMessage();
        }
    }
}
