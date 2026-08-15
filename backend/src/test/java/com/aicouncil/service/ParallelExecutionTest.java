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

import static org.junit.jupiter.api.Assertions.*;

class ParallelExecutionTest {

    private DebateOrchestrator orchestrator;

    static class DelayedAIProvider implements AIProvider {
        private final String id;
        private final long delayMs;

        public DelayedAIProvider(String id, long delayMs) {
            this.id = id;
            this.delayMs = delayMs;
        }

        @Override
        public String getProviderId() { return id; }

        @Override
        public String getProviderName() { return id; }

        @Override
        public AIResponse generateResponse(String prompt) {
            try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
            return AIResponse.builder().provider(id).modelName(id).content("Delayed Ans").responseTimeMs(delayMs).status("SUCCESS").success(true).build();
        }

        @Override
        public AIResponse generateCritique(String q, String own, String t, String tr) {
            return AIResponse.builder().provider(id).content("Critique").status("SUCCESS").success(true).build();
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
        AIProvider p1 = new DelayedAIProvider("openrouter", 300);
        AIProvider p2 = new DelayedAIProvider("groq", 300);
        AIProvider p3 = new DelayedAIProvider("gemini", 300);

        TestAIProviderRegistry registry = new TestAIProviderRegistry(List.of(p1, p2, p3));

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.initialize();

        orchestrator = new DebateOrchestrator(registry, new JudgeService(), executor);
    }

    @Test
    @DisplayName("Verify Stage 1 executes providers in parallel rather than sequentially")
    void testParallelExecutionTiming() {
        long startTime = System.currentTimeMillis();

        var result = orchestrator.runDebatePipeline("Test Concurrency Question", List.of("openrouter", "groq", "gemini"));

        long totalDuration = System.currentTimeMillis() - startTime;

        assertNotNull(result);
        assertEquals(3, result.initialResponses.size());

        // 3 providers with 300ms delays each:
        // Sequential duration would be >= 900ms.
        // Parallel duration should be around 300-600ms.
        assertTrue(totalDuration < 850, "Expected parallel execution time < 850ms, but was " + totalDuration + "ms");
    }
}
