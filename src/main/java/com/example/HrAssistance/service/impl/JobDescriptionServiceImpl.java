package com.example.HrAssistance.service.impl;

import com.example.HrAssistance.model.Candidate;
import com.example.HrAssistance.model.JobDescription;
import com.example.HrAssistance.model.MatchResult;
import com.example.HrAssistance.model.User;
import com.example.HrAssistance.model.dto.request.JobDescriptionRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.JobDescriptionResponse;
import com.example.HrAssistance.model.dto.response.MatchResultResponse;

import com.example.HrAssistance.repositories.JobDescriptionRepo;
import com.example.HrAssistance.repositories.MatchResultRepo;
import com.example.HrAssistance.service.JobDescriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobDescriptionServiceImpl implements JobDescriptionService {

    private final JobDescriptionRepo jobDescriptionRepo;
    private final MatchResultRepo matchResultRepo;
    private final CandidateServiceImpl candidateService;
    private final OpenRouterAIServiceImpl aiService;
    private final ObjectMapper objectMapper;

    // ─────────────────────────────────────────
    // Match JD against candidates — main flow
    // ─────────────────────────────────────────
    public ApiResponse<JobDescriptionResponse> matchCandidates(JobDescriptionRequest request) {

        User currentUser = getCurrentUser();
        String requiredSkillsJson = null;

        try {
            // Convert List<String> to JSON string
            requiredSkillsJson = objectMapper.writeValueAsString(request.getRequiredSkills());
        } catch (JsonProcessingException e) {
            log.error("Failed to convert skills to JSON: {}", e.getMessage());
            throw new RuntimeException("Invalid skills format", e);
        }        // 1. Save JD to DB
        JobDescription jd = JobDescription.builder()
                .title(request.getTitle())
                .field(request.getField())
                .position(request.getPosition())
                .requiredSkills(requiredSkillsJson)
                .minExpYears(request.getMinExpYears())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        JobDescription savedJd = jobDescriptionRepo.save(jd);

        // 2. Pre-filter candidates from DB
        List<Candidate> filtered = candidateService
                .getFilteredCandidates(request.getField(), request.getMinExpYears());

        if (filtered == null || filtered.isEmpty()) {
            List<MatchResult> data = new ArrayList<>();
            JobDescriptionResponse response = toResponse(savedJd, data);
        }

        log.info("✅ Pre-filtered {} candidates for domain: {}",
                filtered.size(), request.getField());

        // 3. Build matching prompt
        String prompt = buildMatchingPrompt(request, filtered);

        // 4. Send to Ollama
        String rawResponse = aiService.chat(prompt);
        String jsonResponse = aiService.extractJson(rawResponse);

        if (jsonResponse == null) {
            return ApiResponse.error("LLM failed to return valid matching results");
        }

        // 5. Parse LLM response + save match results
        List<MatchResult> matchResults = parseAndSaveMatchResults(
                jsonResponse, savedJd, filtered
        );

        // 6. Build response
        JobDescriptionResponse response = toResponse(savedJd, matchResults);
        return ApiResponse.success("Matching complete", response);
    }

    // ─────────────────────────────────────────
    // Get all JDs
    // ─────────────────────────────────────────
    public ApiResponse<List<JobDescriptionResponse>> getAllJds() {
        List<JobDescription> jds = jobDescriptionRepo.findAllByOrderByCreatedAtDesc();

        if (jds.isEmpty()) {
            return ApiResponse.error("No job descriptions found");
        }

        List<JobDescriptionResponse> response = jds.stream()
                .map(jd -> {
                    List<MatchResult> results = matchResultRepo
                            .findByJobDescriptionIdOrderByMatchScoreDesc(jd.getId());
                    return toResponse(jd, results);
                })
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    // ─────────────────────────────────────────
    // Get single JD with results
    // ─────────────────────────────────────────
    public ApiResponse<JobDescriptionResponse> getJdById(Long id) {
        return jobDescriptionRepo.findById(id)
                .map(jd -> {
                    List<MatchResult> results = matchResultRepo
                            .findByJobDescriptionIdOrderByMatchScoreDesc(jd.getId());
                    return ApiResponse.success(toResponse(jd, results));
                })
                .orElse(ApiResponse.error("Job description not found with id: " + id));
    }

    // ─────────────────────────────────────────
    // Delete JD
    // ─────────────────────────────────────────
    public ApiResponse<String> deleteJd(Long id) {
        return jobDescriptionRepo.findById(id)
                .map(jd -> {
                    jobDescriptionRepo.deleteById(id);
                    log.info("✅ JD deleted: {}", id);
                    return ApiResponse.success(
                            "Job description deleted successfully", id.toString()
                    );
                })
                .orElse(ApiResponse.error("Job description not found with id: " + id));
    }

    // ─────────────────────────────────────────
    // Build matching prompt
    // ─────────────────────────────────────────
    private String buildMatchingPrompt(
            JobDescriptionRequest jd,
            List<Candidate> candidates) {

        // Build candidates JSON array
        String candidatesJson = candidates.stream()
                .map(c -> {
                    return """
                            {
                              "id": %d,
                              "name": "%s",
                              "cv_json": %s
                            }
                            """.formatted(
                            c.getId(),
                            c.getName(),
                            c.getCvJson() != null ? c.getCvJson() : "{}"
                    );
                })
                .collect(Collectors.joining(",\n", "[", "]"));

        return """
                You are a CV screening assistant. Rank the candidates below by how well
                they match the job description. Be objective and data-driven.
                Return ONLY a valid JSON array, no extra text, no markdown.
                                
                JOB DESCRIPTION:
                Title: %s
                Field: %s
                Position: %s
                Required Skills: %s
                Minimum Experience: %d years
                Description: %s
                                
                CANDIDATES:
                %s
                                
                Return JSON array sorted by match_score descending:
                [
                  {
                    "candidate_id": 1,
                    "match_score": 85,
                    "match_reasons": ["reason1", "reason2", "reason3"],
                    "gaps": ["gap1", "gap2"]
                  }
                ]
             
                """.formatted(
                jd.getTitle(),
                jd.getField(),
                jd.getPosition(),
                jd.getRequiredSkills(),
                jd.getMinExpYears() != null ? jd.getMinExpYears() : 0,
                jd.getDescription(),
                candidatesJson
        );
    }

    // ─────────────────────────────────────────
    // Parse LLM match results + save to DB
    // ─────────────────────────────────────────
    private List<MatchResult> parseAndSaveMatchResults(
            String jsonResponse,
            JobDescription jd,
            List<Candidate> candidates) {

        List<MatchResult> saved = new ArrayList<>();

        try {
            JsonNode array = objectMapper.readTree(jsonResponse);

            for (JsonNode node : array) {
                Long candidateId = node.path("candidate_id").asLong();
                Integer matchScore = node.path("match_score").asInt();

                // Get the JSON arrays as strings (this preserves the JSON format)
                String matchReasons = node.path("match_reasons").toString();
                String gaps = node.path("gaps").toString();

                // Debug logging to see what's being saved
                log.debug("matchReasons JSON: {}", matchReasons);
                log.debug("gaps JSON: {}", gaps);

                // Find candidate from pre-filtered list
                candidates.stream()
                        .filter(c -> c.getId().equals(candidateId))
                        .findFirst()
                        .ifPresent(candidate -> {
                            try {
                                MatchResult result = MatchResult.builder()
                                        .jobDescription(jd)
                                        .candidate(candidate)
                                        .matchScore(matchScore)
                                        .matchReasons(matchReasons)
                                        .gaps(gaps)
                                        .build();
                                saved.add(matchResultRepo.save(result));
                            } catch (Exception e) {
                                log.error("Failed to save match result for candidate {}: {}",
                                        candidateId, e.getMessage());
                                // Log the actual values that caused the error
                                log.error("matchReasons value: {}", matchReasons);
                                log.error("gaps value: {}", gaps);
                            }
                        });
            }

            log.info("✅ Saved {} match results", saved.size());

        } catch (Exception e) {
            log.error("❌ Failed to parse match results: {}", e.getMessage(), e);
        }

        return saved;
    }

    // ─────────────────────────────────────────
    // Map to response DTO
    // ─────────────────────────────────────────
    private JobDescriptionResponse toResponse(
            JobDescription jd,
            List<MatchResult> matchResults) {

        List<MatchResultResponse> resultResponses = matchResults.stream()
                .map(mr -> {
                    // Parse JSON strings back to List<String>
                    List<String> reasons = parseJsonArray(mr.getMatchReasons());
                    List<String> gaps = parseJsonArray(mr.getGaps());

                    return MatchResultResponse.builder()
                            .id(mr.getId())
                            .candidateId(mr.getCandidate().getId())
                            .candidateName(mr.getCandidate().getName())
                            .candidateEmail(mr.getCandidate().getEmail())
                            .candidatePosition(mr.getCandidate().getPosition())
                            .matchScore(mr.getMatchScore())
                            .matchReasons(reasons)
                            .gaps(gaps)
                            .matchedAt(mr.getMatchedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return JobDescriptionResponse.builder()
                .id(jd.getId())
                .title(jd.getTitle())
                .field(jd.getField())
                .position(jd.getPosition())
                .minExpYears(jd.getMinExpYears())
                .description(jd.getDescription())
                .createdAt(jd.getCreatedAt())
                .createdBy(jd.getCreatedBy() != null
                        ? jd.getCreatedBy().getUsername()
                        : null)
                .matchResults(resultResponses)
                .build();
    }

    private List<String> parseJsonArray(String json) {
        try {
            if (json == null) return new ArrayList<>();
            return objectMapper.readValue(json,
                    new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}