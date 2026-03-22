package com.example.HrAssistance.service.impl;

import com.example.HrAssistance.enums.EmailType;
import com.example.HrAssistance.model.Candidate;
import com.example.HrAssistance.model.Email;
import com.example.HrAssistance.model.User;
import com.example.HrAssistance.model.dto.EmailPayload;
import com.example.HrAssistance.model.dto.request.BulkEmailRequest;
import com.example.HrAssistance.model.dto.response.EmailResponse;
import com.example.HrAssistance.repositories.CandidateRepo;
import com.example.HrAssistance.repositories.EmailRepo;
import com.example.HrAssistance.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailRepo emailRepo;
    private final CandidateRepo candidateRepo;

    @Value("${brevo.token}")
    private String brevoApiKey;

    @Override
    public void sendBulk(BulkEmailRequest request) {
        User currentUser = getCurrentUser();

        for (EmailPayload payload : request.getEmails()) {
            try {
                // Build Brevo API request body
                Map<String, Object> emailBody = new HashMap<>();
                emailBody.put("sender", Map.of(
                        "name", "Sok.HR",
                        "email", "chengchhaylim@gmail.com"
                ));
                emailBody.put("to", List.of(Map.of(
                        "email", payload.getEmail()
                )));
                emailBody.put("subject", payload.getSubject());
                emailBody.put("htmlContent", payload.getBody());

                // Send via Brevo API
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey); // inject from config

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(emailBody, headers);

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.postForEntity(
                        "https://api.brevo.com/v3/smtp/email",
                        entity,
                        String.class
                );

                // Save to DB
                Candidate candidate = candidateRepo.findById(payload.getCandidateId())
                        .orElse(null);

                Email email = Email.builder()
                        .toEmail(payload.getEmail())
                        .subject(payload.getSubject())
                        .body(payload.getBody())
                        .type(EmailType.valueOf(payload.getType()))
                        .meetingDate(payload.getMeetingDate())
                        .meetingTime(payload.getMeetingTime())
                        .meetingLocation(payload.getMeetingLocation())
                        .candidate(candidate)
                        .status("SENT")
                        .sentAt(LocalDateTime.now())
                        .sentBy(currentUser)
                        .build();

                emailRepo.save(email);
                log.info("Email sent to {} via Brevo", payload.getEmail());

            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", payload.getEmail(), e.getMessage());

                Email failed = Email.builder()
                        .toEmail(payload.getEmail())
                        .subject(payload.getSubject())
                        .body(payload.getBody())
                        .type(EmailType.valueOf(payload.getType()))
                        .candidate(candidateRepo.findById(payload.getCandidateId()).orElse(null))
                        .status("FAILED")
                        .sentAt(LocalDateTime.now())
                        .sentBy(currentUser)
                        .build();

                emailRepo.save(failed);
            }
        }
    }
//    @Override
//    public List<Email> getEmailsByCandidate(Long candidateId) {
//        return emailRepo.findByCandidateIdOrderBySentAtDesc(candidateId);
//    }

    @Override
    public List<EmailResponse> getAll() {
        return emailRepo.findAllByOrderBySentAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmailResponse updateStatus(Long id, String status) {
        Email email = emailRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found"));
        email.setStatus(status);
        return toResponse(emailRepo.save(email));
    }

    @Override
    public List<EmailResponse> getByCandidateId(Long candidateId) {
        return emailRepo.findByCandidateIdOrderBySentAtDesc(candidateId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EmailResponse toResponse(Email email) {
        return email.toResponse();
    }


    private User getCurrentUser() {
        return (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}