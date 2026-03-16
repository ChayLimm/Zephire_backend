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

    @Override
    public void sendBulk(BulkEmailRequest request) {
        User currentUser = getCurrentUser();

        for (EmailPayload payload : request.getEmails()) {
            try {
                // Send email
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(payload.getEmail());
                message.setSubject(payload.getSubject());
                message.setText(payload.getBody());
                mailSender.send(message);

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
                log.info("Email sent to {}", payload.getEmail());

            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", payload.getEmail(), e.getMessage());

                // Save failed record
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