package com.aicouncil.dto;

import java.time.LocalDateTime;

public class ModelResponseDto {
    private String id;
    private String provider;
    private String modelName;
    private String response;
    private Long responseTime;
    private Integer tokenUsage;
    private LocalDateTime createdAt;

    public ModelResponseDto() {}

    public ModelResponseDto(String id, String provider, String modelName, String response, Long responseTime, Integer tokenUsage, LocalDateTime createdAt) {
        this.id = id;
        this.provider = provider;
        this.modelName = modelName;
        this.response = response;
        this.responseTime = responseTime;
        this.tokenUsage = tokenUsage;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public Long getResponseTime() { return responseTime; }
    public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }

    public Integer getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(Integer tokenUsage) { this.tokenUsage = tokenUsage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class Builder {
        private String id;
        private String provider;
        private String modelName;
        private String response;
        private Long responseTime;
        private Integer tokenUsage;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder modelName(String modelName) { this.modelName = modelName; return this; }
        public Builder response(String response) { this.response = response; return this; }
        public Builder responseTime(Long responseTime) { this.responseTime = responseTime; return this; }
        public Builder tokenUsage(Integer tokenUsage) { this.tokenUsage = tokenUsage; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ModelResponseDto build() {
            return new ModelResponseDto(id, provider, modelName, response, responseTime, tokenUsage, createdAt);
        }
    }
}
