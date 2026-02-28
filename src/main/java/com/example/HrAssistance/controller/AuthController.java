package com.example.HrAssistance.controller;

import com.example.HrAssistance.model.User;
import com.example.HrAssistance.model.dto.request.AuthRequest;
import com.example.HrAssistance.model.dto.response.AuthResponse;
import com.example.HrAssistance.repositories.UserRepo;
import com.example.HrAssistance.service.AuthService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuthController {
    private final AuthService authService;

    @Autowired
    private final UserRepo userRepo;


    public AuthController(AuthService authService, UserRepo userRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Return proper error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "400");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            System.out.println("Calling register!");
            AuthResponse response = authService.register(request);
            System.out.println("Calling register done!");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "400");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/api/test")
    public ResponseEntity<List<User>> test() {
        final List<User> users = userRepo.findAll();
        return ResponseEntity.ok(users);
    }
}
