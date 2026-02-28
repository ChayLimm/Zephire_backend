package com.example.HrAssistance.service;

import com.example.HrAssistance.model.dto.request.ChatRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.ChatMessageResponse;

import java.util.List;

public interface ChatService {

    ApiResponse<ChatMessageResponse> sendMessage(ChatRequest request);

    ApiResponse<List<ChatMessageResponse>> getChatHistory();

    ApiResponse<List<ChatMessageResponse>> getChatHistoryByJobId(long jobId);

    ApiResponse<String> clearHistory();
}