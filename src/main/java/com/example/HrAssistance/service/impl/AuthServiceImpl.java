package com.example.HrAssistance.service.impl;

import com.example.HrAssistance.enums.Role;
import com.example.HrAssistance.model.User;
import com.example.HrAssistance.model.dto.request.AuthRequest;
import com.example.HrAssistance.model.dto.response.AuthResponse;
import com.example.HrAssistance.repositories.UserRepo;
import com.example.HrAssistance.service.AuthService;
import com.example.HrAssistance.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepo userRepo,
                           PasswordEncoder passwordEncoder, JwtService jwtService, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        try {
            final Authentication authenticated = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            if (authenticated.isAuthenticated()) {
                User user = userRepo.findByEmail(request.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
                final String token = jwtService.generateToken(user);
                final String refreshToken = jwtService.getRefreshToken(user);

                AuthResponse response = new AuthResponse();
                response.setAccessToken(token);
                response.setRefreshToken(refreshToken);

                SecurityContextHolder.getContext().setAuthentication(authenticated);

                return response;
            }
            throw new RuntimeException("Account not authenticated");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AuthResponse register(AuthRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encode password
        user.setRole(Role.ADMIN); // Set default role
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setRole(request.getRole());
//        user.setDepartment(request.getDepartment());
        userRepo.save(user);

        final String token = jwtService.generateToken(user);
        final String refreshToken = jwtService.getRefreshToken(user);
        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setRefreshToken(refreshToken);
        // after register, user will use the access token to make request, so no need auto login
//        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                userDetails,
//                null,
//                userDetails.getAuthorities()
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);

        response.setAccessToken("Registration successful");
        return response;
    }

}
