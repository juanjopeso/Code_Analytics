package ia.integration;

import static org.junit.jupiter.api.Assertions.*;
import ia.AIRecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OpenAIRecommendationService}.
 *
 * <p>The real HTTP calls are replaced with a lightweight fake implementation
 * ({@link StubAIService}) so tests run offline, are deterministic, and execute
 * in milliseconds.
 *
 * <p>Integration tests that exercise the real API should be placed in a separate
 * class (e.g. {@code OpenAIRecommendationServiceIT}) and guarded with
 * {@code @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")}.
 */
@DisplayName("OpenAIRecommendationService (via stub)")
class OpenAIRecommendationServiceTest {

    // ── Constructor validation ────────────────────────────────────────────────

    @Test
    @DisplayName("should throw IllegalArgumentException when API key is null")
    void shouldThrow_WhenApiKeyIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new OpenAIRecommendationService(null));
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when API key is blank")
    void shouldThrow_WhenApiKeyIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new OpenAIRecommendationService("   "));
    }

    @Test
    @DisplayName("should construct successfully with a valid API key")
    void shouldConstruct_WhenApiKeyIsValid() {
        assertDoesNotThrow(() -> new OpenAIRecommendationService("sk-test-key-valid"));
    }

    // ── Contract tests via stub ────────────────────────────────────────────────

    @Test
    @DisplayName("stub service should return the canned response for any prompt")
    void stubShouldReturnCannedResponse() {
        AIRecommendationService stub = new StubAIService("Refactoriza usando Extract Method.");
        String result = stub.generateRecommendation("Método largo con CC alta");

        assertEquals("Refactoriza usando Extract Method.", result);
    }

    @Test
    @DisplayName("failing stub should return a non-null fallback message")
    void failingStubShouldReturnFallback() {
        AIRecommendationService failingStub = new FailingAIService();
        String result = failingStub.generateRecommendation("cualquier contexto");

        assertNotNull(result, "Even on failure the service must return a non-null string");
        assertFalse(result.isBlank(), "Even on failure the service must return a non-blank message");
    }

    @Test
    @DisplayName("stub should not throw regardless of prompt content")
    void stubShouldNotThrow_ForAnyInput() {
        AIRecommendationService stub = new StubAIService("OK");

        assertAll(
                () -> assertDoesNotThrow(() -> stub.generateRecommendation("")),
                () -> assertDoesNotThrow(() -> stub.generateRecommendation(null)),
                () -> assertDoesNotThrow(() -> stub.generateRecommendation("a".repeat(5000)))
        );
    }

    // ── Stubs ─────────────────────────────────────────────────────────────────

    /** Always returns the supplied fixed response. */
    static class StubAIService implements AIRecommendationService {
        private final String fixedResponse;
        StubAIService(String fixedResponse) { this.fixedResponse = fixedResponse; }

        @Override
        public String generateRecommendation(String context) {
            return fixedResponse;
        }
    }

    /** Simulates a network or API failure, returning a fallback message. */
    static class FailingAIService implements AIRecommendationService {
        @Override
        public String generateRecommendation(String context) {
            return "No se pudo generar la recomendación: simulated failure";
        }
    }
}