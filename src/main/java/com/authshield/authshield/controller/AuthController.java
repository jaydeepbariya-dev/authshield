package com.authshield.authshield.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.authshield.authshield.dto.auth.AuthResponse;
import com.authshield.authshield.dto.auth.ChangePasswordRequest;
import com.authshield.authshield.dto.auth.ForgotPasswordRequest;
import com.authshield.authshield.dto.auth.LoginRequest;
import com.authshield.authshield.dto.auth.RefreshTokenRequest;
import com.authshield.authshield.dto.auth.ResetPasswordRequest;
import com.authshield.authshield.dto.auth.SignupRequest;
import com.authshield.authshield.dto.auth.TokenValidationResponse;
import com.authshield.authshield.dto.user.UserResponse;
import com.authshield.authshield.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest dto) {
        AuthResponse response = authService.signup(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest dto) {
        AuthResponse response = authService.login(dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest dto) {
        AuthResponse response = authService.refreshToken(dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(HttpServletRequest request) {

        TokenValidationResponse response = authService.validateToken(request);

        if (!response.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest dto) {
        authService.forgotPassword(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest dto) {
        authService.resetPassword(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest dto) {
        authService.changePassword(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
