package com.aicouncil.service;

import com.aicouncil.dto.AIResponse;
import com.aicouncil.dto.DebateRequest;
import com.aicouncil.dto.DebateResponse;
import com.aicouncil.entity.Debate;
import com.aicouncil.exception.DebateNotFoundException;
import com.aicouncil.provider.AIProvider;
import com.aicouncil.provider.AIProviderRegistry;
import com.aicouncil.provider.GeminiProvider;
import com.aicouncil.provider.GroqProvider;
import com.aicouncil.provider.OpenRouterProvider;
import com.aicouncil.provider.mock.MockGeminiProvider;
import com.aicouncil.provider.mock.MockGroqProvider;
import com.aicouncil.provider.mock.MockOpenRouterProvider;
import com.aicouncil.repository.DebateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DebateServiceTest {

    private DebateService debateService;
    private DebateRepository debateRepository;

    static class StubMockOpenRouter extends MockOpenRouterProvider {
        @Override public AIResponse generateResponse(String p) { return AIResponse.builder().provider("openrouter").modelName("MockOpenRouter").content("REST Ans").status("SUCCESS").success(true).build(); }
        @Override public AIResponse generateCritique(String q, String own, String t, String tr) { return AIResponse.builder().provider("openrouter").content("Critique").status("SUCCESS").success(true).build(); }
    }

    static class StubMockGemini extends MockGeminiProvider {
        @Override public AIResponse generateResponse(String p) { return AIResponse.builder().provider("gemini").modelName("MockGemini").content("REST Ans").status("SUCCESS").success(true).build(); }
        @Override public AIResponse generateCritique(String q, String own, String t, String tr) { return AIResponse.builder().provider("gemini").content("Critique").status("SUCCESS").success(true).build(); }
    }

    @BeforeEach
    void setUp() {
        debateRepository = mock(DebateRepository.class);

        WebClient client = WebClient.create();
        AIProviderRegistry registry = new AIProviderRegistry(
                new OpenRouterProvider(client), new GeminiProvider(client), new GroqProvider(client),
                new StubMockOpenRouter(), new StubMockGemini(), new MockGroqProvider()
        );
        ReflectionTestUtils.setField(registry, "aiMode", "MOCK");

        JudgeService judgeService = new JudgeService();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.initialize();

        DebateOrchestrator debateOrchestrator = new DebateOrchestrator(registry, judgeService, executor);
        debateService = new DebateService(debateRepository, debateOrchestrator, registry);
    }

    @Test
    @DisplayName("Should create debate successfully")
    void testCreateDebateSuccess() {
        DebateRequest request = new DebateRequest();
        request.setQuestion("What is REST?");
        request.setModels(List.of("openrouter", "gemini"));

        Debate savedDebate = Debate.builder()
                .id("debate-123")
                .question("What is REST?")
                .status(Debate.DebateStatus.COMPLETED)
                .finalAnswer("Final REST Answer")
                .build();

        when(debateRepository.save(any(Debate.class))).thenReturn(savedDebate);

        DebateResponse response = debateService.createDebate(request);

        assertNotNull(response);
        assertEquals("debate-123", response.getDebateId());
        assertEquals("What is REST?", response.getQuestion());
        verify(debateRepository, atLeastOnce()).save(any(Debate.class));
    }

    @Test
    @DisplayName("Should retrieve debate by ID")
    void testGetDebateByIdSuccess() {
        Debate debate = Debate.builder()
                .id("debate-123")
                .question("What is REST?")
                .status(Debate.DebateStatus.COMPLETED)
                .build();

        when(debateRepository.findById("debate-123")).thenReturn(Optional.of(debate));

        DebateResponse response = debateService.getDebateById("debate-123");

        assertNotNull(response);
        assertEquals("debate-123", response.getDebateId());
    }

    @Test
    @DisplayName("Should throw DebateNotFoundException when ID is missing")
    void testGetDebateByIdNotFound() {
        when(debateRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(DebateNotFoundException.class, () -> debateService.getDebateById("missing-id"));
    }

    @Test
    @DisplayName("Should retrieve all debates ordered by creation time")
    void testGetAllDebates() {
        Debate d1 = Debate.builder().id("1").question("Q1").status(Debate.DebateStatus.COMPLETED).build();
        Debate d2 = Debate.builder().id("2").question("Q2").status(Debate.DebateStatus.COMPLETED).build();

        when(debateRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(d1, d2));

        List<DebateResponse> debates = debateService.getAllDebates();

        assertEquals(2, debates.size());
        assertEquals("1", debates.get(0).getDebateId());
        assertEquals("2", debates.get(1).getDebateId());
    }
}
