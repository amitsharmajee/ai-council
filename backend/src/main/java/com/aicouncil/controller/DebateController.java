package com.aicouncil.controller;

import com.aicouncil.dto.DebateRequest;
import com.aicouncil.dto.DebateResponse;
import com.aicouncil.service.DebateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Debate Controller", description = "Endpoints for initiating multi-model AI debates and consensus synthesis")
public class DebateController {

    private final DebateService debateService;

    public DebateController(DebateService debateService) {
        this.debateService = debateService;
    }

    @PostMapping("/debate")
    @Operation(summary = "Start a new multi-model AI debate and synthesize consensus")
    public ResponseEntity<DebateResponse> startDebate(@Valid @RequestBody DebateRequest request) {
        DebateResponse response = debateService.createDebate(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/debate/{id}")
    @Operation(summary = "Retrieve a debate by ID")
    public ResponseEntity<DebateResponse> getDebateById(@PathVariable String id) {
        DebateResponse response = debateService.getDebateById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debates")
    @Operation(summary = "Retrieve history of all debates")
    public ResponseEntity<List<DebateResponse>> getAllDebates() {
        List<DebateResponse> debates = debateService.getAllDebates();
        return ResponseEntity.ok(debates);
    }
}
