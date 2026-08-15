package com.aicouncil.service;

import com.aicouncil.dto.AIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JudgeServiceTest {

    private JudgeService judgeService;

    @BeforeEach
    void setUp() {
        judgeService = new JudgeService();
    }

    @Test
    @DisplayName("Test A: Select best model response deterministically when multiple succeed")
    void testSelectsBestModelResponse() {
        Map<String, AIResponse> responses = new HashMap<>();
        responses.put("openrouter", AIResponse.builder().provider("openrouter").modelName("openrouter/free").content("Short text").status("SUCCESS").success(true).build());
        responses.put("groq", AIResponse.builder().provider("groq").modelName("llama-3.3").content("Spring Boot is a Java framework used to build web applications quickly.").status("SUCCESS").success(true).build());
        responses.put("gemini", AIResponse.builder().provider("gemini").modelName("gemini-1.5").content("Medium response text here.").status("SUCCESS").success(true).build());

        var result = judgeService.synthesize("Spring Boot vs Node", responses, List.of());

        assertNotNull(result);
        assertEquals("groq", result.getSelectedModel());
        assertEquals("Spring Boot is a Java framework used to build web applications quickly.", result.getFinalAnswer());
    }

    @Test
    @DisplayName("Test B: Select available successful model when 1 succeeds and 1 fails")
    void testSelectsSingleSuccessfulResponse() {
        Map<String, AIResponse> responses = new HashMap<>();
        responses.put("openrouter", AIResponse.builder().provider("openrouter").modelName("openrouter/free").content("Only OpenRouter succeeded").status("SUCCESS").success(true).build());
        responses.put("groq", AIResponse.builder().provider("groq").modelName("llama-3.3").content("Failed").status("FAILED").success(false).build());

        var result = judgeService.synthesize("Spring Boot vs Node", responses, List.of());

        assertNotNull(result);
        assertEquals("openrouter", result.getSelectedModel());
        assertEquals("Only OpenRouter succeeded", result.getFinalAnswer());
    }

    @Test
    @DisplayName("Test C: Zero successful models should return clear failure response")
    void testSynthesizeZeroSuccessfulResponses() {
        Map<String, AIResponse> responses = new HashMap<>();
        responses.put("openrouter", AIResponse.builder().provider("openrouter").modelName("openrouter/free").content("Failed").status("FAILED").success(false).build());

        var result = judgeService.synthesize("Spring Boot vs Node", responses, List.of());

        assertNotNull(result);
        assertNull(result.getSelectedModel());
        assertTrue(result.getFinalAnswer().contains("Unable to evaluate question"));
        assertTrue(result.getCaveats().contains("SYSTEM FAILURE"));
    }
}
