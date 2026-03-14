package com.example.HrAssistance.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Data
public class CVUploadRequest {

    private String name;
    private String email;
    private String phone;
    private String domain;
    private String position;
    private MultipartFile file;

    @JsonProperty("exp_years")
    private Integer expYears;

    @JsonProperty("cover_note")
    private String coverNote;
}