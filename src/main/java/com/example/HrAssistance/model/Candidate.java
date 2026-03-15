package com.example.HrAssistance.model;

import com.example.HrAssistance.enums.CandidateSource;
import com.example.HrAssistance.enums.CandidateStatus;
import com.example.HrAssistance.model.dto.request.CandidateRequest;
import com.example.HrAssistance.model.dto.response.CandidateResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String domain;
    private String position;

    @Column(name = "exp_years")
    private Integer expYears;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String skills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String stack;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String cvRaw;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cv_json", columnDefinition = "JSON")
    private String cvJson;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    @Builder.Default
    private CandidateSource source = CandidateSource.HR_UPLOADED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private CandidateStatus status = CandidateStatus.APPROVED;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    @JsonIgnore
    private User uploadedBy;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MatchResult> matchResults;

    public CandidateResponse toResponse() {
        return CandidateResponse.builder()
                .id(this.getId())
                .name(this.getName())
                .email(this.getEmail())
                .phone(this.getPhone())
                .domain(this.getDomain())
                .fileName(this.getFileName())
                .cvJson(this.getCvRaw())
                .cvJson(this.getCvJson())
                .position(this.getPosition())
                .expYears(this.getExpYears())
                .fileName(this.getFileName())
                .cvJson(this.getCvJson())
                .uploadedAt(this.getUploadedAt())
                .source(this.getSource())      // ← add
                .status(this.getStatus())      // ← add
                .uploadedBy(this.getUploadedBy() != null
                        ? this.getUploadedBy().getUsername()
                        : null)
                .build();
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

}