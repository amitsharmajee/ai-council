package com.aicouncil.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health Controller", description = "System status check")
public class HealthController {

    @Value("${ai.mode:MOCK}")
    private String aiMode = "MOCK";

    @GetMapping("/health")
    @Operation(summary = "Check AI Council system health status")
    public ResponseEntity<Map<String, Object>> getHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "AI Council Backend",
                "aiMode", aiMode != null ? aiMode : "MOCK",
                "timestamp", LocalDateTime.now(),
                "providers", Map.of(
                        "openrouter", "ACTIVE",
                        "gemini", "ACTIVE",
                        "groq", "ACTIVE"
                )
        ));
    }
}
