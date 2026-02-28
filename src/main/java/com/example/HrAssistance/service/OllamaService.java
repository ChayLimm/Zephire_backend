package com.example.HrAssistance.service;

public interface OllamaService {

    String chat(String prompt);

    String extractJson(String rawResponse);
}