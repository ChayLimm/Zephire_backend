package com.example.HrAssistance.service.impl;
import com.example.HrAssistance.model.dto.request.CVUploadRequest;

import com.example.HrAssistance.enums.CandidateSource;
import com.example.HrAssistance.enums.CandidateStatus;
import com.example.HrAssistance.model.Candidate;
import com.example.HrAssistance.model.User;
import com.example.HrAssistance.model.dto.request.CandidateRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.CandidateResponse;
import com.example.HrAssistance.repositories.CandidateRepo;

import com.example.HrAssistance.service.CandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepo candidateRepo;
    private final PdfServiceImpl pdfService;
    private final OpenRouterAIServiceImpl aiService;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    // ─────────────────────────────────────────
    // Upload CV — full flow
    // ─────────────────────────────────────────
    public ApiResponse<CandidateResponse> uploadCv(CVUploadRequest request) {
        // 1. Get current logged-in HR user
        User currentUser = getCurrentUser();

        // 2. Extract text from PDF
        String rawText = pdfService.extractText(request.getFile());
        if (rawText == null) {
            return ApiResponse.error("Failed to extract text from PDF");
        }

        // 3. Save PDF file to server
        String filePath = saveFile(request.getFile());
        if (filePath == null) {
            return ApiResponse.error("Failed to save PDF file");
        }

        // 4. Send raw text to Ollama for compression
        String compressionPrompt = buildCompressionPrompt(rawText);
        String rawJson = aiService.chat(compressionPrompt);
        String cvJson = aiService.extractJson(rawJson);

        if (cvJson == null) {
            return ApiResponse.error("Failed to compress CV via LLM");
        }

        // 5. Parse JSON to extract key fields for DB columns
        String name = null, email = null, phone = null;
        String domain = null, position = null;
        Integer expYears = null;
        String skills = null, stack = null;

        try {
            JsonNode node = objectMapper.readTree(cvJson);
            name     = node.path("name").asText(null);
            email    = request.getEmail(); //node.path("email").asText(null);
            phone    = request.getPhone();/// node.path("phone").asText(null);
//            domain   = node.path("domain").asText(null);
            position = request.getPosition(); //node.path("position").asText(null);
            expYears = request.getExpYears(); //node.path("exp_years").asInt(0);
            skills   = node.path("skills").toString();
            stack    = node.path("stack").toString();
        } catch (Exception e) {
            log.warn("⚠️ Could not parse some fields from cv_json: {}", e.getMessage());
        }

        // 6. Save candidate to DB
        Candidate candidate = Candidate.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .domain(request.getDomain())
                .position(position)
                .expYears(expYears)
                .skills(skills)
                .stack(stack)
                .fileName(request.getFile().getOriginalFilename())
                .filePath(filePath)
                .cvRaw(rawText)
                .cvJson(cvJson)
                .uploadedBy(currentUser)
                .build();

        Candidate saved = candidateRepo.save(candidate);
        log.info("✅ Candidate saved: {}", saved.getName());

        return ApiResponse.success("CV uploaded successfully", toResponse(saved));
    }


    public ApiResponse<CandidateResponse> updateCandidate(Long id, CandidateRequest request) {
        Candidate data = candidateRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (request.getDomain() != null) data.setDomain(request.getDomain());
        if (request.getEmail() != null) data.setEmail(request.getEmail());
        if (request.getName() != null) data.setName(request.getName());
        if (request.getPhone() != null) data.setPhone(request.getPhone());
        if (request.getExpYears() != null) data.setExpYears(request.getExpYears());
        if (request.getPosition() != null) data.setPosition(request.getPosition());

        candidateRepo.save(data);

        return ApiResponse.success("Candidate updated", data.toResponse());
    }

    // ─────────────────────────────────────────
    // Get all candidates
    // ─────────────────────────────────────────
    public ApiResponse<List<CandidateResponse>> getAllCandidates() {
        List<Candidate> candidates = candidateRepo.findByStatus(CandidateStatus.APPROVED);
        if (candidates.isEmpty()) {
            return ApiResponse.error("No candidates found");
        }
        List<CandidateResponse> response = candidates.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(response);
    }

    // ─────────────────────────────────────────
    // Get single candidate
    // ─────────────────────────────────────────
    public ApiResponse<CandidateResponse> getCandidateById(Long id) {
        return candidateRepo.findById(id)
                .map(candidate -> ApiResponse.success(toResponse(candidate)))
                .orElse(ApiResponse.error("Candidate not found with id: " + id));
    }

    // ─────────────────────────────────────────
    // Delete candidate
    // ─────────────────────────────────────────
    public ApiResponse<String> deleteCandidate(Long id) {
        return candidateRepo.findById(id)
                .map(candidate -> {
                    // Delete PDF file from server
                    deleteFile(candidate.getFilePath());
                    // Delete from DB
                    candidateRepo.deleteById(id);
                    log.info("✅ Candidate deleted: {}", id);
                    return ApiResponse.success("Candidate deleted successfully", id.toString());
                })
                .orElse(ApiResponse.error("Candidate not found with id: " + id));
    }

    // ─────────────────────────────────────────
    // Get PDF file path for streaming
    // ─────────────────────────────────────────
    public String getPdfPath(Long id) {
        return candidateRepo.findById(id)
                .map(Candidate::getFilePath)
                .orElse(null);
    }

    // ─────────────────────────────────────────
    // Get pre-filtered candidates for JD matching
    // ─────────────────────────────────────────
    public List<Candidate> getFilteredCandidates(String domain, Integer minExpYears) {
        if (minExpYears != null && minExpYears > 0) {
            return candidateRepo
                    .findByDomainAndExpYearsGreaterThanEqual(domain, minExpYears);
        }
        return candidateRepo.findByDomain(domain);
    }

    // ─────────────────────────────────────────
    // Save file to disk
    // ─────────────────────────────────────────
    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis()
                    + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("✅ File saved: {}", filePath);
            return filePath.toString();

        } catch (IOException e) {
            log.error("❌ Failed to save file: {}", e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────
    // Delete file from disk
    // ─────────────────────────────────────────
    private void deleteFile(String filePath) {
        try {
            if (filePath != null) {
                Files.deleteIfExists(Paths.get(filePath));
                log.info("✅ File deleted: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("⚠️ Could not delete file: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // Build compression prompt
    // ─────────────────────────────────────────
    private String buildCompressionPrompt(String rawText) {
        return """
                Compress this resume into a single dense JSON object for LLM-based job matching.
                Be extremely concise. No full sentences, use keywords and short phrases only.
                Infer the candidate domain: tech / sales / marketing / finance / hr / operations.
                Return only valid JSON, no extra text, no markdown.
                                
                FORMAT:
                {
                  "name": "",
                  "email": "",
                  "phone": "",
                  "domain": "",
                  "position": "",
                  "exp_years": 0,
                  "skills": [],
                  "stack": [],
                  "roles": ["title|company|duration|one-line-impact"],
                  "projects": ["name|role|tech|outcome"],
                  "awards": ["title|year|result"],
                  "langs": ["language|level"]
                }
                                
                If sales/marketing also include:
                  "channels": [], "metrics": []
                                
                RESUME:
                """
                + rawText;
    }

    // ─────────────────────────────────────────
    // Get current logged-in user from JWT
    // ─────────────────────────────────────────
    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // ─────────────────────────────────────────
    // Map entity to response DTO
    // ─────────────────────────────────────────
    private CandidateResponse toResponse(Candidate candidate) {
        return CandidateResponse.builder()
                .id(candidate.getId())
                .name(candidate.getName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .domain(candidate.getDomain())
                .position(candidate.getPosition())
                .expYears(candidate.getExpYears())
                .fileName(candidate.getFileName())
                .cvJson(candidate.getCvJson())
                .uploadedAt(candidate.getUploadedAt())
                .uploadedBy(candidate.getUploadedBy() != null
                        ? candidate.getUploadedBy().getUsername()
                        : null)
                .build();
    }
    public ApiResponse<CandidateResponse> publicApply(MultipartFile file, CVUploadRequest request) {

        String filePath = saveFile(file);
        if (filePath == null) {
            return ApiResponse.error("Failed to save PDF file");
        }

        String rawText = pdfService.extractText(file);

        Candidate candidate = Candidate.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .domain(request.getDomain())
                .position(request.getPosition())
                .expYears(request.getExpYears())
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .cvRaw(rawText)
                .source(CandidateSource.SELF_APPLIED)
                .status(CandidateStatus.PENDING)
                .build();

        Candidate saved = candidateRepo.save(candidate);
        log.info("✅ Self-applied candidate saved (PENDING): {}", saved.getName());

        return ApiResponse.success("Application submitted successfully", toResponse(saved));
    }

    // ─────────────────────────────────────────
// Approve — triggers Ollama processing
// ─────────────────────────────────────────
    public ApiResponse<CandidateResponse> approveCandidate(Long id) {
        Candidate candidate = candidateRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        // Run Ollama on the raw CV text
        if (candidate.getCvRaw() != null) {
            String compressionPrompt = buildCompressionPrompt(candidate.getCvRaw());
            String rawJson = aiService.chat(compressionPrompt);
            String cvJson = aiService.extractJson(rawJson);

            if (cvJson != null) {
                try {
                    JsonNode node = objectMapper.readTree(cvJson);
                    if (candidate.getPosition() == null) candidate.setPosition(node.path("position").asText(null));
                    if (candidate.getExpYears() == null) candidate.setExpYears(node.path("exp_years").asInt(0));
                    candidate.setSkills(node.path("skills").toString());
                    candidate.setStack(node.path("stack").toString());
                    candidate.setCvJson(cvJson);
                } catch (Exception e) {
                    log.warn("⚠️ Could not parse Ollama response: {}", e.getMessage());
                }
            }
        }

        candidate.setStatus(CandidateStatus.APPROVED);
        candidateRepo.save(candidate);
        log.info("✅ Candidate approved: {}", candidate.getName());

        return ApiResponse.success("Candidate approved", toResponse(candidate));
    }

    // ─────────────────────────────────────────
// Reject candidate
// ─────────────────────────────────────────
    public ApiResponse<CandidateResponse> rejectCandidate(Long id) {
        Candidate candidate = candidateRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.setStatus(CandidateStatus.REJECTED);
        candidateRepo.save(candidate);
        log.info("✅ Candidate rejected: {}", candidate.getName());

        return ApiResponse.success("Candidate rejected", toResponse(candidate));
    }

    // ─────────────────────────────────────────
// Get pending candidates
// ─────────────────────────────────────────
    public ApiResponse<List<CandidateResponse>> getPendingCandidates() {
        List<Candidate> pending = candidateRepo.findByStatus(CandidateStatus.PENDING);
        List<CandidateResponse> response = pending.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(response);
    }


}