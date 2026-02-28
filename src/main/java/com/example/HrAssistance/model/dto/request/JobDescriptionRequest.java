package com.example.HrAssistance.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class JobDescriptionRequest {

    @JsonProperty("title")
    private String title;               // "Senior Flutter Developer"

    @JsonProperty("field")
    private String field;               // "tech" / "sales" / "marketing"

    @JsonProperty("position")
    private String position;            // "Flutter Developer"

    @JsonProperty("required_skills")
    private List<String> requiredSkills; // ["Flutter", "Dart", "REST API"]

    @JsonProperty("min_exp_years")
    private Integer minExpYears;        // 2

    @JsonProperty("description")
    private String description;         // full JD text
}