package com.aicouncil.provider;

import com.aicouncil.provider.mock.MockGeminiProvider;
import com.aicouncil.provider.mock.MockGroqProvider;
import com.aicouncil.provider.mock.MockOpenRouterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AIProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(AIProviderRegistry.class);

    private final Map<String, AIProvider> liveProviders = new HashMap<>();
    private final Map<String, AIProvider> mockProviders = new HashMap<>();
    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("openrouter", "groq", "gemini");

    @Value("${ai.mode:MOCK}")
    private String aiMode;

    public AIProviderRegistry(OpenRouterProvider openRouterProvider,
                               GeminiProvider geminiProvider,
                               GroqProvider groqProvider,
                               MockOpenRouterProvider mockOpenRouterProvider,
                               MockGeminiProvider mockGeminiProvider,
                               MockGroqProvider mockGroqProvider) {

        liveProviders.put(openRouterProvider.getProviderId().toLowerCase(), openRouterProvider);
        liveProviders.put(geminiProvider.getProviderId().toLowerCase(), geminiProvider);
        liveProviders.put(groqProvider.getProviderId().toLowerCase(), groqProvider);

        mockProviders.put(mockOpenRouterProvider.getProviderId().toLowerCase(), mockOpenRouterProvider);
        mockProviders.put(mockGeminiProvider.getProviderId().toLowerCase(), mockGeminiProvider);
        mockProviders.put(mockGroqProvider.getProviderId().toLowerCase(), mockGroqProvider);
    }

    public boolean isValidProvider(String providerId) {
        if (providerId == null || providerId.isBlank()) return false;
        return SUPPORTED_PROVIDERS.contains(providerId.trim().toLowerCase());
    }

    public Set<String> getSupportedProviders() {
        return SUPPORTED_PROVIDERS;
    }

    public AIProvider getProvider(String providerId) {
        if (!isValidProvider(providerId)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid AI provider ID '%s'. Supported providers are: %s",
                    providerId, String.join(", ", SUPPORTED_PROVIDERS)
            ));
        }

        String key = providerId.toLowerCase().trim();
        boolean isMock = "MOCK".equalsIgnoreCase(aiMode);
        Map<String, AIProvider> registry = isMock ? mockProviders : liveProviders;

        AIProvider provider = registry.get(key);
        if (provider == null) {
            log.warn("Provider '{}' not found in mode '{}', falling back to mock provider", providerId, aiMode);
            provider = mockProviders.get(key);
        }
        return provider;
    }

    public List<AIProvider> getAllActiveProviders() {
        boolean isMock = "MOCK".equalsIgnoreCase(aiMode);
        return new ArrayList<>((isMock ? mockProviders : liveProviders).values());
    }

    public String getAiMode() {
        return aiMode;
    }
}
