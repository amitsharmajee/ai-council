package com.aicouncil.provider.mock;

import com.aicouncil.dto.AIResponse;
import com.aicouncil.provider.AIProvider;
import org.springframework.stereotype.Component;

@Component("mockOpenRouterProvider")
public class MockOpenRouterProvider implements AIProvider {

    @Override
    public String getProviderId() {
        return "openrouter";
    }

    @Override
    public String getProviderName() {
        return "OpenRouter";
    }

    @Override
    public AIResponse generateResponse(String prompt) {
        long startTime = System.currentTimeMillis();
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
                .modelName(getProviderName() + " [MOCK]")
                .content(content)
                .responseTimeMs(duration)
                .inputTokens(125)
                .outputTokens(210)
                .tokenUsage(335)
                .status("SUCCESS")
                .success(true)
                .build();
    }

    @Override
    public AIResponse generateCritique(String question, String ownResponse, String targetProviderId, String targetResponse) {
        long startTime = System.currentTimeMillis();
        try {
            Thread.sleep(180);
        } catch (InterruptedException ignored) {}

        String critiqueContent = String.format(
                "**OpenRouter Critique on %s:**\n" +
                        "- **Speed & Throughput**: %s's proposal offers reasonable depth.\n" +
                        "- **Counter-Perspective**: However, %s under-indexes on model diversity and flexible failover routing across providers.",
                targetProviderId.toUpperCase(), targetProviderId, targetProviderId
        );

        long duration = System.currentTimeMillis() - startTime;
        return AIResponse.builder()
                .provider(getProviderId())
                .modelName(getProviderName() + " [MOCK]")
                .content(critiqueContent)
                .responseTimeMs(duration)
                .inputTokens(80)
                .outputTokens(110)
                .tokenUsage(190)
                .status("SUCCESS")
                .success(true)
                .build();
    }
}
