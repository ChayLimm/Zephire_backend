package com.example.HrAssistance.model.dto.request;

import com.example.HrAssistance.enums.EmailType;
import com.example.HrAssistance.model.Candidate;
import com.example.HrAssistance.model.Email;
import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Data;
@Data
public class EmailRequest {

    private String toEmail;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    private EmailType type;

    @JsonProperty("candidate_id")
    private Candidate candidate;

    private String meetingDate;
    private String meetingTime;
    private String meetingLocation;

    private String status;

    public Email toEmail (){
        Email email = new Email();
        email.setBody(this.body);
        email.setType(this.type);
        email.setToEmail(this.toEmail);
        email.setSubject(this.subject);
        email.setCandidate(this.candidate);
        email.setMeetingTime(this.meetingTime);
        email.setMeetingDate(this.meetingDate);
        email.setMeetingLocation(this.meetingLocation);
        email.setStatus(this.status);
        return email;
    }
}