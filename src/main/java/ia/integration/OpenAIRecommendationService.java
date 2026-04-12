package ia.integration;

import ia.AIRecommendationService;
import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to the OpenAI Chat Completions API to produce refactoring recommendations.
 *
 * <h3>Design decisions</h3>
 * <ul>
 *   <li>The {@link HttpClient} is shared and re-used across calls (thread-safe, efficient).</li>
 *   <li>A configurable timeout avoids indefinite blocking.</li>
 *   <li>HTTP error statuses (4xx, 5xx) are surfaced as {@link AIServiceException}
 *       so callers can react rather than receiving a silent empty string.</li>
 *   <li>{@link InterruptedException} re-interrupts the thread rather than swallowing it.</li>
 * </ul>
 */
public class OpenAIRecommendationService implements AIRecommendationService {

    private static final Logger LOGGER =
            Logger.getLogger(OpenAIRecommendationService.class.getName());

    private static final String API_URL =
            "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";
    private static final int MAX_TOKENS = 180;
    private static final double TEMPERATURE = 0.3;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final String apiKey;
    // HttpClient is expensive to create — reuse it for the lifetime of this object.
    private final HttpClient httpClient;

    public OpenAIRecommendationService(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("OpenAI API key must not be blank.");
        }
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
    }

    @Override
    public String generateRecommendation(String context) {
        try {
            String requestBody = buildRequestBody(context);
            HttpResponse<String> response = sendRequest(requestBody);
            validateStatus(response);
            return parseRecommendation(response.body());

        } catch (AIServiceException e) {
            LOGGER.log(Level.SEVERE, "AI service error: {0}", e.getMessage());
            return "No se pudo generar la recomendación: " + e.getMessage();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Network I/O error calling OpenAI", e);
            return "Error de red al consultar la IA. Verifique su conexión.";

        } catch (InterruptedException e) {
            // Restore the interrupted flag so higher-level code can react properly.
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Request interrupted", e);
            return "La solicitud fue interrumpida.";
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildRequestBody(String context) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", context);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject body = new JsonObject();
        body.addProperty("model", MODEL);
        body.add("messages", messages);
        body.addProperty("temperature", TEMPERATURE);
        body.addProperty("max_tokens", MAX_TOKENS);

        return body.toString();
    }

    private HttpResponse<String> sendRequest(String body)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void validateStatus(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new AIServiceException(
                    "HTTP " + status + " from OpenAI API. Body: " + response.body());
        }
    }

    private String parseRecommendation(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray choices = json.getAsJsonArray("choices");

        if (choices == null || choices.isEmpty()) {
            return "La IA no retornó ninguna sugerencia.";
        }

        return choices.get(0)
                      .getAsJsonObject()
                      .getAsJsonObject("message")
                      .get("content")
                      .getAsString()
                      .trim();
    }

    // -------------------------------------------------------------------------
    // Checked exception (keeps the public API clean)
    // -------------------------------------------------------------------------

    /** Signals a non-2xx response from the OpenAI API. */
    public static final class AIServiceException extends RuntimeException {
        public AIServiceException(String message) {
            super(message);
        }
    }
}
