package com.aicouncil.controller;

import com.aicouncil.dto.DebateRequest;
import com.aicouncil.entity.Debate;
import com.aicouncil.exception.GlobalExceptionHandler;
import com.aicouncil.provider.AIProviderRegistry;
import com.aicouncil.provider.GeminiProvider;
import com.aicouncil.provider.GroqProvider;
import com.aicouncil.provider.OpenRouterProvider;
import com.aicouncil.provider.mock.MockGeminiProvider;
import com.aicouncil.provider.mock.MockGroqProvider;
import com.aicouncil.provider.mock.MockOpenRouterProvider;
import com.aicouncil.repository.DebateRepository;
import com.aicouncil.service.DebateOrchestrator;
import com.aicouncil.service.DebateService;
import com.aicouncil.service.JudgeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DebateControllerTest {

    private MockMvc mockMvc;
    private DebateRepository debateRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        debateRepository = mock(DebateRepository.class);

        WebClient client = WebClient.create();
        AIProviderRegistry registry = new AIProviderRegistry(
                new OpenRouterProvider(client), new GeminiProvider(client), new GroqProvider(client),
                new MockOpenRouterProvider(), new MockGeminiProvider(), new MockGroqProvider()
        );
        ReflectionTestUtils.setField(registry, "aiMode", "MOCK");

        JudgeService judgeService = new JudgeService();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.initialize();

        DebateOrchestrator debateOrchestrator = new DebateOrchestrator(registry, judgeService, executor);
        DebateService debateService = new DebateService(debateRepository, debateOrchestrator, registry);

        DebateController debateController = new DebateController(debateService);
        HealthController healthController = new HealthController();

        mockMvc = MockMvcBuilders.standaloneSetup(debateController, healthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /api/debate should return 201 Created for valid request")
    void testCreateDebateValidRequest() throws Exception {
        DebateRequest request = new DebateRequest();
        request.setQuestion("Explain REST");
        request.setModels(List.of("openrouter", "gemini"));

        Debate dummyDebate = Debate.builder()
                .id("debate-123")
                .question("Explain REST")
                .status(Debate.DebateStatus.COMPLETED)
                .finalAnswer("REST synthesis")
                .build();

        when(debateRepository.save(any(Debate.class))).thenReturn(dummyDebate);

        mockMvc.perform(post("/api/debate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debateId").value("debate-123"))
                .andExpect(jsonPath("$.question").value("Explain REST"));
    }

    @Test
    @DisplayName("POST /api/debate should return 400 Bad Request for blank question")
    void testCreateDebateBlankQuestion() throws Exception {
        DebateRequest request = new DebateRequest();
        request.setQuestion("   ");
        request.setModels(List.of("openrouter"));

        mockMvc.perform(post("/api/debate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/debate should return 400 Bad Request for empty models list")
    void testCreateDebateEmptyModelsList() throws Exception {
        DebateRequest request = new DebateRequest();
        request.setQuestion("Valid question?");
        request.setModels(List.of());

        mockMvc.perform(post("/api/debate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/debate should return 201 Created for gemini, groq, openrouter models")
    void testCreateDebateGeminiGroqOpenRouter() throws Exception {
        DebateRequest request = new DebateRequest();
        request.setQuestion("Explain dependency injection in Spring Boot simply.");
        request.setModels(List.of("gemini", "groq", "openrouter"));

        Debate dummyDebate = Debate.builder()
                .id("debate-456")
                .question("Explain dependency injection in Spring Boot simply.")
                .status(Debate.DebateStatus.COMPLETED)
                .finalAnswer("Dependency Injection synthesis")
                .build();

        when(debateRepository.save(any(Debate.class))).thenReturn(dummyDebate);

        mockMvc.perform(post("/api/debate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.debateId").value("debate-456"));
    }

    @Test
    @DisplayName("POST /api/debate should return 400 Bad Request for stale provider 'cerebras'")
    void testCreateDebateInvalidCerebrasProvider() throws Exception {
        DebateRequest request = new DebateRequest();
        request.setQuestion("Explain dependency injection");
        request.setModels(List.of("cerebras", "groq", "gemini"));

        mockMvc.perform(post("/api/debate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid model provider 'cerebras'")));
    }

    @Test
    @DisplayName("GET /api/debate/{id} should return 200 OK for existing ID")
    void testGetDebateByIdSuccess() throws Exception {
        Debate dummyDebate = Debate.builder()
                .id("debate-123")
                .question("Explain REST")
                .status(Debate.DebateStatus.COMPLETED)
                .build();

        when(debateRepository.findById("debate-123")).thenReturn(Optional.of(dummyDebate));

        mockMvc.perform(get("/api/debate/debate-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debateId").value("debate-123"));
    }

    @Test
    @DisplayName("GET /api/debate/{id} should return 404 Not Found for missing ID")
    void testGetDebateByIdNotFound() throws Exception {
        when(debateRepository.findById("missing-id")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/debate/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Debate not found with ID: missing-id"));
    }

    @Test
    @DisplayName("GET /api/debates should return 200 OK")
    void testGetAllDebates() throws Exception {
        when(debateRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        mockMvc.perform(get("/api/debates"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/health should return 200 OK")
    void testGetHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
