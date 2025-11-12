package com.pwr_zpi.reservespotapi.entities.users.service;

import com.pwr_zpi.reservespotapi.config.security.jwt.JwtService;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public User requireCurrentUser(HttpServletRequest request) {
        String token = extractToken(request);
        String email = jwtService.extractUsername(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for token"));
    }

    public Long requireCurrentUserId(HttpServletRequest request) {
        return requireCurrentUser(request).getId();
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        return header.substring(7);
    }
}

