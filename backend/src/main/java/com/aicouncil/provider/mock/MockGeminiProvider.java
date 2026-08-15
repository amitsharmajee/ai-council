package com.aicouncil.provider.mock;

import com.aicouncil.dto.AIResponse;
import com.aicouncil.provider.AIProvider;
import org.springframework.stereotype.Component;

@Component("mockGeminiProvider")
public class MockGeminiProvider implements AIProvider {

    @Override
    public String getProviderId() {
        return "gemini";
    }

    @Override
    public String getProviderName() {
        return "Google Gemini 1.5";
    }

    @Override
    public AIResponse generateResponse(String prompt) {
        long startTime = System.currentTimeMillis();
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
                .modelName(getProviderName() + " [MOCK]")
                .content(content)
                .responseTimeMs(duration)
                .inputTokens(125)
                .outputTokens(270)
                .tokenUsage(395)
                .status("SUCCESS")
                .success(true)
                .build();
    }

    @Override
    public AIResponse generateCritique(String question, String ownResponse, String targetProviderId, String targetResponse) {
        long startTime = System.currentTimeMillis();
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

        long duration = System.currentTimeMillis() - startTime;
        return AIResponse.builder()
                .provider(getProviderId())
                .modelName(getProviderName() + " [MOCK]")
                .content(critiqueContent)
                .responseTimeMs(duration)
                .inputTokens(85)
                .outputTokens(115)
                .tokenUsage(200)
                .status("SUCCESS")
                .success(true)
                .build();
    }
}
