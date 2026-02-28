package com.example.HrAssistance.model.dto.response;

import com.example.HrAssistance.model.Candidate;
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
public class CandidateResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("position")
    private String position;

    @JsonProperty("exp_years")
    private Integer expYears;

    @JsonProperty("skills")
    private List<String> skills;

    @JsonProperty("stack")
    private List<String> stack;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("cv_json")
    private String cvJson;             // full compressed JSON from LLM

    @JsonProperty("uploaded_at")
    private LocalDateTime uploadedAt;

    @JsonProperty("uploaded_by")
    private String uploadedBy;         // HR user name

    public Candidate toCandidate(){
        Candidate candidate = new Candidate();
        candidate.setId(this.id);
        candidate.setName(this.name);
        candidate.setEmail(this.email);
        candidate.setPhone(this.phone);
        candidate.setDomain(this.domain);
        candidate.setPosition(this.position);
        candidate.setExpYears(this.expYears);

        // Convert List<String> to JSON string for skills and stack
        if (this.skills != null) {
            candidate.setSkills(convertListToJsonString(this.skills));
        }

        if (this.stack != null) {
            candidate.setStack(convertListToJsonString(this.stack));
        }

        candidate.setFileName(this.fileName);
        candidate.setCvJson(this.cvJson);
        candidate.setUploadedAt(this.uploadedAt);

        return candidate;
    }

    private String convertListToJsonString(List<String> list) {
        if (list == null) return null;
        // Simple JSON array conversion - you might want to use a proper JSON library
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

}