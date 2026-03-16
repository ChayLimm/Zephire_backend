package com.example.HrAssistance.controller;

import com.example.HrAssistance.model.Email;
import com.example.HrAssistance.model.dto.request.BulkEmailRequest;
import com.example.HrAssistance.model.dto.request.UpdateStatusRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.EmailResponse;
import com.example.HrAssistance.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-bulk")
    public ResponseEntity<ApiResponse<String>> sendBulk(
            @RequestBody BulkEmailRequest request) {
        emailService.sendBulk(request);
        return ResponseEntity.ok(ApiResponse.success("Emails sent successfully", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmailResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(emailService.getAll()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EmailResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(emailService.updateStatus(id, request.getStatus())));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<ApiResponse<List<EmailResponse>>> getByCandidateId(
            @PathVariable Long candidateId) {
        return ResponseEntity.ok(ApiResponse.success(emailService.getByCandidateId(candidateId)));
    }
}