package com.example.HrAssistance.controller;

import com.example.HrAssistance.model.dto.request.ChatRequest;
import com.example.HrAssistance.model.dto.response.ApiResponse;
import com.example.HrAssistance.model.dto.response.ChatMessageResponse;
import com.example.HrAssistance.service.impl.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatServiceImpl chatService;

    // POST /api/chat
    @PostMapping
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @RequestBody ChatRequest request) {

        ApiResponse<ChatMessageResponse> response = chatService.sendMessage(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    // GET /api/chat/history
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory() {
        return ResponseEntity.ok(chatService.getChatHistory());
    }


    // GET /api/chat/history
    @GetMapping("/history/{id}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistoryByJobId( @PathVariable int id) {
        return ResponseEntity.ok(chatService.getChatHistoryByJobId(id));
    }


    // DELETE /api/chat/history
    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<String>> clearHistory() {
        return ResponseEntity.ok(chatService.clearHistory());
    }
}