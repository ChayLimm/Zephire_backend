package com.example.HrAssistance.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@Data
public class ChatRequest {
    private String message;

    @JsonProperty("jd_id")
    private Long jdId;

    @JsonProperty("candidate_id")
    private Long candidateId;  // ← add
}