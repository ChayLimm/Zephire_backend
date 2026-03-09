package com.example.HrAssistance.controller;

import com.example.HrAssistance.model.dto.request.PublicApplyRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.CandidateResponse;
import com.example.HrAssistance.service.impl.CandidateServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final CandidateServiceImpl candidateService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<CandidateResponse>> apply(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("domain") String domain,
            @RequestParam("position") String position,
            @RequestParam("exp_years") Integer expYears
    ) {
        PublicApplyRequest request = new PublicApplyRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPhone(phone);
        request.setDomain(domain);
        request.setPosition(position);
        request.setExpYears(expYears);

        return ResponseEntity.ok(candidateService.publicApply(file, request));
    }
}