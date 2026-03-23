//package com.example.HrAssistance.service.impl;
//
//import com.example.HrAssistance.service.AIService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.JsonNode;
//
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class OllamaServiceImpl implements AIService {
//
//    private final RestTemplate restTemplate;
//    private final ObjectMapper objectMapper;
//
//    @Value("${ollama.base-url:http://localhost:11434}")
//    private String ollamaBaseUrl;
//
//    @Value("${ollama.model:qwen2.5:14b-instruct}")
//    private String model;
//
//    // ─────────────────────────────────────────
//    // Core method — send prompt, get text back
//    // ─────────────────────────────────────────
//    public String chat(String prompt) {
//        try {
//            String url = ollamaBaseUrl + "/api/generate";
//
//            Map<String, Object> requestBody = Map.of(
//                    "model", model,
//                    "prompt", prompt,
//                    "stream", false
//            );
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//
//            ResponseEntity<String> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.POST,
//                    entity,
//                    String.class
//            );
//
//            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
//                JsonNode root = objectMapper.readTree(response.getBody());
//                return root.path("response").asText();
//            }
//
//            log.error("❌ Ollama returned empty response");
//            return null;
//
//        } catch (Exception e) {
//            log.error("❌ Ollama error: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    // ─────────────────────────────────────────
//    // Extract JSON string from LLM response
//    // LLM sometimes wraps JSON in ```json ... ```
//    // ─────────────────────────────────────────
//    public String extractJson(String rawResponse) {
//        if (rawResponse == null || rawResponse.trim().isEmpty()) {
//            return null;
//        }
//
//        try {
//            // Remove markdown code blocks if present
//            String cleaned = rawResponse
//                    .replaceAll("```json", "")
//                    .replaceAll("```", "")
//                    .trim();
//
//            // Validate it's actually JSON
//            objectMapper.readTree(cleaned);
//            return cleaned;
//
//        } catch (Exception e) {
//            log.error("❌ Failed to extract JSON from LLM response: {}", e.getMessage());
//            return null;
//        }
//    }
//}