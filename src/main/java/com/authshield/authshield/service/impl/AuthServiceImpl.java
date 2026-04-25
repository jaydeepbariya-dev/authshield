package com.authshield.authshield.service.impl;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.authshield.authshield.dto.auth.*;
import com.authshield.authshield.dto.user.UserResponse;
import com.authshield.authshield.entity.RefreshToken;
import com.authshield.authshield.entity.Role;
import com.authshield.authshield.entity.User;
import com.authshield.authshield.repository.RefreshTokenRepository;
import com.authshield.authshield.repository.RoleRepository;
import com.authshield.authshield.repository.UserRepository;
import com.authshield.authshield.security.JwtUtil;
import com.authshield.authshield.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse signup(SignupRequest dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("User already exists");
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(true);
        user.setRoles(Set.of(role));

        User saved = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(
                saved.getId(),
                saved.getEmail(),
                saved.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

        String refreshToken = jwtUtil.generateRefreshToken(saved.getId());

        saveRefreshToken(saved, refreshToken);

        return buildAuthResponse(saved, accessToken, refreshToken);
    }

    @Override
    public AuthResponse login(LoginRequest dto) {

        User user = userRepository.findByEmail(dto.getEmail()).get();

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        saveRefreshToken(user, refreshToken);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest dto) {

        String oldToken = dto.getRefreshToken();

        if (oldToken == null || oldToken.isBlank()) {
            throw new RuntimeException("Refresh token required");
        }

        if (!jwtUtil.isRefreshToken(oldToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        RefreshToken stored = refreshTokenRepository.findByToken(oldToken);

        if (stored == null || stored.isRevoked() || stored.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token invalid/revoked");
        }

        User user = stored.getUser();

        // rotate
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        saveRefreshToken(user, newRefreshToken);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String token) {

        RefreshToken stored = refreshTokenRepository.findByToken(token);

        if (stored != null) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
        }
    }

    @Override
    public UserResponse getCurrentUser() {

        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("user not found"));

        return mapToUserResponse(user);
    }

    @Override
    public TokenValidationResponse validateToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        TokenValidationResponse res = new TokenValidationResponse();

        // 1. Validate header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setValid(false);
            return res;
        }

        // 2. Extract token
        String token = authHeader.substring(7);

        // 3. Validate token
        boolean valid = jwtUtil.validateToken(token);
        res.setValid(valid);

        if (!valid) {
            return res;
        }

        // 4. Extract everything from SAME token
        res.setEmail(jwtUtil.extractEmail(token));
        res.setUserId(jwtUtil.extractUserId(token));
        res.setRoles(jwtUtil.extractRoles(token));

        return res;
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest dto) {

        User user = userRepository.findByEmail(dto.getEmail()).get();

        if (user == null)
            return;

        String token = jwtUtil.generatePasswordResetToken(user.getEmail());

        // TODO: send email
        System.out.println("Reset Token: " + token);
    }

    @Override
    public void resetPassword(ResetPasswordRequest dto) {

        if (!jwtUtil.validateToken(dto.getToken())) {
            throw new RuntimeException("Invalid reset token");
        }

        String email = jwtUtil.extractEmail(dto.getToken());

        User user = userRepository.findByEmail(email).get();

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest dto) {

        String email = getCurrentUserEmail();

        User user = userRepository.findByEmail(email).get();

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong old password");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setUser(user);
        rt.setRevoked(false);
        rt.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        refreshTokenRepository.save(rt);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse res = new AuthResponse();
        res.setAccessToken(accessToken);
        res.setRefreshToken(refreshToken);
        res.setUser(mapToUserResponse(user));
        return res;
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getCreatedAt());
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}