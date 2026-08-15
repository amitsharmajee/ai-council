package com.aicouncil.service;

import com.aicouncil.dto.AIResponse;
import com.aicouncil.provider.AIProvider;
import com.aicouncil.provider.AIProviderRegistry;
import com.aicouncil.provider.GeminiProvider;
import com.aicouncil.provider.GroqProvider;
import com.aicouncil.provider.OpenRouterProvider;
import com.aicouncil.provider.mock.MockGeminiProvider;
import com.aicouncil.provider.mock.MockGroqProvider;
import com.aicouncil.provider.mock.MockOpenRouterProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class DebateOrchestratorTest {

    private DebateOrchestrator orchestrator;
    private JudgeService judgeService;

    private TestAIProvider testOpenRouter;
    private TestAIProvider testGroq;
    private TestAIProvider testGemini;

    static class TestAIProvider implements AIProvider {
        private final String id;
        private final String name;
        private Function<String, AIResponse> responseHandler;
        private Function<String, AIResponse> critiqueHandler;

        public TestAIProvider(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public void setResponseHandler(Function<String, AIResponse> handler) {
            this.responseHandler = handler;
        }

        public void setCritiqueHandler(Function<String, AIResponse> handler) {
            this.critiqueHandler = handler;
        }

        @Override
        public String getProviderId() { return id; }

        @Override
        public String getProviderName() { return name; }

        @Override
        public AIResponse generateResponse(String prompt) {
            if (responseHandler != null) return responseHandler.apply(prompt);
            return AIResponse.builder().provider(id).modelName(name).content("Success").status("SUCCESS").success(true).build();
        }

        @Override
        public AIResponse generateCritique(String question, String ownResponse, String targetProviderId, String targetResponse) {
            if (critiqueHandler != null) return critiqueHandler.apply(targetProviderId);
            return AIResponse.builder().provider(id).modelName(name).content("Critique").status("SUCCESS").success(true).build();
        }
    }

    static class TestAIProviderRegistry extends AIProviderRegistry {
        private final Map<String, AIProvider> providers = new HashMap<>();

        public TestAIProviderRegistry(List<AIProvider> providerList) {
            super(
                    new OpenRouterProvider(WebClient.create()),
                    new GeminiProvider(WebClient.create()),
                    new GroqProvider(WebClient.create()),
                    new MockOpenRouterProvider(),
                    new MockGeminiProvider(),
                    new MockGroqProvider()
            );
            for (AIProvider p : providerList) {
                providers.put(p.getProviderId().toLowerCase(), p);
            }
        }

        @Override
        public boolean isValidProvider(String id) {
            return id != null && providers.containsKey(id.toLowerCase().trim());
        }

        @Override
        public AIProvider getProvider(String id) {
            if (id == null) return null;
            return providers.get(id.toLowerCase().trim());
        }

        @Override
        public List<AIProvider> getAllActiveProviders() {
            return new ArrayList<>(providers.values());
        }
    }

    @BeforeEach
    void setUp() {
        judgeService = new JudgeService();
        testOpenRouter = new TestAIProvider("openrouter", "OpenRouter");
        testGroq = new TestAIProvider("groq", "Groq");
        testGemini = new TestAIProvider("gemini", "Gemini");

        TestAIProviderRegistry registry = new TestAIProviderRegistry(List.of(testOpenRouter, testGroq, testGemini));

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.initialize();

        orchestrator = new DebateOrchestrator(registry, judgeService, executor);
    }

    @Test
    @DisplayName("Scenario A: All 3 providers succeed")
    void testAllProvidersSucceed() {
        testOpenRouter.setResponseHandler(p -> AIResponse.builder().provider("openrouter").modelName("openrouter/free").content("OpenRouter Ans").status("SUCCESS").success(true).responseTimeMs(100L).build());
        testGroq.setResponseHandler(p -> AIResponse.builder().provider("groq").modelName("llama-3.3").content("Groq Ans").status("SUCCESS").success(true).responseTimeMs(100L).build());
        testGemini.setResponseHandler(p -> AIResponse.builder().provider("gemini").modelName("gemini-1.5").content("Gemini Ans").status("SUCCESS").success(true).responseTimeMs(100L).build());

        var result = orchestrator.runDebatePipeline("What is REST?", List.of("openrouter", "groq", "gemini"));

        assertNotNull(result);
        assertEquals(3, result.initialResponses.size());
        assertEquals(0, result.critiques.size());
        assertNotNull(result.synthesis);
    }

    @Test
    @DisplayName("Scenario B: 1 provider fails, remaining 2 succeed")
    void testOneProviderFails() {
        testOpenRouter.setResponseHandler(p -> AIResponse.builder().provider("openrouter").modelName("openrouter/free").content("OpenRouter Ans").status("SUCCESS").success(true).responseTimeMs(100L).build());
        testGroq.setResponseHandler(p -> { throw new RuntimeException("Groq API error"); });
        testGemini.setResponseHandler(p -> AIResponse.builder().provider("gemini").modelName("gemini-1.5").content("Gemini Ans").status("SUCCESS").success(true).responseTimeMs(100L).build());

        var result = orchestrator.runDebatePipeline("What is REST?", List.of("openrouter", "groq", "gemini"));

        assertNotNull(result);
        assertEquals(3, result.initialResponses.size());
        assertEquals("FAILED", result.initialResponses.get("groq").getStatus());
        assertEquals(0, result.critiques.size());
        assertNotNull(result.synthesis);
    }

    @Test
    @DisplayName("Scenario C: All providers fail")
    void testAllProvidersFail() {
        testOpenRouter.setResponseHandler(p -> { throw new RuntimeException("OpenRouter fail"); });
        testGroq.setResponseHandler(p -> { throw new RuntimeException("Groq fail"); });
        testGemini.setResponseHandler(p -> { throw new RuntimeException("Gemini fail"); });

        var result = orchestrator.runDebatePipeline("What is REST?", List.of("openrouter", "groq", "gemini"));

        assertNotNull(result);
        assertEquals(3, result.initialResponses.size());
        assertEquals(0, result.critiques.size());
        assertTrue(result.synthesis.getFinalAnswer().contains("Unable to evaluate question"));
    }
}
