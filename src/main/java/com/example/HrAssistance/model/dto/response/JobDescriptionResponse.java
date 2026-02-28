package com.example.HrAssistance.model.dto.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDescriptionResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("field")
    private String field;

    @JsonProperty("position")
    private String position;

    @JsonProperty("required_skills")
    private List<String> requiredSkills;

    @JsonProperty("min_exp_years")
    private Integer minExpYears;

    @JsonProperty("description")
    private String description;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("created_by")
    private String createdBy;           // HR user name

    @JsonProperty("match_results")
    private List<MatchResultResponse> matchResults;  // results from LLM
}