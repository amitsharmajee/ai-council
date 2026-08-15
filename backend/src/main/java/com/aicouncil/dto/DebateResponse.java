package com.aicouncil.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DebateResponse {
    private String debateId;
    private String question;
    private String status;
    private String aiMode;
    private String selectedModel;
    private String finalAnswer;
    private String consensus;
    private String agreementPoints;
    private String disagreementPoints;
    private String caveats;
    private LocalDateTime createdAt;
    private List<ModelResponseDto> responses;
    private List<CritiqueDto> critiques;

    public DebateResponse() {}

    public DebateResponse(String debateId, String question, String status, String aiMode, String selectedModel, String finalAnswer, String consensus, String agreementPoints, String disagreementPoints, String caveats, LocalDateTime createdAt, List<ModelResponseDto> responses, List<CritiqueDto> critiques) {
        this.debateId = debateId;
        this.question = question;
        this.status = status;
        this.aiMode = aiMode;
        this.selectedModel = selectedModel;
        this.finalAnswer = finalAnswer;
        this.consensus = consensus;
        this.agreementPoints = agreementPoints;
        this.disagreementPoints = disagreementPoints;
        this.caveats = caveats;
        this.createdAt = createdAt;
        this.responses = responses;
        this.critiques = critiques;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getDebateId() { return debateId; }
    public void setDebateId(String debateId) { this.debateId = debateId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAiMode() { return aiMode; }
    public void setAiMode(String aiMode) { this.aiMode = aiMode; }

    public String getSelectedModel() { return selectedModel; }
    public void setSelectedModel(String selectedModel) { this.selectedModel = selectedModel; }

    public String getFinalAnswer() { return finalAnswer; }
    public void setFinalAnswer(String finalAnswer) { this.finalAnswer = finalAnswer; }

    public String getConsensus() { return consensus; }
    public void setConsensus(String consensus) { this.consensus = consensus; }

    public String getAgreementPoints() { return agreementPoints; }
    public void setAgreementPoints(String agreementPoints) { this.agreementPoints = agreementPoints; }

    public String getDisagreementPoints() { return disagreementPoints; }
    public void setDisagreementPoints(String disagreementPoints) { this.disagreementPoints = disagreementPoints; }

    public String getCaveats() { return caveats; }
    public void setCaveats(String caveats) { this.caveats = caveats; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ModelResponseDto> getResponses() { return responses; }
    public void setResponses(List<ModelResponseDto> responses) { this.responses = responses; }

    public List<CritiqueDto> getCritiques() { return critiques; }
    public void setCritiques(List<CritiqueDto> critiques) { this.critiques = critiques; }

    public static class Builder {
        private String debateId;
        private String question;
        private String status;
        private String aiMode;
        private String selectedModel;
        private String finalAnswer;
        private String consensus;
        private String agreementPoints;
        private String disagreementPoints;
        private String caveats;
        private LocalDateTime createdAt;
        private List<ModelResponseDto> responses;
        private List<CritiqueDto> critiques;

        public Builder debateId(String debateId) { this.debateId = debateId; return this; }
        public Builder question(String question) { this.question = question; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder aiMode(String aiMode) { this.aiMode = aiMode; return this; }
        public Builder selectedModel(String selectedModel) { this.selectedModel = selectedModel; return this; }
        public Builder finalAnswer(String finalAnswer) { this.finalAnswer = finalAnswer; return this; }
        public Builder consensus(String consensus) { this.consensus = consensus; return this; }
        public Builder agreementPoints(String agreementPoints) { this.agreementPoints = agreementPoints; return this; }
        public Builder disagreementPoints(String disagreementPoints) { this.disagreementPoints = disagreementPoints; return this; }
        public Builder caveats(String caveats) { this.caveats = caveats; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder responses(List<ModelResponseDto> responses) { this.responses = responses; return this; }
        public Builder critiques(List<CritiqueDto> critiques) { this.critiques = critiques; return this; }

        public DebateResponse build() {
            return new DebateResponse(debateId, question, status, aiMode, selectedModel, finalAnswer, consensus, agreementPoints, disagreementPoints, caveats, createdAt, responses, critiques);
        }
    }
}
