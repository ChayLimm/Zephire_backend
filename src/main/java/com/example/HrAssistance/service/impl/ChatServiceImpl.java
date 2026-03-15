package com.example.HrAssistance.service.impl;

import com.example.HrAssistance.enums.MessageRole;
import com.example.HrAssistance.model.Candidate;
import com.example.HrAssistance.model.ChatMessage;
import com.example.HrAssistance.model.JobDescription;
import com.example.HrAssistance.model.User;
import com.example.HrAssistance.model.dto.request.ChatRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.CandidateResponse;
import com.example.HrAssistance.model.dto.response.ChatMessageResponse;
import com.example.HrAssistance.repositories.CandidateRepo;
import com.example.HrAssistance.repositories.ChatMessageRepo;
import com.example.HrAssistance.repositories.JobDescriptionRepo;
import com.example.HrAssistance.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepo chatMessageRepo;
    private final JobDescriptionRepo jobDescriptionRepo;
    private final CandidateRepo candidateRepo;
    private final CandidateServiceImpl candidateService;
    private final OpenRouterAIServiceImpl aiService;

    // ─────────────────────────────────────────
    // Send message — HR asks, AI answers
    // ─────────────────────────────────────────
    public ApiResponse<ChatMessageResponse> sendMessage(ChatRequest request) {

        User currentUser = getCurrentUser();

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ApiResponse.error("Message cannot be empty");
        }

        String aiResponse;
        Candidate scopedCandidate = null;

        // ── Case 1: Candidate-scoped chat ──
        if (request.getCandidateId() != null) {
            scopedCandidate = candidateRepo.findById(request.getCandidateId())
                    .orElse(null);

            if (scopedCandidate == null) {
                return ApiResponse.error("Candidate not found");
            }
            // Save HR message
            ChatMessage hrMessage = ChatMessage.builder()
                    .user(currentUser)
                    .role(MessageRole.HR)
                    .message(request.getMessage())
                    .candidate(scopedCandidate)
                    .build();
            chatMessageRepo.save(hrMessage);

            String prompt = buildCandidatePrompt(request.getMessage(), scopedCandidate);
            aiResponse = aiService.chat(prompt);

            // ── Case 2: Job-scoped chat ──
        } else if (request.getJdId() != null) {
            Optional<JobDescription> job = jobDescriptionRepo.findById(request.getJdId());
            if (job.isEmpty()) {
                return ApiResponse.error("Job description not found");
            }

            List<Candidate> allCandidates = new ArrayList<>();
            job.get().getMatchResults().forEach(item -> allCandidates.add(item.getCandidate()));

            // Save HR message
            ChatMessage hrMessage = ChatMessage.builder()
                    .user(currentUser)
                    .role(MessageRole.HR)
                    .message(request.getMessage())
                    .jobDescription(job.get())
                    .build();
            chatMessageRepo.save(hrMessage);

            String prompt = buildChatPrompt(request.getMessage(), allCandidates, job);
            aiResponse = aiService.chat(prompt);

            // ── Case 3: General chat (all candidates) ──
        } else {
            List<Candidate> allCandidates = candidateRepo.findAll();

            // Save HR message
            ChatMessage hrMessage = ChatMessage.builder()
                    .user(currentUser)
                    .role(MessageRole.HR)
                    .message(request.getMessage())
                    .build();
            chatMessageRepo.save(hrMessage);

            String prompt = buildChatPrompt(request.getMessage(), allCandidates, null);
            aiResponse = aiService.chat(prompt);
        }

        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return ApiResponse.error("AI failed to respond, please try again");
        }

        // Save AI response
        ChatMessage aiMessage = ChatMessage.builder()
                .user(currentUser)
                .role(MessageRole.ASSISTANT)
                .message(aiResponse)
                .jobDescription(request.getJdId() != null ? getJdIfPresent(request.getJdId()) : null)
                .candidate(scopedCandidate)
                .build();
        ChatMessage saved = chatMessageRepo.save(aiMessage);
        return ApiResponse.success(toResponse(saved));
    }

    // ─────────────────────────────────────────
    // Get general chat history
    // ─────────────────────────────────────────
    public ApiResponse<List<ChatMessageResponse>> getChatHistory() {
        User currentUser = getCurrentUser();
        List<ChatMessage> messages = chatMessageRepo.findByUserIdAndJobDescriptionIsNullAndCandidateIsNullOrderByCreatedAtAsc(currentUser.getId());
        if (messages.isEmpty()) {
            return ApiResponse.error("No chat history found");
        }
        return ApiResponse.success(messages.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────
    // Get job-scoped chat history
    // ─────────────────────────────────────────
    public ApiResponse<List<ChatMessageResponse>> getChatHistoryByJobId(long jobId) {
        List<ChatMessage> messages = chatMessageRepo
                .findByJobDescriptionIdOrderByCreatedAtAsc(jobId);
        if (messages.isEmpty()) {
            return ApiResponse.error("No chat history found");
        }
        return ApiResponse.success(messages.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────
    // Get candidate-scoped chat history
    // ─────────────────────────────────────────
    public ApiResponse<List<ChatMessageResponse>> getChatHistoryByCandidateId(long candidateId) {
        List<ChatMessage> messages = chatMessageRepo
                .findByCandidateIdOrderByCreatedAtAsc(candidateId);
        if (messages.isEmpty()) {
            return ApiResponse.error("No chat history found");
        }
        return ApiResponse.success(messages.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────
    // Clear chat history
    // ─────────────────────────────────────────

    @Transactional
    public ApiResponse<String> clearHistory() {
        User currentUser = getCurrentUser();
        chatMessageRepo.deleteByUserIdAndJobDescriptionIsNullAndCandidateIsNull(currentUser.getId());
        return ApiResponse.success("Chat history cleared", null);
    }

    // ─────────────────────────────────────────
    // Build prompt scoped to a single candidate
    // ─────────────────────────────────────────
    private String buildCandidatePrompt(String message, Candidate candidate) {
        String candidateContext = """
                ID: %d
                Name: %s
                Email: %s
                Phone: %s
                Domain: %s
                Position: %s
                Experience: %d years
                Skills: %s
                Stack: %s
                CV Summary: %s
                """.formatted(
                candidate.getId(),
                candidate.getName(),
                candidate.getEmail(),
                candidate.getPhone() != null ? candidate.getPhone() : "N/A",
                candidate.getDomain(),
                candidate.getPosition(),
                candidate.getExpYears() != null ? candidate.getExpYears() : 0,
                candidate.getSkills() != null ? candidate.getSkills() : "[]",
                candidate.getStack() != null ? candidate.getStack() : "[]",
                candidate.getCvJson() != null ? candidate.getCvJson() : "N/A"
        );

        return """
                You are Maya, a smart HR assistant helping recruiters evaluate a specific candidate.
                
                Guidelines:
                - Answer conversationally and confidently about this candidate
                - Only use information from the candidate profile below
                - Never invent, assume, or infer anything not in the data
                - Keep answers concise unless detail is requested
                - Never ask follow-up questions or offer further help
                
                CANDIDATE PROFILE:
                %s
                
                HR QUESTION:
                %s
                """.formatted(candidateContext, message);
    }

    // ─────────────────────────────────────────
    // Build prompt for job or general chat
    // ─────────────────────────────────────────
    private String buildChatPrompt(String message, List<Candidate> candidates, Optional<JobDescription> job) {

        String candidateContext = candidates != null && !candidates.isEmpty()
                ? candidates.stream()
                .map(c -> "ID:%d | %s | %s | %s | exp:%d yrs | skills:%s | CV:%s".formatted(
                        c.getId(),
                        c.getName(),
                        c.getDomain(),
                        c.getPosition(),
                        c.getExpYears() != null ? c.getExpYears() : 0,
                        c.getSkills() != null ? c.getSkills() : "[]",
                        c.getCvRaw()
                ))
                .collect(Collectors.joining("\n"))
                : "No candidate data available";

        if (job != null && job.isPresent()) {
            return """
                    You are Sok, a smart HR assistant helping recruiters find the right candidates quickly.
                    
                    Guidelines:
                    - Answer conversationally and confidently, like a knowledgeable colleague
                    - Present results clearly but naturally
                    - Never say "based on the data below" or "you provided"
                    - If no candidates match, say so simply
                    - Never invent information not in the data
                    - Keep answers concise unless detail is requested
                    - Never ask follow-up questions or offer further help
                    
                    JOB DESCRIPTION:
                    %s
                    
                    CANDIDATE DATA:
                    %s
                    
                    HR QUESTION:
                    %s
                    """.formatted(job.get(), candidateContext, message);
        }

        return """
                You are Sok, a smart HR assistant helping recruiters find the right candidates quickly.
                
                Guidelines:
                - Answer conversationally and confidently, like a knowledgeable colleague
                - Present results clearly but naturally
                - Never say "based on the data below" or "you provided"
                - If no candidates match, say so simply
                - Never invent information not in the data
                - Keep answers concise unless detail is requested
                - Never ask follow-up questions or offer further help
                
                CANDIDATE DATABASE:
                %s
                
                HR QUESTION:
                %s
                """.formatted(candidateContext, message);
    }

    private JobDescription getJdIfPresent(Long jdId) {
        if (jdId == null) return null;
        return jobDescriptionRepo.findById(jdId).orElse(null);
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .role(message.getRole())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}