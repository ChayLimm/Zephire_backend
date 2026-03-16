package com.example.HrAssistance.model.dto;

import lombok.Data;

// EmailPayload.java
@Data
public class EmailPayload {
    private Long candidateId;
    private String email;
    private String subject;
    private String body;
    private String type;
    private String meetingDate;
    private String meetingTime;
    private String meetingLocation;
}