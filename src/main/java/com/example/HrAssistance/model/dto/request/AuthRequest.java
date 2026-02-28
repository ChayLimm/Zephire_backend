package com.example.HrAssistance.model.dto.request;

import com.example.HrAssistance.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AuthRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
//
//    @JsonProperty("department")
//    private Department department;

    @JsonProperty("role")
    private Role role;


}
