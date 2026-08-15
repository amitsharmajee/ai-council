package com.aicouncil.dto;

import java.time.LocalDateTime;

public class CritiqueDto {
    private String id;
    private String provider;
    private String targetProvider;
    private String critique;
    private String agreements;
    private String disagreements;
    private String strengths;
    private String weaknesses;
    private LocalDateTime createdAt;

    public CritiqueDto() {}

    public CritiqueDto(String id, String provider, String targetProvider, String critique, String agreements, String disagreements, String strengths, String weaknesses, LocalDateTime createdAt) {
        this.id = id;
        this.provider = provider;
        this.targetProvider = targetProvider;
        this.critique = critique;
        this.agreements = agreements;
        this.disagreements = disagreements;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getTargetProvider() { return targetProvider; }
    public void setTargetProvider(String targetProvider) { this.targetProvider = targetProvider; }

    public String getCritique() { return critique; }
    public void setCritique(String critique) { this.critique = critique; }

    public String getAgreements() { return agreements; }
    public void setAgreements(String agreements) { this.agreements = agreements; }

    public String getDisagreements() { return disagreements; }
    public void setDisagreements(String disagreements) { this.disagreements = disagreements; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class Builder {
        private String id;
        private String provider;
        private String targetProvider;
        private String critique;
        private String agreements;
        private String disagreements;
        private String strengths;
        private String weaknesses;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder targetProvider(String targetProvider) { this.targetProvider = targetProvider; return this; }
        public Builder critique(String critique) { this.critique = critique; return this; }
        public Builder agreements(String agreements) { this.agreements = agreements; return this; }
        public Builder disagreements(String disagreements) { this.disagreements = disagreements; return this; }
        public Builder strengths(String strengths) { this.strengths = strengths; return this; }
        public Builder weaknesses(String weaknesses) { this.weaknesses = weaknesses; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public CritiqueDto build() {
            return new CritiqueDto(id, provider, targetProvider, critique, agreements, disagreements, strengths, weaknesses, createdAt);
        }
    }
}
