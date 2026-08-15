package com.aicouncil.provider.mock;

import com.aicouncil.dto.AIResponse;
import com.aicouncil.provider.AIProvider;
import org.springframework.stereotype.Component;

@Component("mockGroqProvider")
public class MockGroqProvider implements AIProvider {

    @Override
    public String getProviderId() {
        return "groq";
    }

    @Override
    public String getProviderName() {
        return "Groq Llama 3.3";
    }

    @Override
    public AIResponse generateResponse(String prompt) {
        long startTime = System.currentTimeMillis();
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
                .modelName(getProviderName() + " [MOCK]")
                .content(content)
                .responseTimeMs(duration)
                .inputTokens(110)
                .outputTokens(220)
                .tokenUsage(330)
                .status("SUCCESS")
                .success(true)
                .build();
    }

    @Override
    public AIResponse generateCritique(String question, String ownResponse, String targetProviderId, String targetResponse) {
        long startTime = System.currentTimeMillis();
        try {
            Thread.sleep(250);
        } catch (InterruptedException ignored) {}

        String critiqueContent = String.format(
                "**Groq Critique on %s:**\n" +
                        "- **Speed & Precision**: %s's proposal offers valuable insight.\n" +
                        "- **Counter-Perspective**: However, %s under-indexes on real-time inference throughput which is critical for scale.",
                targetProviderId.toUpperCase(), targetProviderId, targetProviderId
        );

        long duration = System.currentTimeMillis() - startTime;
        return AIResponse.builder()
                .provider(getProviderId())
                .modelName(getProviderName() + " [MOCK]")
                .content(critiqueContent)
                .responseTimeMs(duration)
                .inputTokens(75)
                .outputTokens(100)
                .tokenUsage(175)
                .status("SUCCESS")
                .success(true)
                .build();
    }
}
