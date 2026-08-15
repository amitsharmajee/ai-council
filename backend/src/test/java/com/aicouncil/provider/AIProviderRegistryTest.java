package com.aicouncil.provider;

import com.aicouncil.provider.mock.MockGeminiProvider;
import com.aicouncil.provider.mock.MockGroqProvider;
import com.aicouncil.provider.mock.MockOpenRouterProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

class AIProviderRegistryTest {

    private AIProviderRegistry registry;
    private OpenRouterProvider openRouterProvider;
    private GeminiProvider geminiProvider;
    private GroqProvider groqProvider;
    private MockOpenRouterProvider mockOpenRouterProvider;
    private MockGeminiProvider mockGeminiProvider;
    private MockGroqProvider mockGroqProvider;

    @BeforeEach
    void setUp() {
        openRouterProvider = new OpenRouterProvider(WebClient.create());
        geminiProvider = new GeminiProvider(WebClient.create());
        groqProvider = new GroqProvider(WebClient.create());

        mockOpenRouterProvider = new MockOpenRouterProvider();
        mockGeminiProvider = new MockGeminiProvider();
        mockGroqProvider = new MockGroqProvider();

        registry = new AIProviderRegistry(
                openRouterProvider, geminiProvider, groqProvider,
                mockOpenRouterProvider, mockGeminiProvider, mockGroqProvider
        );
        ReflectionTestUtils.setField(registry, "aiMode", "MOCK");
    }

    @Test
    @DisplayName("Should validate supported provider IDs including openrouter and groq")
    void testIsValidProvider() {
        assertTrue(registry.isValidProvider("openrouter"));
        assertTrue(registry.isValidProvider("gemini"));
        assertTrue(registry.isValidProvider("groq"));
        assertTrue(registry.isValidProvider("OPENROUTER"));
        assertFalse(registry.isValidProvider("claude"));
        assertFalse(registry.isValidProvider("cerebras"));
        assertFalse(registry.isValidProvider("openai"));
        assertFalse(registry.isValidProvider("unknown-provider"));
        assertFalse(registry.isValidProvider(""));
        assertFalse(registry.isValidProvider(null));
    }

    @Test
    @DisplayName("Should return mock providers in MOCK mode including openrouter")
    void testGetProviderInMockMode() {
        AIProvider provider = registry.getProvider("openrouter");
        assertNotNull(provider);
        assertInstanceOf(MockOpenRouterProvider.class, provider);
        assertEquals("openrouter", provider.getProviderId());
    }

    @Test
    @DisplayName("Should return live providers in LIVE mode including openrouter")
    void testGetProviderInLiveMode() {
        ReflectionTestUtils.setField(registry, "aiMode", "LIVE");
        AIProvider provider = registry.getProvider("openrouter");
        assertNotNull(provider);
        assertInstanceOf(OpenRouterProvider.class, provider);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid provider ID")
    void testGetInvalidProvider() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> registry.getProvider("invalid_id")
        );
        assertTrue(ex.getMessage().contains("Invalid AI provider ID"));
    }

    @Test
    @DisplayName("Should return all active providers for current mode")
    void testGetAllActiveProviders() {
        var providers = registry.getAllActiveProviders();
        assertEquals(3, providers.size());
    }
}
