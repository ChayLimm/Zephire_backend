package com.example.HrAssistance.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailResponse {
    private Long id;
    private String toEmail;
    private String subject;
    private String body;
    private String type;
    private String status;
    private String sentAt;
    private String meetingDate;
    private String meetingTime;
    private String meetingLocation;
    private Long candidateId;
    private String candidateName;
}