package com.example.HrAssistance.security;

import com.example.HrAssistance.model.User;
import com.example.HrAssistance.repositories.UserRepo;
import com.example.HrAssistance.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepo userRepo;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService, UserRepo userRepo) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepo = userRepo;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/register");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String email;

        // Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);

        try {
            // Extract email from JWT
            email = jwtService.extractUsername(jwt);

            // If email exists and no authentication in SecurityContext
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user from database
                Optional<User> userOptional = userRepo.findByEmail(email);

                if (userOptional.isEmpty()) {
                    // User not found in database
                    sendErrorResponse(response, "User not found", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                User user = userOptional.get();

                // Validate token
                if (!jwtService.isTokenValid(jwt, user)) {
                    // Token is invalid or expired
                    sendErrorResponse(response, "Invalid or expired token", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // Create authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

                // Set details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Update SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Token is invalid or expired
            logger.error("Cannot set user authentication: {}", e);
            sendErrorResponse(response, "Authentication failed: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\", \"status\": " + status + "}");
    }
}