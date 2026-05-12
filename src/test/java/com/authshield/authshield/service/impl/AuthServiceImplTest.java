package com.authshield.authshield.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.authshield.authshield.dto.auth.ChangePasswordRequest;
import com.authshield.authshield.dto.auth.ForgotPasswordRequest;
import com.authshield.authshield.dto.auth.LoginRequest;
import com.authshield.authshield.dto.auth.RefreshTokenRequest;
import com.authshield.authshield.dto.auth.ResetPasswordRequest;
import com.authshield.authshield.dto.auth.SignupRequest;
import com.authshield.authshield.dto.auth.TokenValidationResponse;
import com.authshield.authshield.dto.user.UserResponse;
import com.authshield.authshield.dto.auth.AuthResponse;
import com.authshield.authshield.entity.RefreshToken;
import com.authshield.authshield.entity.Role;
import com.authshield.authshield.entity.User;
import com.authshield.authshield.repository.RefreshTokenRepository;
import com.authshield.authshield.repository.RoleRepository;
import com.authshield.authshield.repository.UserRepository;
import com.authshield.authshield.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role testRole;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ROLE_USER");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setEnabled(true);
        testUser.setRoles(Set.of(testRole));

        testRefreshToken = new RefreshToken();
        testRefreshToken.setId(1L);
        testRefreshToken.setToken("refreshToken");
        testRefreshToken.setUser(testUser);
        testRefreshToken.setRevoked(false);
        testRefreshToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60));
    }

    @Test
    @DisplayName("Signup - Success with valid data")
    void testSignup_Success() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("New User");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");

        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateAccessToken(any(UUID.class), anyString(), anySet())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(any(UUID.class))).thenReturn("refreshToken");

        AuthResponse response = authService.signup(signupRequest);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals(testUser.getEmail(), response.getUser().getEmail());

        verify(userRepository, times(1)).existsByEmail(signupRequest.getEmail());
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(userRepository, times(1)).save(any(User.class));
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Signup - Fails when user already exists")
    void testSignup_UserAlreadyExists() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("existing@example.com");
        signupRequest.setPassword("password123");

        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.signup(signupRequest));
        verify(userRepository, times(1)).existsByEmail(signupRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Signup - Fails when role not found")
    void testSignup_RoleNotFound() {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password123");

        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.signup(signupRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login - Success with valid credentials")
    void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateAccessToken(any(UUID.class), anyString(), anySet())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(any(UUID.class))).thenReturn("refreshToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), testUser.getPassword());
    }

    @Test
    @DisplayName("Login - Fails with invalid credentials")
    void testLogin_InvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Login - Fails when user not found")
    void testLogin_UserNotFound() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("RefreshToken - Success with valid token")
    void testRefreshToken_Success() {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest();
        tokenRequest.setRefreshToken("validRefreshToken");

        when(jwtUtil.isRefreshToken("validRefreshToken")).thenReturn(true);
        when(refreshTokenRepository.findByToken("validRefreshToken")).thenReturn(testRefreshToken);
        when(jwtUtil.generateAccessToken(any(UUID.class), anyString(), anySet())).thenReturn("newAccessToken");
        when(jwtUtil.generateRefreshToken(any(UUID.class))).thenReturn("newRefreshToken");

        AuthResponse response = authService.refreshToken(tokenRequest);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("RefreshToken - Fails with blank token")
    void testRefreshToken_BlankToken() {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest();
        tokenRequest.setRefreshToken("   ");

        assertThrows(RuntimeException.class, () -> authService.refreshToken(tokenRequest));
    }

    @Test
    @DisplayName("RefreshToken - Fails with null token")
    void testRefreshToken_NullToken() {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest();
        tokenRequest.setRefreshToken(null);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(tokenRequest));
    }

    @Test
    @DisplayName("RefreshToken - Fails with revoked token")
    void testRefreshToken_RevokedToken() {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest();
        tokenRequest.setRefreshToken("revokedToken");

        testRefreshToken.setRevoked(true);

        when(jwtUtil.isRefreshToken("revokedToken")).thenReturn(true);
        when(refreshTokenRepository.findByToken("revokedToken")).thenReturn(testRefreshToken);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(tokenRequest));
    }

    @Test
    @DisplayName("RefreshToken - Fails with expired token")
    void testRefreshToken_ExpiredToken() {
        RefreshTokenRequest tokenRequest = new RefreshTokenRequest();
        tokenRequest.setRefreshToken("expiredToken");

        testRefreshToken.setExpiryDate(Instant.now().minusSeconds(1000));

        when(jwtUtil.isRefreshToken("expiredToken")).thenReturn(true);
        when(refreshTokenRepository.findByToken("expiredToken")).thenReturn(testRefreshToken);

        assertThrows(RuntimeException.class, () -> authService.refreshToken(tokenRequest));
    }

    @Test
    @DisplayName("Logout - Success with valid token")
    void testLogout_Success() {
        String token = "validToken";

        when(refreshTokenRepository.findByToken(token)).thenReturn(testRefreshToken);

        authService.logout(token);

        assertTrue(testRefreshToken.isRevoked());
        verify(refreshTokenRepository, times(1)).save(testRefreshToken);
    }

    @Test
    @DisplayName("Logout - Succeeds silently when token not found")
    void testLogout_TokenNotFound() {
        String token = "invalidToken";

        when(refreshTokenRepository.findByToken(token)).thenReturn(null);

        authService.logout(token);

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("GetCurrentUser - Success")
    void testGetCurrentUser_Success() {
        String email = "test@example.com";

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        UserResponse response = authService.getCurrentUser();

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getEmail(), response.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("GetCurrentUser - Fails when user not found")
    void testGetCurrentUser_UserNotFound() {
        String email = "nonexistent@example.com";

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.getCurrentUser());
    }

    @Test
    @DisplayName("ValidateToken - Fails with missing Authorization header")
    void testValidateToken_MissingHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getHeader("Authorization")).thenReturn(null);

        TokenValidationResponse response = authService.validateToken(request);

        assertNotNull(response);
        assertFalse(response.isValid());
    }

    @Test
    @DisplayName("ValidateToken - Fails with invalid Bearer format")
    void testValidateToken_InvalidBearerFormat() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getHeader("Authorization")).thenReturn("InvalidFormat");

        TokenValidationResponse response = authService.validateToken(request);

        assertNotNull(response);
        assertFalse(response.isValid());
    }

    @Test
    @DisplayName("ValidateToken - Fails with invalid token")
    void testValidateToken_InvalidToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "invalidToken";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        TokenValidationResponse response = authService.validateToken(request);

        assertNotNull(response);
        assertFalse(response.isValid());
    }

    @Test
    @DisplayName("ForgotPassword - Success")
    void testForgotPassword_Success() {
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setEmail("test@example.com");

        when(userRepository.findByEmail(forgotRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generatePasswordResetToken(testUser.getEmail())).thenReturn("resetToken");

        authService.forgotPassword(forgotRequest);

        verify(userRepository, times(1)).findByEmail(forgotRequest.getEmail());
        verify(jwtUtil, times(1)).generatePasswordResetToken(testUser.getEmail());
    }

    @Test
    @DisplayName("ForgotPassword - Throws when user not found")
    void testForgotPassword_UserNotFound() {
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest();
        forgotRequest.setEmail("nonexistent@example.com");

        when(userRepository.findByEmail(forgotRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> authService.forgotPassword(forgotRequest));
    }

    @Test
    @DisplayName("ResetPassword - Success with valid token")
    void testResetPassword_Success() {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken("validResetToken");
        resetRequest.setNewPassword("newPassword123");

        when(jwtUtil.validateToken("validResetToken")).thenReturn(true);
        when(jwtUtil.extractEmail("validResetToken")).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        authService.resetPassword(resetRequest);

        assertEquals("encodedNewPassword", testUser.getPassword());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("ResetPassword - Fails with invalid token")
    void testResetPassword_InvalidToken() {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setToken("invalidToken");
        resetRequest.setNewPassword("newPassword123");

        when(jwtUtil.validateToken("invalidToken")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.resetPassword(resetRequest));
    }

    @Test
    @DisplayName("ChangePassword - Success with correct old password")
    void testChangePassword_Success() {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setOldPassword("oldPassword");
        changeRequest.setNewPassword("newPassword123");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        authService.changePassword(changeRequest);

        assertEquals("encodedNewPassword", testUser.getPassword());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("ChangePassword - Fails with wrong old password")
    void testChangePassword_WrongOldPassword() {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest();
        changeRequest.setOldPassword("wrongPassword");
        changeRequest.setNewPassword("newPassword123");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.changePassword(changeRequest));
        verify(userRepository, never()).save(any(User.class));
    }
}
