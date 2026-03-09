package com.example.HrAssistance.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PublicApplyRequest {

    private String name;
    private String email;
    private String phone;
    private String domain;
    private String position;

    @JsonProperty("exp_years")
    private Integer expYears;

    @JsonProperty("cover_note")
    private String coverNote;
}