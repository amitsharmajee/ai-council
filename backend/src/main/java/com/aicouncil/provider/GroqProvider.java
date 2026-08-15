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
public class GroqProvider implements AIProvider {

    private static final Logger log = LoggerFactory.getLogger(GroqProvider.class);

    private final WebClient webClient;

    @Value("${ai.mode:MOCK}")
    private String aiMode;

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String modelName;

    public GroqProvider(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getProviderId() {
        return "groq";
    }

    @Override
    public String getProviderName() {
        return "Groq (" + modelName + ")";
    }

    @Override
    public AIResponse generateResponse(String prompt) {
        long startTime = System.currentTimeMillis();

        if ("LIVE".equalsIgnoreCase(aiMode)) {
            if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("your_")) {
                log.info("Groq API key missing or unconfigured in LIVE mode, falling back to simulated mock response.");
                return generateSimulatedInitialResponse(prompt, startTime);
            }

            try {
                Map<String, Object> requestBody = Map.of(
                        "model", modelName,
                        "messages", List.of(
                                Map.of("role", "system", "content", com.aicouncil.util.PromptTemplates.AI_SYSTEM_POLICY + "\nYou are Groq Llama 3.3, an ultra-fast high-performance AI reasoning engine. Provide authoritative, concise, production-grade answers."),
                                Map.of("role", "user", "content", prompt)
                        ),
                        "temperature", 0.7
                );

                Map response = webClient.post()
                        .uri(apiUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(30))
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

                        log.info("Successfully received Groq response in {}ms (tokens: {})", duration, totalTokens);

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
                log.error("Groq API HTTP error [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("Groq API HTTP Error: " + e.getStatusCode())
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .status("FAILED")
                        .success(false)
                        .errorMessage("HTTP " + e.getStatusCode() + ": " + e.getMessage())
                        .build();
            } catch (Exception e) {
                log.error("Groq LIVE API request error: {}", e.getMessage());
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("Groq request error: " + e.getMessage())
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
                                Map.of("role", "system", "content", "You are an AI debater evaluating competing architectures. Be objective and precise."),
                                Map.of("role", "user", "content", critiquePrompt)
                        )
                );

                Map response = webClient.post()
                        .uri(apiUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(30))
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
                log.warn("Groq LIVE API critique call failed: {}", e.getMessage());
                return AIResponse.builder()
                        .provider(getProviderId())
                        .modelName(modelName)
                        .content("Groq critique failed: " + e.getMessage())
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
            Thread.sleep(300 + (long)(Math.random() * 200));
        } catch (InterruptedException ignored) {}

        String content = "### Ultra-Fast Architectural Synthesis (Groq Llama 3.3 70B)\n\n" +
                "Evaluating **" + prompt + "** with Groq's high-speed inference engine reveals key trade-offs:\n\n" +
                "#### Core Technical Evaluation:\n" +
                "1. **Inference Latency & Speed**: Sub-second response generation optimized for real-time human interaction.\n" +
                "2. **Open Weights Ecosystem**: Leveraging state-of-the-art open models with high fine-tuning potential.\n" +
                "3. **Cost-Efficiency**: Unmatched token generation throughput per dollar.\n\n" +
                "**Verdict**: Groq Llama 3.3 provides high-speed execution suited for interactive low-latency architectures.";

        long duration = System.currentTimeMillis() - startTime;
        return AIResponse.builder()
                .provider(getProviderId())
                .modelName(modelName + " (Simulated)")
                .content(content)
                .responseTimeMs(duration)
                .inputTokens(110)
                .outputTokens(220)
                .tokenUsage(330)
                .status("SUCCESS")
                .success(true)
                .build();
    }

    private AIResponse generateSimulatedCritique(String targetProviderId, String question, long startTime) {
        try {
            Thread.sleep(250);
        } catch (InterruptedException ignored) {}

        String critiqueContent = String.format(
                "**Groq Critique on %s:**\n" +
                        "- **Speed & Precision**: %s's proposal offers valuable insight.\n" +
                        "- **Counter-Perspective**: However, %s under-indexes on real-time inference throughput which is critical for scale.",
                targetProviderId.toUpperCase(), targetProviderId, targetProviderId
        );

        return AIResponse.builder()
                .provider(getProviderId())
                .modelName(modelName + " (Simulated)")
                .content(critiqueContent)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .inputTokens(75)
                .outputTokens(100)
                .tokenUsage(175)
                .status("SUCCESS")
                .success(true)
                .build();
    }
}
