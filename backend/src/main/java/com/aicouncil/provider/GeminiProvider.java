package com.aicouncil.provider;

import com.aicouncil.dto.AIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiProvider.class);

    private final WebClient webClient;

    @Value("${ai.mode:MOCK}")
    private String aiMode;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String baseUrl;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String modelName;

    public GeminiProvider(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getProviderId() {
        return "gemini";
    }

    @Override
    public String getProviderName() {
        return "Google Gemini (" + modelName + ")";
    }

    @Override
    public AIResponse generateResponse(String prompt) {
        long startTime = System.currentTimeMillis();

        if ("LIVE".equalsIgnoreCase(aiMode)) {
            if (apiKey == null || apiKey.isBlank()) {
                log.error("Gemini LIVE mode active but GEMINI_API_KEY environment variable is missing.");
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("Gemini request failed: GEMINI_API_KEY environment variable is missing.")
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .inputTokens(0)
                        .outputTokens(0)
                        .tokenUsage(0)
                        .status("FAILED")
                        .success(false)
                        .errorMessage("Missing GEMINI_API_KEY environment variable")
                        .build();
            }

            try {
                String endpointUrl = String.format("%s/%s:generateContent?key=%s", baseUrl, modelName, apiKey.trim());
                Map<String, Object> requestBody = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(
                                        Map.of("text", com.aicouncil.util.PromptTemplates.buildSystemPrompt(prompt))
                                ))
                        )
                );

                Map response = webClient.post()
                        .uri(endpointUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(30))
                        .block();

                long duration = System.currentTimeMillis() - startTime;

                if (response != null && response.containsKey("candidates")) {
                    List candidates = (List) response.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map candidate = (Map) candidates.get(0);
                        Map content = (Map) candidate.get("content");
                        List parts = (List) content.get("parts");
                        if (!parts.isEmpty()) {
                            Map part = (Map) parts.get(0);
                            String text = (String) part.get("text");

                            Integer inputTokens = null;
                            Integer outputTokens = null;
                            Integer totalTokens = null;

                            if (response.containsKey("usageMetadata") && response.get("usageMetadata") instanceof Map) {
                                Map usage = (Map) response.get("usageMetadata");
                                if (usage.containsKey("promptTokenCount")) inputTokens = ((Number) usage.get("promptTokenCount")).intValue();
                                if (usage.containsKey("candidatesTokenCount")) outputTokens = ((Number) usage.get("candidatesTokenCount")).intValue();
                                if (usage.containsKey("totalTokenCount")) totalTokens = ((Number) usage.get("totalTokenCount")).intValue();
                            }

                            log.info("Successfully received Gemini response in {}ms (tokens: {})", duration, totalTokens);

                            return AIResponse.builder()
                                    .provider(getProviderId())
                                    .modelName(modelName)
                                    .content(text)
                                    .responseTimeMs(duration)
                                    .inputTokens(inputTokens)
                                    .outputTokens(outputTokens)
                                    .tokenUsage(totalTokens)
                                    .status("SUCCESS")
                                    .success(true)
                                    .build();
                        }
                    }
                }
            } catch (WebClientResponseException e) {
                log.error("Gemini API HTTP error [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("Gemini API HTTP Error: " + e.getStatusCode())
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .status("FAILED")
                        .success(false)
                        .errorMessage("HTTP " + e.getStatusCode() + ": " + e.getMessage())
                        .build();
            } catch (Exception e) {
                log.error("Gemini LIVE API request error: {}", e.getMessage());
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("Gemini request error: " + e.getMessage())
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .status("FAILED")
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        }

        return generateSimulatedInitialResponse(prompt, startTime);
    }

    @Override
    public AIResponse generateCritique(String question, String ownResponse, String targetProviderId, String targetResponse) {
        long startTime = System.currentTimeMillis();

        if ("LIVE".equalsIgnoreCase(aiMode)) {
            if (apiKey == null || apiKey.isBlank()) {
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("Gemini critique failed: Missing GEMINI_API_KEY")
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .status("FAILED")
                        .success(false)
                        .errorMessage("Missing GEMINI_API_KEY")
                        .build();
            }

            try {
                String critiquePrompt = String.format("Analyze and critique %s's response to: %s\n\nTarget Response:\n%s", targetProviderId, question, targetResponse);
                String endpointUrl = String.format("%s/%s:generateContent?key=%s", baseUrl, modelName, apiKey.trim());
                Map<String, Object> requestBody = Map.of(
                        "contents", List.of(
                                Map.of("parts", List.of(Map.of("text", critiquePrompt)))
                        )
                );

                Map response = webClient.post()
                        .uri(endpointUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(30))
                        .block();

                long duration = System.currentTimeMillis() - startTime;
                if (response != null && response.containsKey("candidates")) {
                    List candidates = (List) response.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map candidate = (Map) candidates.get(0);
                        Map content = (Map) candidate.get("content");
                        List parts = (List) content.get("parts");
                        if (!parts.isEmpty()) {
                            Map part = (Map) parts.get(0);
                            String text = (String) part.get("text");

                            Integer inputTokens = null;
                            Integer outputTokens = null;
                            Integer totalTokens = null;

                            if (response.containsKey("usageMetadata") && response.get("usageMetadata") instanceof Map) {
                                Map usage = (Map) response.get("usageMetadata");
                                if (usage.containsKey("promptTokenCount")) inputTokens = ((Number) usage.get("promptTokenCount")).intValue();
                                if (usage.containsKey("candidatesTokenCount")) outputTokens = ((Number) usage.get("candidatesTokenCount")).intValue();
                                if (usage.containsKey("totalTokenCount")) totalTokens = ((Number) usage.get("totalTokenCount")).intValue();
                            }

                            return AIResponse.builder()
                                    .provider(getProviderId())
                                    .modelName(modelName)
                                    .content(text)
                                    .responseTimeMs(duration)
                                    .inputTokens(inputTokens)
                                    .outputTokens(outputTokens)
                                    .tokenUsage(totalTokens)
                                    .status("SUCCESS")
                                    .success(true)
                                    .build();
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Gemini LIVE API critique call failed: {}", e.getMessage());
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("Gemini critique failed: " + e.getMessage())
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .status("FAILED")
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build();
            }
        }

        return generateSimulatedCritique(targetProviderId, question, startTime);
    }

    private AIResponse generateSimulatedInitialResponse(String prompt, long startTime) {
        try {
            Thread.sleep(500 + (long)(Math.random() * 300));
        } catch (InterruptedException ignored) {}

        String content = "### Multi-Perspective Synthesis (Google Gemini 1.5)\n\n" +
                "Analyzing **" + prompt + "** requires evaluating both pragmatic application requirements and long-term ecosystem trajectories.\n\n" +
                "#### Comparative Evaluation Matrix:\n" +
                "1. **Scalability & Multithreading**: Excellent throughput when architected with async non-blocking principles or reactive streams.\n" +
                "2. **Ecosystem Depth & Integration**: Rich availability of pre-built SDKs, ORMs, and enterprise connectors.\n" +
                "3. **Operational Overhead**: Requires careful profiling of heap memory, garbage collection parameters, and startup cold times.\n\n" +
                "#### Gemini Decision Matrix:\n" +
                "- Choose **Enterprise Strong-Typing** for large team codebases and strict domain invariants.\n" +
                "- Choose **Event-Driven Async Engine** for real-time web-sockets, micro-services, and rapid API layers.\n\n" +
                "**Verdict**: Both approaches excel in specific domains. A hybrid architecture or context-specific choice yields the best return on investment.";

        long duration = System.currentTimeMillis() - startTime;
        return AIResponse.builder()
                .provider(getProviderId())
                .modelName(modelName + " (Simulated)")
                .content(content)
                .responseTimeMs(duration)
                .inputTokens(125)
                .outputTokens(270)
                .tokenUsage(395)
                .status("SUCCESS")
                .success(true)
                .build();
    }

    private AIResponse generateSimulatedCritique(String targetProviderId, String question, long startTime) {
        try {
            Thread.sleep(380);
        } catch (InterruptedException ignored) {}

        String critiqueContent = String.format(
                "**Google Gemini Critique on %s:**\n" +
                        "- **Perspective**: %s's response offers valid points regarding specific domain trade-offs.\n" +
                        "- **Key Disagreement**: %s presents a slightly binary view. Modern architectures frequently combine both approaches depending on bounded context microservice needs.\n" +
                        "- **Recommendation**: The Judge synthesizer should highlight contextual suitability rather than absolute winner selection.",
                targetProviderId.toUpperCase(), targetProviderId, targetProviderId
        );

        return AIResponse.builder()
                .provider(getProviderId())
                .modelName(modelName + " (Simulated)")
                .content(critiqueContent)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .inputTokens(85)
                .outputTokens(115)
                .tokenUsage(200)
                .status("SUCCESS")
                .success(true)
                .build();
    }
}
