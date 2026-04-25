package com.authshield.authshield.service;

import com.authshield.authshield.dto.auth.AuthResponse;
import com.authshield.authshield.dto.auth.ChangePasswordRequest;
import com.authshield.authshield.dto.auth.ForgotPasswordRequest;
import com.authshield.authshield.dto.auth.LoginRequest;
import com.authshield.authshield.dto.auth.RefreshTokenRequest;
import com.authshield.authshield.dto.auth.ResetPasswordRequest;
import com.authshield.authshield.dto.auth.SignupRequest;
import com.authshield.authshield.dto.auth.TokenValidationResponse;
import com.authshield.authshield.dto.user.UserResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthResponse signup(SignupRequest dto);

    AuthResponse login(LoginRequest dto);

    AuthResponse refreshToken(RefreshTokenRequest dto);

    void logout(String token);

    UserResponse getCurrentUser();

    TokenValidationResponse validateToken(HttpServletRequest request);

    void forgotPassword(ForgotPasswordRequest dto);

    void resetPassword(ResetPasswordRequest dto);

    void changePassword(ChangePasswordRequest dto);

}
