package com.example.HrAssistance.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CandidateRequest {

    private Long id;

    private String domain;
    private String email;

    @JsonProperty("exp_years")
    private Integer expYears;

    private String name;
    private String phone;
    private String position;

}