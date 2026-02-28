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

import com.example.HrAssistance.repositories.ChatMessageRepo;
import com.example.HrAssistance.repositories.JobDescriptionRepo;
import com.example.HrAssistance.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    private final CandidateServiceImpl candidateService;
    private final OllamaServiceImpl ollamaService;

    // ─────────────────────────────────────────
    // Send message — HR asks, AI answers
    // ─────────────────────────────────────────
    public ApiResponse<ChatMessageResponse> sendMessage(ChatRequest request) {

        User currentUser = getCurrentUser();
        String aiResponse;
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ApiResponse.error("Message cannot be empty");
        }

        if (request.getJdId() == null){
            // 1. Save HR message
            ChatMessage hrMessage = ChatMessage.builder()
                    .user(currentUser)
                    .role(MessageRole.HR)
                    .message(request.getMessage())
                    .jobDescription(getJdIfPresent(request.getJdId()))
                    .build();
            chatMessageRepo.save(hrMessage);

            // 2. Get all candidates for context
            List<CandidateResponse> allCandidatesRes= candidateService.getAllCandidates().getData();
            List<Candidate> allCandidates = candidateService.getAllCandidates().getData()
                    .stream()
                    .map(CandidateResponse::toCandidate)
                    .collect(Collectors.toList());

            String prompt = buildChatPrompt(request.getMessage(), allCandidates,null);

            // 4. Send to Ollama
             aiResponse = ollamaService.chat(prompt);
        }else{
            // 1. Save HR message
            ChatMessage hrMessage = ChatMessage.builder()
                    .user(currentUser)
                    .role(MessageRole.HR)
                    .message(request.getMessage())
                    .jobDescription(getJdIfPresent(request.getJdId()))
                    .build();
            chatMessageRepo.save(hrMessage);

            // 2. Get all candidates in job idfor context
            // 2. Get all candidates in job id for context
            List<Candidate> allCandidates = new ArrayList<>();
            Optional<JobDescription> job = jobDescriptionRepo.findById(request.getJdId());

            if (job.isEmpty()) {
                return ApiResponse.error("Job description not found");
            }
            job.get().getMatchResults().forEach(item -> {
                allCandidates.add(item.getCandidate());
            });

            // 3. Build prompt with all CV data as context
            String prompt = buildChatPrompt(request.getMessage(), allCandidates,job);

            // 4. Send to Ollama
             aiResponse = ollamaService.chat(prompt);
        }

        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return ApiResponse.error("AI failed to respond, please try again");
        }

        // 5. Save AI response
        ChatMessage aiMessage = ChatMessage.builder()
                .user(currentUser)
                .role(MessageRole.ASSISTANT)
                .message(aiResponse)
                .jobDescription(getJdIfPresent(request.getJdId()))
                .build();
        ChatMessage saved = chatMessageRepo.save(aiMessage);

        log.info("✅ Chat response saved for user: {}", currentUser.getUsername());

        return ApiResponse.success(toResponse(saved));
    }

    // ─────────────────────────────────────────
    // Get chat history for current user
    // ─────────────────────────────────────────
    public ApiResponse<List<ChatMessageResponse>> getChatHistory() {
        User currentUser = getCurrentUser();

        List<ChatMessage> messages = chatMessageRepo
                .findByUserIdOrderByCreatedAtAsc(currentUser.getId());

        if (messages.isEmpty()) {
            return ApiResponse.error("No chat history found");
        }

        List<ChatMessageResponse> response = messages.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    public ApiResponse<List<ChatMessageResponse>> getChatHistoryByJobId(long jobId) {
        User currentUser = getCurrentUser();

        List<ChatMessage> messages = chatMessageRepo
                .findByJobDescriptionIdOrderByCreatedAtAsc(jobId);

        if (messages.isEmpty()) {
            return ApiResponse.error("No chat history found");
        }

        List<ChatMessageResponse> response = messages.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(response);
    }

    // ─────────────────────────────────────────
    // Clear chat history for current user
    // ─────────────────────────────────────────
    public ApiResponse<String> clearHistory() {
        User currentUser = getCurrentUser();
        chatMessageRepo.deleteByUserId(currentUser.getId());
        log.info("✅ Chat history cleared for user: {}", currentUser.getUsername());
        return ApiResponse.success("Chat history cleared", null);
    }

    // ─────────────────────────────────────────
    // Build chat prompt with all CV context
    // ─────────────────────────────────────────
    private String buildChatPrompt(String message, List<Candidate> candidates, Optional<JobDescription> job) {

        String candidateContext = candidates != null && !candidates.isEmpty()
                ? candidates.stream()
                .map(c -> "ID:%d | %s | %s | %s | exp:%d yrs | skills:%s".formatted(
                        c.getId(),
                        c.getName(),
                        c.getDomain(),
                        c.getPosition(),
                        c.getExpYears() != null ? c.getExpYears() : 0,
                        c.getSkills() != null ? c.getSkills() : "[]"
                ))
                .collect(Collectors.joining("\n"))
                : "No candidate data available";

        if(job != null && job.isPresent()){
            return """
                    You are Maya, a smart HR assistant helping recruiters find the right candidates quickly.
                    
                    You have direct access to the candidate database and answer questions naturally — as if you already know the data, not as if you're "reading" or "searching" it in real time.
                    
                    Guidelines:
                    - Answer conversationally and confidently, like a knowledgeable colleague
                    - When filtering or listing candidates, present results clearly but naturally (e.g. "Here are the 5 candidates who match..." not "Based on the data provided...")
                    - Never say things like "based on the data below", "I found in the database", or "you provided"
                    - If no candidates match, say so simply: "No candidates match that criteria."
                    - Never invent information not in the data
                    - Keep answers concise unless detail is requested
                    - Answer straight to the point, if no match, answer no match.
                    - Never ask follow-up questions or offer further help (e.g. "Would you like me to...", "Do you want me to..."). Just answer what was asked and stop.
                    IMPORTANT: Do not invent, assume, or infer any information. If it is not in the candidate data, it does not exist.
                JOB DESCRIPTION
                %s
                                
                Candidate data::
                %s
                                
                HR QUESTION:
                %s
                """.formatted(job.get(),candidateContext, message);
        }

        return """
                You are Maya, a smart HR assistant helping recruiters find the right candidates quickly.
                    
                    You have direct access to the candidate database and answer questions naturally — as if you already know the data, not as if you're "reading" or "searching" it in real time.
                    
                    Guidelines:
                    - Answer conversationally and confidently, like a knowledgeable colleague
                    - When filtering or listing candidates, present results clearly but naturally (e.g. "Here are the 5 candidates who match..." not "Based on the data provided...")
                    - Never say things like "based on the data below", "I found in the database", or "you provided"
                    - If no candidates match, say so simply: "No candidates match that criteria."
                    - Never invent information not in the data
                    - Keep answers concise unless detail is requested
                    - Answer straight to the point, if no match, answer no match.
                    - Never ask follow-up questions or offer further help (e.g. "Would you like me to...", "Do you want me to..."). Just answer what was asked and stop.
                    IMPORTANT: Do not invent, assume, or infer any information. If it is not in the candidate data, it does not exist.
   
                CANDIDATE DATABASE:
                %s
                                
                HR QUESTION:
                %s
                """.formatted(candidateContext, message);
    }

    // ─────────────────────────────────────────
    // Get JD entity if ID provided
    // ─────────────────────────────────────────
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