package com.aicouncil.dto;

public class AIResponse {
    private String provider;
    private String modelName;
    private String content;
    private Long responseTimeMs;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer tokenUsage;
    private String status; // SUCCESS, FAILED, TIMEOUT
    private boolean success;
    private String errorMessage;

    public AIResponse() {}

    public AIResponse(String provider, String modelName, String content, Long responseTimeMs, Integer inputTokens, Integer outputTokens, Integer tokenUsage, String status, boolean success, String errorMessage) {
        this.provider = provider;
        this.modelName = modelName;
        this.content = content;
        this.responseTimeMs = responseTimeMs;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.tokenUsage = tokenUsage != null ? tokenUsage : ((inputTokens != null ? inputTokens : 0) + (outputTokens != null ? outputTokens : 0));
        this.status = status != null ? status : (success ? "SUCCESS" : "FAILED");
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public Integer getInputTokens() { return inputTokens; }
    public void setInputTokens(Integer inputTokens) { this.inputTokens = inputTokens; }

    public Integer getOutputTokens() { return outputTokens; }
    public void setOutputTokens(Integer outputTokens) { this.outputTokens = outputTokens; }

    public Integer getTokenUsage() { return tokenUsage; }
    public void setTokenUsage(Integer tokenUsage) { this.tokenUsage = tokenUsage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public static class Builder {
        private String provider;
        private String modelName;
        private String content;
        private Long responseTimeMs;
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer tokenUsage;
        private String status;
        private boolean success = true;
        private String errorMessage;

        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder modelName(String modelName) { this.modelName = modelName; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder responseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; return this; }
        public Builder inputTokens(Integer inputTokens) { this.inputTokens = inputTokens; return this; }
        public Builder outputTokens(Integer outputTokens) { this.outputTokens = outputTokens; return this; }
        public Builder tokenUsage(Integer tokenUsage) { this.tokenUsage = tokenUsage; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }

        public AIResponse build() {
            return new AIResponse(provider, modelName, content, responseTimeMs, inputTokens, outputTokens, tokenUsage, status, success, errorMessage);
        }
    }
}
