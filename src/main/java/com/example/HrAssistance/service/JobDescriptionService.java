package com.example.HrAssistance.service;

import com.example.HrAssistance.model.dto.request.JobDescriptionRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.JobDescriptionResponse;

import java.util.List;

public interface JobDescriptionService {

    ApiResponse<JobDescriptionResponse> matchCandidates(JobDescriptionRequest request);

    ApiResponse<List<JobDescriptionResponse>> getAllJds();

    ApiResponse<JobDescriptionResponse> getJdById(Long id);

    ApiResponse<String> deleteJd(Long id);
}