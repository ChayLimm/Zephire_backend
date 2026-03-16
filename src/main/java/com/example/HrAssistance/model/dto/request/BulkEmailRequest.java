package com.example.HrAssistance.model.dto.request;

import com.example.HrAssistance.model.dto.EmailPayload;
import lombok.Data;

import java.util.List;

@Data
public class BulkEmailRequest {
    private List<EmailPayload> emails;
}