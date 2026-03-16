package com.example.HrAssistance.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CandidateRequest {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("domain")
    private String domain;

    @JsonProperty("email")
    private String email;

    @JsonProperty("exp_years")
    private Integer expYears;

    @JsonProperty("name")
    private String name;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("position")
    private String position;

}