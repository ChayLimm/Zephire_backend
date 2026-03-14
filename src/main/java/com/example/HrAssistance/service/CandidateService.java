package com.example.HrAssistance.service;

import com.example.HrAssistance.model.Candidate;
import com.example.HrAssistance.model.dto.request.CVUploadRequest;
import com.example.HrAssistance.model.dto.request.CandidateRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.CandidateResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateService {

    ApiResponse<CandidateResponse> uploadCv(CVUploadRequest request);

    ApiResponse<CandidateResponse> updateCandidate(Long id, CandidateRequest request);

    ApiResponse<List<CandidateResponse>> getAllCandidates();

    ApiResponse<CandidateResponse> getCandidateById(Long id);

    ApiResponse<String> deleteCandidate(Long id);

    String getPdfPath(Long id);

    List<Candidate> getFilteredCandidates(String domain, Integer minExpYears);
}