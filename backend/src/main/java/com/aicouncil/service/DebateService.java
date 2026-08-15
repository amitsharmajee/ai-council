package com.aicouncil.service;

import com.aicouncil.dto.*;
import com.aicouncil.entity.Critique;
import com.aicouncil.entity.Debate;
import com.aicouncil.entity.ModelResponse;
import com.aicouncil.exception.DebateNotFoundException;
import com.aicouncil.provider.AIProviderRegistry;
import com.aicouncil.repository.DebateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DebateService {

    private static final Logger log = LoggerFactory.getLogger(DebateService.class);

    private final DebateRepository debateRepository;
    private final DebateOrchestrator debateOrchestrator;
    private final AIProviderRegistry providerRegistry;

    @Value("${ai.mode:MOCK}")
    private String aiMode;

    public DebateService(DebateRepository debateRepository,
                         DebateOrchestrator debateOrchestrator,
                         AIProviderRegistry providerRegistry) {
        this.debateRepository = debateRepository;
        this.debateOrchestrator = debateOrchestrator;
        this.providerRegistry = providerRegistry;
    }

    public DebateResponse createDebate(DebateRequest request) {
        // Validate provider IDs
        if (request.getModels() != null) {
            for (String modelId : request.getModels()) {
                if (!providerRegistry.isValidProvider(modelId)) {
                    throw new IllegalArgumentException(String.format(
                            "Invalid model provider '%s'. Valid providers are: %s",
                            modelId, String.join(", ", providerRegistry.getSupportedProviders())
                    ));
                }
            }
        }

        log.info("Creating new debate request (mode: {}) for question: {}", aiMode, request.getQuestion());

        Debate debate = Debate.builder()
                .question(request.getQuestion())
                .status(Debate.DebateStatus.IN_PROGRESS)
                .build();

        debate = debateRepository.save(debate);

        DebateOrchestrator.OrchestrationResult result = debateOrchestrator.runDebatePipeline(
                request.getQuestion(), request.getModels()
        );

        List<ModelResponse> savedResponses = new ArrayList<>();
        for (Map.Entry<String, AIResponse> entry : result.initialResponses.entrySet()) {
            AIResponse resp = entry.getValue();
            ModelResponse modelResponse = ModelResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .provider(resp.getProvider())
                    .modelName(resp.getModelName())
                    .response(resp.getContent())
                    .responseTime(resp.getResponseTimeMs())
                    .tokenUsage(resp.getTokenUsage())
                    .build();
            savedResponses.add(modelResponse);
        }
        debate.setResponses(savedResponses);

        List<Critique> savedCritiques = new ArrayList<>();
        for (Map<String, Object> cMap : result.critiques) {
            String provider = (String) cMap.get("provider");
            String targetProvider = (String) cMap.get("targetProvider");
            String critiqueText = (String) cMap.get("critique");

            Critique critique = Critique.builder()
                    .id(UUID.randomUUID().toString())
                    .provider(provider)
                    .targetProvider(targetProvider)
                    .critique(critiqueText)
                    .agreements("Identified points of architectural alignment.")
                    .disagreements("Identified key framework and ecosystem trade-off differences.")
                    .strengths("Acknowledged strengths in latency, tooling, or domain safety.")
                    .weaknesses("Highlighted potential verbosity or event-loop CPU constraints.")
                    .build();
            savedCritiques.add(critique);
        }
        debate.setCritiques(savedCritiques);

        JudgeService.SynthesisResult synth = result.synthesis;
        debate.setSelectedModel(synth.getSelectedModel());
        debate.setFinalAnswer(synth.getFinalAnswer());
        debate.setConsensus(synth.getConsensus());
        debate.setAgreementPoints(synth.getAgreementPoints());
        debate.setDisagreementPoints(synth.getDisagreementPoints());
        debate.setCaveats(synth.getCaveats());
        debate.setStatus(Debate.DebateStatus.COMPLETED);

        debate = debateRepository.save(debate);
        log.info("Debate #{} completed successfully in {} mode (selected model: {}) and persisted to MongoDB", debate.getId(), aiMode, debate.getSelectedModel());

        return mapToResponseDto(debate);
    }

    public DebateResponse getDebateById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Debate ID cannot be empty");
        }
        Debate debate = debateRepository.findById(id)
                .orElseThrow(() -> new DebateNotFoundException("Debate not found with ID: " + id));
        return mapToResponseDto(debate);
    }

    public List<DebateResponse> getAllDebates() {
        return debateRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private DebateResponse mapToResponseDto(Debate debate) {
        List<ModelResponseDto> responseDtos = debate.getResponses() == null ? List.of() : debate.getResponses().stream()
                .map(mr -> ModelResponseDto.builder()
                        .id(mr.getId())
                        .provider(mr.getProvider())
                        .modelName(mr.getModelName())
                        .response(mr.getResponse())
                        .responseTime(mr.getResponseTime())
                        .tokenUsage(mr.getTokenUsage())
                        .createdAt(mr.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<CritiqueDto> critiqueDtos = debate.getCritiques() == null ? List.of() : debate.getCritiques().stream()
                .map(c -> CritiqueDto.builder()
                        .id(c.getId())
                        .provider(c.getProvider())
                        .targetProvider(c.getTargetProvider())
                        .critique(c.getCritique())
                        .agreements(c.getAgreements())
                        .disagreements(c.getDisagreements())
                        .strengths(c.getStrengths())
                        .weaknesses(c.getWeaknesses())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return DebateResponse.builder()
                .debateId(debate.getId())
                .question(debate.getQuestion())
                .status(debate.getStatus().name())
                .aiMode(aiMode)
                .selectedModel(debate.getSelectedModel())
                .finalAnswer(debate.getFinalAnswer())
                .consensus(debate.getConsensus())
                .agreementPoints(debate.getAgreementPoints())
                .disagreementPoints(debate.getDisagreementPoints())
                .caveats(debate.getCaveats())
                .createdAt(debate.getCreatedAt())
                .responses(responseDtos)
                .critiques(critiqueDtos)
                .build();
    }
}
