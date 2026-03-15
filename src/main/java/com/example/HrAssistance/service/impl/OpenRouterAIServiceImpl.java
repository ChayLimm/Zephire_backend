package com.example.HrAssistance.service.impl;

import com.example.HrAssistance.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenRouterAIServiceImpl implements AIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openrouter.base-url}")
    private String openRouterBaseUrl;

    @Value("${openrouter.model}")
    private String model;

    @Value("${openrouter.token}")
    private String token ;

    @Override
    public String chat(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)  // ✅ correct format
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);  // ✅ required for OpenRouter

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    openRouterBaseUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("choices").get(0)
                        .path("message")
                        .path("content")
                        .asText();  // ✅ OpenAI format
            }

            log.error("❌ OpenRouter returned empty response");
            return null;

        } catch (Exception e) {
            log.error("❌ OpenRouter error: {}", e.getMessage());
            return null;
        }
    }

    public String extractJson(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove markdown code blocks if present
            String cleaned = rawResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // Validate it's actually JSON
            objectMapper.readTree(cleaned);
            return cleaned;

        } catch (Exception e) {
            log.error("❌ Failed to extract JSON from LLM response: {}", e.getMessage());
            return null;
        }
    }
}

