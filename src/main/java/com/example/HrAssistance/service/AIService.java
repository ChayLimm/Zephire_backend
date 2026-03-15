package com.example.HrAssistance.service;

public interface AIService {

    String chat(String prompt);

    String extractJson(String rawResponse);
}