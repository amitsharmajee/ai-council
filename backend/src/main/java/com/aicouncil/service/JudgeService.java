package com.aicouncil.service;

import com.aicouncil.dto.AIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JudgeService {

    private static final Logger log = LoggerFactory.getLogger(JudgeService.class);

    public static class SynthesisResult {
        private String selectedModel;
        private String finalAnswer;
        private String consensus;
        private String agreementPoints;
        private String disagreementPoints;
        private String caveats;

        public SynthesisResult() {}

        public SynthesisResult(String selectedModel, String finalAnswer, String consensus, String agreementPoints, String disagreementPoints, String caveats) {
            this.selectedModel = selectedModel;
            this.finalAnswer = finalAnswer;
            this.consensus = consensus;
            this.agreementPoints = agreementPoints;
            this.disagreementPoints = disagreementPoints;
            this.caveats = caveats;
        }

        public static Builder builder() {
            return new Builder();
        }

        public String getSelectedModel() { return selectedModel; }
        public String getFinalAnswer() { return finalAnswer; }
        public String getConsensus() { return consensus; }
        public String getAgreementPoints() { return agreementPoints; }
        public String getDisagreementPoints() { return disagreementPoints; }
        public String getCaveats() { return caveats; }

        public static class Builder {
            private String selectedModel;
            private String finalAnswer;
            private String consensus;
            private String agreementPoints;
            private String disagreementPoints;
            private String caveats;

            public Builder selectedModel(String selectedModel) { this.selectedModel = selectedModel; return this; }
            public Builder finalAnswer(String finalAnswer) { this.finalAnswer = finalAnswer; return this; }
            public Builder consensus(String consensus) { this.consensus = consensus; return this; }
            public Builder agreementPoints(String agreementPoints) { this.agreementPoints = agreementPoints; return this; }
            public Builder disagreementPoints(String disagreementPoints) { this.disagreementPoints = disagreementPoints; return this; }
            public Builder caveats(String caveats) { this.caveats = caveats; return this; }

            public SynthesisResult build() {
                return new SynthesisResult(selectedModel, finalAnswer, consensus, agreementPoints, disagreementPoints, caveats);
            }
        }
    }

    /**
     * Deterministically selects the best original Stage 1 response without modifying or rewriting it.
     */
    public SynthesisResult synthesize(String question, Map<String, AIResponse> initialResponses, List<Map<String, Object>> critiques) {
        log.info("Judge selecting best response for question: '{}'", question);

        // Filter ONLY successful Stage 1 responses
        Map<String, AIResponse> validResponses = initialResponses.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue().isSuccess())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        int validCount = validResponses.size();

        // 0 Successful Providers (All Failed)
        if (validCount == 0) {
            log.warn("Judge encountered ZERO successful model responses.");
            return SynthesisResult.builder()
                    .selectedModel(null)
                    .finalAnswer("Unable to evaluate question: All selected AI providers were unavailable or failed to generate responses.")
                    .consensus("No AI providers were available.")
                    .agreementPoints("- None (All provider calls failed)")
                    .disagreementPoints("- None")
                    .caveats("⚠️ **SYSTEM FAILURE**: Please check API network connectivity or provider credentials and retry.")
                    .build();
        }

        // Single or Multi Provider: Select the best response deterministically based on highest content quality/length
        Map.Entry<String, AIResponse> bestEntry = validResponses.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().getContent() != null ? e.getValue().getContent().length() : 0))
                .orElse(validResponses.entrySet().iterator().next());

        String winningProviderId = bestEntry.getKey().toLowerCase();
        AIResponse winningResponse = bestEntry.getValue();

        log.info("Judge deterministically selected provider '{}' (length: {} chars)", winningProviderId, winningResponse.getContent().length());

        return SynthesisResult.builder()
                .selectedModel(winningProviderId)
                .finalAnswer(winningResponse.getContent())
                .consensus(String.format("Response selected from %s.", winningProviderId.toUpperCase()))
                .agreementPoints("- Evaluated Stage 1 provider responses.")
                .disagreementPoints("- Deterministic selection based on response completeness.")
                .caveats(String.format("Original response supplied by %s.", winningProviderId.toUpperCase()))
                .build();
    }
}
