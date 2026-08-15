package com.aicouncil.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "debates")
public class Debate {

    @Id
    private String id;

    private String question;

    private DebateStatus status;

    private String selectedModel;

    private String finalAnswer;

    private String consensus;

    private String agreementPoints;

    private String disagreementPoints;

    private String caveats;

    @Indexed
    private LocalDateTime createdAt;

    private List<ModelResponse> responses = new ArrayList<>();

    private List<Critique> critiques = new ArrayList<>();

    public Debate() {
        this.createdAt = LocalDateTime.now();
        this.status = DebateStatus.IN_PROGRESS;
    }

    public Debate(String id, String question, DebateStatus status, String selectedModel, String finalAnswer, String consensus, String agreementPoints, String disagreementPoints, String caveats, LocalDateTime createdAt, List<ModelResponse> responses, List<Critique> critiques) {
        this.id = id;
        this.question = question;
        this.status = status != null ? status : DebateStatus.IN_PROGRESS;
        this.selectedModel = selectedModel;
        this.finalAnswer = finalAnswer;
        this.consensus = consensus;
        this.agreementPoints = agreementPoints;
        this.disagreementPoints = disagreementPoints;
        this.caveats = caveats;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        if (responses != null) this.responses = responses;
        if (critiques != null) this.critiques = critiques;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum DebateStatus {
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public DebateStatus getStatus() { return status; }
    public void setStatus(DebateStatus status) { this.status = status; }

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

    public List<ModelResponse> getResponses() { return responses; }
    public void setResponses(List<ModelResponse> responses) { this.responses = responses; }

    public List<Critique> getCritiques() { return critiques; }
    public void setCritiques(List<Critique> critiques) { this.critiques = critiques; }

    public static class Builder {
        private String id;
        private String question;
        private DebateStatus status;
        private String selectedModel;
        private String finalAnswer;
        private String consensus;
        private String agreementPoints;
        private String disagreementPoints;
        private String caveats;
        private LocalDateTime createdAt;
        private List<ModelResponse> responses = new ArrayList<>();
        private List<Critique> critiques = new ArrayList<>();

        public Builder id(String id) { this.id = id; return this; }
        public Builder question(String question) { this.question = question; return this; }
        public Builder status(DebateStatus status) { this.status = status; return this; }
        public Builder selectedModel(String selectedModel) { this.selectedModel = selectedModel; return this; }
        public Builder finalAnswer(String finalAnswer) { this.finalAnswer = finalAnswer; return this; }
        public Builder consensus(String consensus) { this.consensus = consensus; return this; }
        public Builder agreementPoints(String agreementPoints) { this.agreementPoints = agreementPoints; return this; }
        public Builder disagreementPoints(String disagreementPoints) { this.disagreementPoints = disagreementPoints; return this; }
        public Builder caveats(String caveats) { this.caveats = caveats; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder responses(List<ModelResponse> responses) { this.responses = responses; return this; }
        public Builder critiques(List<Critique> critiques) { this.critiques = critiques; return this; }

        public Debate build() {
            return new Debate(id, question, status, selectedModel, finalAnswer, consensus, agreementPoints, disagreementPoints, caveats, createdAt, responses, critiques);
        }
    }
}
