package com.example.HrAssistance.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UploadCVRequest {

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("email")
    private String email;

}
