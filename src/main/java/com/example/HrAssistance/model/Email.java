package com.example.HrAssistance.model;




import com.example.HrAssistance.enums.EmailType;
import com.example.HrAssistance.enums.MessageRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "emails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String toEmail;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @JsonProperty("type")
    private EmailType type;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    @JsonProperty("candidate_id")
    private Candidate candidate;

    private String meetingDate;
    private String meetingTime;
    private String meetingLocation;
    private String status;         // SENT, FAILED

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_by")
    private User sentBy;

//    public EmailResponse toResponse(){
//
//    }
}