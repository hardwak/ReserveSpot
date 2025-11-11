package com.pwr_zpi.reservespotapi.controller.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.pwr_zpi.reservespotapi.config.security.jwt.JwtService;
import com.pwr_zpi.reservespotapi.controller.auth.dto.*;
import com.pwr_zpi.reservespotapi.entities.users.AuthProvider;
import com.pwr_zpi.reservespotapi.entities.users.Role;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import com.pwr_zpi.reservespotapi.entities.users.service.CurrentUserService;
import com.pwr_zpi.reservespotapi.service.GoogleTokenVerifierService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifierService googleTokenVerifier;
    private final CurrentUserService currentUserService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (AuthenticationException e) {
            var userOpt = userRepository.findByEmail(request.email());

            if (userOpt.isPresent() && userOpt.get().getPassword() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Login with Google to proceed"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid email or password"));
            }
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email is already taken"));
        }

        Role role;
        try {
            role = Role.valueOf(request.role().toUpperCase());
            if (role == Role.ADMIN)
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid role name"));
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid role name"));
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .provider(AuthProvider.LOCAL)
                .build();

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleTokenRequest request) {
        try {
            GoogleIdToken.Payload payload = googleTokenVerifier.verifyToken(request.googleToken());
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleUserId = payload.getSubject();

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> registerNewGoogleUser(email, name, googleUserId));

            if (user.getProvider() == AuthProvider.LOCAL) {
                user.setProvider(AuthProvider.GOOGLE);
                user.setOauthProviderId(googleUserId);
                userRepository.save(user);
            }

            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid Google token: " + e.getMessage()));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(new AuthResponse("Hello, " + auth.getName() + "! Your roles are: " + auth.getAuthorities()));
    }

    private User registerNewGoogleUser(String email, String name, String googleUserId) {
        User user = User.builder()
                .name(name)
                .email(email)
                .oauthProviderId(googleUserId)
                .provider(AuthProvider.GOOGLE)
                .role(Role.CLIENT)
                .build();
        return userRepository.save(user);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(HttpServletRequest request,
                                            @Valid @RequestBody ChangePasswordRequest changeRequest) {
        User user = currentUserService.requireCurrentUser(request);

        if (user.getProvider() == AuthProvider.GOOGLE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Password change is not available for Google accounts"));
        }

        if (!passwordEncoder.matches(changeRequest.currentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Current password is incorrect"));
        }

        if (passwordEncoder.matches(changeRequest.newPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("New password must be different from the current password"));
        }

        user.setPasswordHash(passwordEncoder.encode(changeRequest.newPassword()));
        userRepository.save(user);

        return ResponseEntity.noContent().build();
    }
}