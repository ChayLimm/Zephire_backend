package com.example.HrAssistance.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChatRequest {

    @JsonProperty("message")
    private String message;             // HR's question or filter request

    @JsonProperty("jd_id")
    private Long jdId;                  // optional - link chat to specific JD
}