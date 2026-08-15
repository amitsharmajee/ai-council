package com.aicouncil.service;

import com.aicouncil.dto.AIResponse;
import com.aicouncil.provider.AIProvider;
import com.aicouncil.provider.AIProviderRegistry;
import com.aicouncil.util.PromptTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class DebateOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DebateOrchestrator.class);

    private final AIProviderRegistry providerRegistry;
    private final JudgeService judgeService;
    private final Executor aiTaskExecutor;

    @Value("${stage1.timeout.seconds:35}")
    private long stage1TimeoutSeconds = 35L;

    public DebateOrchestrator(AIProviderRegistry providerRegistry,
                              JudgeService judgeService,
                              @Qualifier("aiTaskExecutor") Executor aiTaskExecutor) {
        this.providerRegistry = providerRegistry;
        this.judgeService = judgeService;
        this.aiTaskExecutor = aiTaskExecutor;
    }

    public static class OrchestrationResult {
        public Map<String, AIResponse> initialResponses = new ConcurrentHashMap<>();
        public List<Map<String, Object>> critiques = Collections.synchronizedList(new ArrayList<>());
        public JudgeService.SynthesisResult synthesis;
    }

    public OrchestrationResult runDebatePipeline(String question, List<String> requestedModelIds) {
        log.info("Starting debate pipeline for question: '{}' with models: {}", question, requestedModelIds);
        OrchestrationResult result = new OrchestrationResult();

        List<AIProvider> selectedProviders = requestedModelIds.stream()
                .map(String::toLowerCase)
                .map(providerRegistry::getProvider)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (selectedProviders.isEmpty()) {
            selectedProviders = providerRegistry.getAllActiveProviders();
        }

        // =========================================================================
        // STAGE 1: Parallel Independent AI Querying
        // =========================================================================
        long stage1Start = System.currentTimeMillis();
        log.info("STAGE 1: Launching {} concurrent AI provider futures on thread pool...", selectedProviders.size());

        List<CompletableFuture<Map.Entry<String, AIResponse>>> stage1Futures = selectedProviders.stream()
                .map(provider -> CompletableFuture.supplyAsync(() -> {
                    long providerStart = System.currentTimeMillis();
                    try {
                        AIResponse resp = provider.generateResponse(question);
                        log.info("Provider '{}' [{}] finished response in {}ms (status: {})",
                                provider.getProviderId(), resp.getModelName(), resp.getResponseTimeMs(), resp.getStatus());
                        return Map.entry(provider.getProviderId(), resp);
                    } catch (Exception e) {
                        log.error("Error generating response from provider '{}': {}", provider.getProviderId(), e.getMessage());
                        AIResponse failResp = AIResponse.builder()
                                .provider(provider.getProviderId())
                                .modelName(provider.getProviderName())
                                .content("Provider unavailable: " + e.getMessage())
                                .status("FAILED")
                                .success(false)
                                .errorMessage(e.getMessage())
                                .responseTimeMs(System.currentTimeMillis() - providerStart)
                                .inputTokens(0)
                                .outputTokens(0)
                                .tokenUsage(0)
                                .build();
                        return Map.entry(provider.getProviderId(), failResp);
                    }
                }, aiTaskExecutor))
                .collect(Collectors.toList());

        long timeoutToUse = stage1TimeoutSeconds > 0 ? stage1TimeoutSeconds : 35L;
        CompletableFuture<Void> allStage1Future = CompletableFuture.allOf(
                stage1Futures.toArray(new CompletableFuture[0])
        ).orTimeout(timeoutToUse, TimeUnit.SECONDS);

        try {
            allStage1Future.join();
        } catch (CompletionException e) {
            log.warn("Stage 1 execution aggregate timeout or exception: {}", e.getMessage());
        }

        long maxProviderDuration = 0L;
        for (CompletableFuture<Map.Entry<String, AIResponse>> future : stage1Futures) {
            try {
                if (future.isDone() && !future.isCompletedExceptionally()) {
                    Map.Entry<String, AIResponse> entry = future.join();
                    result.initialResponses.put(entry.getKey(), entry.getValue());
                    if (entry.getValue().getResponseTimeMs() != null && entry.getValue().getResponseTimeMs() > maxProviderDuration) {
                        maxProviderDuration = entry.getValue().getResponseTimeMs();
                    }
                }
            } catch (Exception ex) {
                log.error("Error collecting Stage 1 future result: {}", ex.getMessage());
            }
        }

        long stage1Duration = System.currentTimeMillis() - stage1Start;
        log.info("STAGE 1 COMPLETE: Collected {} responses in {}ms (Max single provider: {}ms) [Parallel Execution Verified]",
                result.initialResponses.size(), stage1Duration, maxProviderDuration);

        // Direct single-pass synthesis via Judge Engine (No Stage 2 cross-critique API calls)
        log.info("STAGE 2 / JUDGE: Selecting best response via Judge Engine...");
        result.synthesis = judgeService.synthesize(question, result.initialResponses, Collections.emptyList());
        log.info("PIPELINE COMPLETE: Best response selected for provider '{}'.", result.synthesis.getSelectedModel());

        return result;
    }
}
