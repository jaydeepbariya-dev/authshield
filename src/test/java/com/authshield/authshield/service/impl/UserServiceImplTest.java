package com.authshield.authshield.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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

import com.authshield.authshield.dto.user.UpdateUserRequest;
import com.authshield.authshield.dto.user.UserResponse;
import com.authshield.authshield.entity.Role;
import com.authshield.authshield.entity.User;
import com.authshield.authshield.repository.RefreshTokenRepository;
import com.authshield.authshield.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User testUser2;
    private Role testRole;

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
        testUser.setCreatedAt(Instant.now());

        testUser2 = new User();
        testUser2.setId(UUID.randomUUID());
        testUser2.setName("Another User");
        testUser2.setEmail("another@example.com");
        testUser2.setPassword("encodedPassword");
        testUser2.setEnabled(true);
        testUser2.setRoles(Set.of(testRole));
        testUser2.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("GetAllUsers - Returns list of all users")
    void testGetAllUsers_Success() {
        List<User> userList = Arrays.asList(testUser, testUser2);

        when(userRepository.findAll()).thenReturn(userList);

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Test User", responses.get(0).getName());
        assertEquals("Another User", responses.get(1).getName());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("GetAllUsers - Returns empty list when no users exist")
    void testGetAllUsers_Empty() {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("GetUserById - Success with valid user id")
    void testGetUserById_Success() {
        UUID userId = testUser.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertTrue(response.isEnabled());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("GetUserById - Fails when user not found")
    void testGetUserById_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUserById(userId));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("UpdateUser - Success with valid id and update request")
    void testUpdateUser_Success() {
        UUID userId = testUser.getId();
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEnabled(false);

        testUser.setName("Updated Name");
        testUser.setEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Name", response.getName());
        assertFalse(response.isEnabled());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("UpdateUser - Success with only name update")
    void testUpdateUser_NameOnly() {
        UUID userId = testUser.getId();
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("New Name");
        updateRequest.setEnabled(null);

        testUser.setName("New Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals("New Name", response.getName());
        assertTrue(response.isEnabled());

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("UpdateUser - Success with only enabled flag update")
    void testUpdateUser_EnabledOnly() {
        UUID userId = testUser.getId();
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName(null);
        updateRequest.setEnabled(false);

        testUser.setEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals("Test User", response.getName());
        assertFalse(response.isEnabled());

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("UpdateUser - Succeeds with blank name (no update)")
    void testUpdateUser_BlankName() {
        UUID userId = testUser.getId();
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("   ");
        updateRequest.setEnabled(false);

        testUser.setEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals("Test User", response.getName());
        assertFalse(response.isEnabled());

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("UpdateUser - Fails when user not found")
    void testUpdateUser_UserNotFound() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("New Name");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(userId, updateRequest));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("DeleteUser - Success with valid user id")
    void testDeleteUser_Success() {
        UUID userId = testUser.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("DeleteUser - Fails when user not found")
    void testDeleteUser_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(userId));

        assertEquals("User not found", exception.getMessage());
        verify(refreshTokenRepository, never()).deleteByUserId(any(UUID.class));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("DeleteUser - Cleans up refresh tokens before deletion")
    void testDeleteUser_CleansUpRefreshTokens() {
        UUID userId = testUser.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        userService.deleteUser(userId);

        verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("UpdateUser - Validates null update request values are ignored")
    void testUpdateUser_NullValues() {
        UUID userId = testUser.getId();
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName(null);
        updateRequest.setEnabled(null);

        String originalName = testUser.getName();
        boolean originalEnabled = testUser.isEnabled();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals(originalName, response.getName());
        assertEquals(originalEnabled, response.isEnabled());

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("GetUserById - Response contains correct role information")
    void testGetUserById_ValidateRoleMapping() {
        UUID userId = testUser.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);
        assertNotNull(response.getRoles());
        assertTrue(response.getRoles().contains("ROLE_USER"));
        assertEquals(1, response.getRoles().size());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("GetAllUsers - All users mapped correctly with roles")
    void testGetAllUsers_ValidateRoleMapping() {
        List<User> userList = Arrays.asList(testUser, testUser2);

        when(userRepository.findAll()).thenReturn(userList);

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        responses.forEach(response -> {
            assertNotNull(response.getRoles());
            assertTrue(response.getRoles().contains("ROLE_USER"));
        });

        verify(userRepository, times(1)).findAll();
    }
}
