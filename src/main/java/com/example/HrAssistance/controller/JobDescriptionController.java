package com.example.HrAssistance.controller;

import com.example.HrAssistance.model.dto.request.JobDescriptionRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.JobDescriptionResponse;
import com.example.HrAssistance.service.impl.JobDescriptionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jd")
@RequiredArgsConstructor
public class JobDescriptionController {

    private final JobDescriptionServiceImpl jobDescriptionService;

    // POST /api/jd/match
    @PostMapping("/match")
    public ResponseEntity<ApiResponse<JobDescriptionResponse>> matchCandidates(
            @RequestBody JobDescriptionRequest request) {

        ApiResponse<JobDescriptionResponse> response =
                jobDescriptionService.matchCandidates(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // GET /api/jd
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobDescriptionResponse>>> getAllJds() {
        return ResponseEntity.ok(jobDescriptionService.getAllJds());
    }

    // GET /api/jd/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDescriptionResponse>> getJdById(
            @PathVariable Long id) {

        ApiResponse<JobDescriptionResponse> response =
                jobDescriptionService.getJdById(id);

        if (!response.isSuccess()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    // DELETE /api/jd/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteJd(
            @PathVariable Long id) {

        ApiResponse<String> response = jobDescriptionService.deleteJd(id);

        if (!response.isSuccess()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}