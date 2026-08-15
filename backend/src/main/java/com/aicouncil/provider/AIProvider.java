package com.aicouncil.provider;

import com.aicouncil.dto.AIResponse;

public interface AIProvider {

    /**
     * Unique identifier for the provider (e.g., "openai", "claude", "gemini")
     */
    String getProviderId();

    /**
     * Human-readable display name of the AI model
     */
    String getProviderName();

    /**
     * Generate initial independent response to the question prompt
     */
    AIResponse generateResponse(String prompt);

    /**
     * Generate critique evaluating a competing AI model's response
     */
    AIResponse generateCritique(String question, String ownResponse, String targetProviderId, String targetResponse);
}
