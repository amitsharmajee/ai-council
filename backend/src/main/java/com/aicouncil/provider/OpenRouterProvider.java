package com.aicouncil.provider;

import com.aicouncil.dto.AIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OpenRouterProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterProvider.class);

    private final WebClient webClient;

    @Value("${ai.mode:MOCK}")
    private String aiMode;

    @Value("${openrouter.api.key:}")
    private String apiKey;

    @Value("${openrouter.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String apiUrl;

    @Value("${openrouter.model:openrouter/free}")
    private String modelName;

    public OpenRouterProvider(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getProviderId() {
        return "openrouter";
    }

    @Override
    public String getProviderName() {
        return "OpenRouter (" + modelName + ")";
    }

    @Override
    public AIResponse generateResponse(String prompt) {
        long startTime = System.currentTimeMillis();

        if ("LIVE".equalsIgnoreCase(aiMode)) {
            if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("your_")) {
                log.info("OpenRouter API key missing or unconfigured in LIVE mode, falling back to simulated mock response.");
                return generateSimulatedInitialResponse(prompt, startTime);
            }

            try {
                Map<String, Object> requestBody = Map.of(
                        "model", modelName,
                        "messages", List.of(
                                Map.of("role", "system", "content", com.aicouncil.util.PromptTemplates.AI_SYSTEM_POLICY + "\nYou are OpenRouter AI, a multi-model intelligence engine. Provide authoritative, concise, production-grade answers."),
                                Map.of("role", "user", "content", prompt)
                        ),
                        "temperature", 0.7
                );

                Map response = webClient.post()
                        .uri(apiUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                        .header("HTTP-Referer", "https://github.com/aicouncil")
                        .header("X-Title", "AI Council")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(45))
                        .block();

                long duration = System.currentTimeMillis() - startTime;

                if (response != null && response.containsKey("choices")) {
                    List choices = (List) response.get("choices");
                    if (!choices.isEmpty()) {
                        Map choice = (Map) choices.get(0);
                        Map message = (Map) choice.get("message");
                        String content = (String) message.get("content");

                        Integer inputTokens = null;
                        Integer outputTokens = null;
                        Integer totalTokens = null;

                        if (response.containsKey("usage") && response.get("usage") instanceof Map) {
                            Map usage = (Map) response.get("usage");
                            if (usage.containsKey("prompt_tokens")) inputTokens = ((Number) usage.get("prompt_tokens")).intValue();
                            if (usage.containsKey("completion_tokens")) outputTokens = ((Number) usage.get("completion_tokens")).intValue();
                            if (usage.containsKey("total_tokens")) totalTokens = ((Number) usage.get("total_tokens")).intValue();
                        }

                        log.info("Successfully received OpenRouter response in {}ms (tokens: {})", duration, totalTokens);

                        return AIResponse.builder()
                                .provider(getProviderId())
                                .modelName(modelName)
                                .content(content)
                                .responseTimeMs(duration)
                                .inputTokens(inputTokens)
                                .outputTokens(outputTokens)
                                .tokenUsage(totalTokens)
                                .status("SUCCESS")
                                .success(true)
                                .build();
                    }
                }
            } catch (WebClientResponseException e) {
                log.error("OpenRouter API HTTP error [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("OpenRouter API HTTP Error: " + e.getStatusCode())
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .status("FAILED")
                        .success(false)
                        .errorMessage("HTTP " + e.getStatusCode() + ": " + e.getMessage())
                        .build();
            } catch (Exception e) {
                log.error("OpenRouter LIVE API request error: {}", e.getMessage());
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("OpenRouter request error: " + e.getMessage())
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
            if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("your_")) {
                return generateSimulatedCritique(targetProviderId, question, startTime);
            }

            try {
                String critiquePrompt = String.format(
                        "Critique the following response from %s regarding '%s'.\n\nTarget Response:\n%s\n\nProvide constructive analysis, strengths, and weaknesses.",
                        targetProviderId.toUpperCase(), question, targetResponse
                );

                Map<String, Object> requestBody = Map.of(
                        "model", modelName,
                        "messages", List.of(
                                Map.of("role", "system", "content", "You are an AI debater evaluating competing software architectures. Be objective, precise, and rigorous."),
                                Map.of("role", "user", "content", critiquePrompt)
                        )
                );

                Map response = webClient.post()
                        .uri(apiUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                        .header("HTTP-Referer", "https://github.com/aicouncil")
                        .header("X-Title", "AI Council")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(45))
                        .block();

                long duration = System.currentTimeMillis() - startTime;
                if (response != null && response.containsKey("choices")) {
                    List choices = (List) response.get("choices");
                    if (!choices.isEmpty()) {
                        Map choice = (Map) choices.get(0);
                        Map message = (Map) choice.get("message");
                        String content = (String) message.get("content");

                        Integer inputTokens = null;
                        Integer outputTokens = null;
                        Integer totalTokens = null;

                        if (response.containsKey("usage") && response.get("usage") instanceof Map) {
                            Map usage = (Map) response.get("usage");
                            if (usage.containsKey("prompt_tokens")) inputTokens = ((Number) usage.get("prompt_tokens")).intValue();
                            if (usage.containsKey("completion_tokens")) outputTokens = ((Number) usage.get("completion_tokens")).intValue();
                            if (usage.containsKey("total_tokens")) totalTokens = ((Number) usage.get("total_tokens")).intValue();
                        }

                        return AIResponse.builder()
                                .provider(getProviderId())
                                .modelName(modelName)
                                .content(content)
                                .responseTimeMs(duration)
                                .inputTokens(inputTokens)
                                .outputTokens(outputTokens)
                                .tokenUsage(totalTokens)
                                .status("SUCCESS")
                                .success(true)
                                .build();
                    }
                }
            } catch (Exception e) {
                log.warn("OpenRouter LIVE API critique call failed: {}", e.getMessage());
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("OpenRouter critique failed: " + e.getMessage())
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
            Thread.sleep(200 + (long)(Math.random() * 150));
        } catch (InterruptedException ignored) {}

        String content = "### Multi-Model Engine Synthesis (OpenRouter free)\n\n" +
                "Evaluating **" + prompt + "** via OpenRouter free model routing:\n\n" +
                "#### Key Architectural Trade-offs:\n" +
                "1. **Unified Access & Routing**: Seamless access to top open and commercial models with fallback support.\n" +
                "2. **Standardized Schema**: OpenAI-compatible format ensuring multi-provider flexibility.\n" +
                "3. **Cost & Scalability**: Zero-cost free tier access for high-speed evaluation.\n\n" +
                "**Verdict**: OpenRouter provides flexible multi-model routing ideal for dynamic consensus engines.";

        long duration = System.currentTimeMillis() - startTime;
        return AIResponse.builder()
                .provider(getProviderId())
                .modelName(modelName + " (Simulated)")
                .content(content)
                .responseTimeMs(duration)
                .inputTokens(125)
                .outputTokens(210)
                .tokenUsage(335)
                .status("SUCCESS")
                .success(true)
                .build();
    }

    private AIResponse generateSimulatedCritique(String targetProviderId, String question, long startTime) {
        try {
            Thread.sleep(180);
        } catch (InterruptedException ignored) {}

        String critiqueContent = String.format(
                "**OpenRouter Critique on %s:**\n" +
                        "- **Speed & Throughput**: %s's proposal offers reasonable depth.\n" +
                        "- **Counter-Perspective**: However, %s under-indexes on model diversity and flexible failover routing across providers.",
                targetProviderId.toUpperCase(), targetProviderId, targetProviderId
        );

        return AIResponse.builder()
                .provider(getProviderId())
                .modelName(modelName + " (Simulated)")
                .content(critiqueContent)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .inputTokens(80)
                .outputTokens(110)
                .tokenUsage(190)
                .status("SUCCESS")
                .success(true)
                .build();
    }
}
