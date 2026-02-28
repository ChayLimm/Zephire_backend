package com.example.HrAssistance.model.dto.response;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("candidate_id")
    private Long candidateId;

    @JsonProperty("candidate_name")
    private String candidateName;

    @JsonProperty("candidate_email")
    private String candidateEmail;

    @JsonProperty("candidate_position")
    private String candidatePosition;

    @JsonProperty("match_score")
    private Integer matchScore;         // 0-100

    @JdbcTypeCode(SqlTypes.JSON)
    @JsonProperty("match_reasons")
    private List<String> matchReasons;  // ["3yr Flutter", "built 2 apps"]

    @JdbcTypeCode(SqlTypes.JSON)
    @JsonProperty("gaps")
    private List<String> gaps;          // ["no Dart cert"]

    @JsonProperty("matched_at")
    private LocalDateTime matchedAt;
}
