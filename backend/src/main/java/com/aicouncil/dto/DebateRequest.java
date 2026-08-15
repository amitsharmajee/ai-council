package com.aicouncil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class DebateRequest {

    @NotBlank(message = "Question prompt cannot be empty")
    @Size(max = 4000, message = "Question prompt cannot exceed 4000 characters")
    private String question;

    @NotEmpty(message = "At least one model provider must be selected")
    private List<String> models;

    public DebateRequest() {}

    public DebateRequest(String question, List<String> models) {
        this.question = question;
        this.models = models;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getModels() { return models; }
    public void setModels(List<String> models) { this.models = models; }
}
