package com.example.HrAssistance.controller;

import com.example.HrAssistance.model.dto.request.CVUploadRequest;
import com.example.HrAssistance.model.dto.request.CandidateRequest;
import com.example.HrAssistance.model.dto.request.UploadCVRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.CandidateResponse;
import com.example.HrAssistance.service.impl.CandidateServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateServiceImpl candidateService;

    // POST /api/candidates/upload
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<CandidateResponse>> uploadCv(
            @ModelAttribute CVUploadRequest request

//        @RequestParam("file") MultipartFile file,
//        @RequestBody UploadCVRequest cvRequest,
//        @RequestParam("domain") String domain
    ){

        ApiResponse<CandidateResponse> response = candidateService.uploadCv(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }



    // GET /api/candidates
    @GetMapping
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getAllCandidates() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }

    // GET /api/candidates
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateCandidate(
            @PathVariable long id,
            @RequestBody CandidateRequest request
    ) {
        ApiResponse<CandidateResponse> response = candidateService.updateCandidate(id, request);
        return ResponseEntity.ok(response);
    }

    // GET /api/candidates/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidateById(
            @PathVariable Long id) {

        ApiResponse<CandidateResponse> response = candidateService.getCandidateById(id);

        if (!response.isSuccess()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    // GET /api/candidates/{id}/preview — stream PDF to browser
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewPdf(@PathVariable Long id) {
        try {
            String filePath = candidateService.getPdfPath(id);

            if (filePath == null) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DELETE /api/candidates/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCandidate(
            @PathVariable Long id) {

        ApiResponse<String> response = candidateService.deleteCandidate(id);

        if (!response.isSuccess()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    // GET /api/candidates/pending
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getPending() {
        return ResponseEntity.ok(candidateService.getPendingCandidates());
    }

    // PUT /api/candidates/{id}/approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<CandidateResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.approveCandidate(id));
    }

    // PUT /api/candidates/{id}/reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<CandidateResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.rejectCandidate(id));
    }
}