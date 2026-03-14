package com.example.HrAssistance.controller;

import com.example.HrAssistance.model.dto.request.CVUploadRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.CandidateResponse;
import com.example.HrAssistance.service.impl.CandidateServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final CandidateServiceImpl candidateService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<CandidateResponse>> apply(
            @ModelAttribute CVUploadRequest request
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("name") String name,
//            @RequestParam("email") String email,
//            @RequestParam("phone") String phone,
//            @RequestParam("domain") String domain,
//            @RequestParam("position") String position,
//            @RequestParam("exp_years") Integer expYears
    ) {
//        PublicApplyRequest request = new PublicApplyRequest();
//        request.setName(name);
//        request.setEmail(email);
//        request.setPhone(phone);
//        request.setDomain(domain);
//        request.setPosition(position);
//        request.setExpYears(expYears);
        ApiResponse<CandidateResponse> response = candidateService.uploadCv(request);


        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);    }
}