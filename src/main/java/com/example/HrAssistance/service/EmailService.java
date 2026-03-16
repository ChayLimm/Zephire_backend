package com.example.HrAssistance.service;

import com.example.HrAssistance.model.Email;
import com.example.HrAssistance.model.dto.request.BulkEmailRequest;
import com.example.HrAssistance.model.dto.response.EmailResponse;

import java.util.List;

public interface EmailService {
     void sendBulk(BulkEmailRequest request);
     List<EmailResponse> getAll();
     EmailResponse updateStatus(Long id, String status);
     List<EmailResponse> getByCandidateId(Long candidateId);
}
