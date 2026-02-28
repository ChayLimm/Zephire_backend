package com.example.HrAssistance.service;

import com.example.HrAssistance.model.dto.request.AuthRequest;
import com.example.HrAssistance.model.dto.response.AuthResponse;
import org.springframework.stereotype.Service;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    AuthResponse register(AuthRequest request);
}
