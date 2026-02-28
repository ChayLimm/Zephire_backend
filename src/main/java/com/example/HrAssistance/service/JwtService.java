package com.example.HrAssistance.service;

import com.example.HrAssistance.model.User;

public interface JwtService {
    String generateToken(User user);
    String getRefreshToken(User user);
    String extractUsername(String token);
    String extractEmail(String token);
    String extractRole(String token);
    boolean isTokenValid(String token, User user);
    boolean validateToken(String token);
    boolean isTokenExpired(String token);
}