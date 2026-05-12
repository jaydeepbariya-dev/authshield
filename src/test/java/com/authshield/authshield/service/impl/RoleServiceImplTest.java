package com.authshield.authshield.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.authshield.authshield.dto.role.AssignRoleRequest;
import com.authshield.authshield.dto.role.RoleRequest;
import com.authshield.authshield.dto.role.RoleResponse;
import com.authshield.authshield.entity.Role;
import com.authshield.authshield.entity.User;
import com.authshield.authshield.repository.RoleRepository;
import com.authshield.authshield.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl Unit Tests")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ROLE_USER");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRoles(new java.util.HashSet<>(Set.of(testRole)));
    }

    @Test
    @DisplayName("GetAllRoles - Returns list of all roles")
    void testGetAllRoles_Success() {
        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("ROLE_ADMIN");

        List<Role> roleList = Arrays.asList(testRole, role2);

        when(roleRepository.findAll()).thenReturn(roleList);

        List<RoleResponse> responses = roleService.getAllRoles();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("ROLE_USER", responses.get(0).getName());
        assertEquals("ROLE_ADMIN", responses.get(1).getName());

        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("GetAllRoles - Returns empty list when no roles exist")
    void testGetAllRoles_Empty() {
        when(roleRepository.findAll()).thenReturn(Arrays.asList());

        List<RoleResponse> responses = roleService.getAllRoles();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("CreateRole - Success with new role")
    void testCreateRole_Success() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("ROLE_ADMIN");

        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        RoleResponse response = roleService.createRole(roleRequest);

        assertNotNull(response);
        assertEquals("ROLE_USER", response.getName());
        assertEquals(1L, response.getId());

        verify(roleRepository, times(1)).findByName("ROLE_ADMIN");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("CreateRole - Fails when role already exists")
    void testCreateRole_RoleAlreadyExists() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("ROLE_USER");

        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> roleService.createRole(roleRequest));

        assertEquals("Role already exists: ROLE_USER", exception.getMessage());
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("UpdateRole - Success with valid role id and new name")
    void testUpdateRole_Success() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("ROLE_MODERATOR");

        testRole.setName("ROLE_MODERATOR");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.findByName("ROLE_MODERATOR")).thenReturn(Optional.empty());
        when(roleRepository.save(testRole)).thenReturn(testRole);

        RoleResponse response = roleService.updateRole(1L, roleRequest);

        assertNotNull(response);
        assertEquals("ROLE_MODERATOR", response.getName());

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findByName("ROLE_MODERATOR");
        verify(roleRepository, times(1)).save(testRole);
    }

    @Test
    @DisplayName("UpdateRole - Fails when role not found")
    void testUpdateRole_RoleNotFound() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("ROLE_MODERATOR");

        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> roleService.updateRole(999L, roleRequest));

        assertEquals("Role not found", exception.getMessage());
        verify(roleRepository, times(1)).findById(999L);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("UpdateRole - Fails when new name already in use by another role")
    void testUpdateRole_NameAlreadyInUse() {
        Role existingRole = new Role();
        existingRole.setId(2L);
        existingRole.setName("ROLE_ADMIN");

        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("ROLE_ADMIN");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(existingRole));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> roleService.updateRole(1L, roleRequest));

        assertEquals("Role name already in use", exception.getMessage());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("UpdateRole - Success when updating with same name")
    void testUpdateRole_SameName() {
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setName("ROLE_USER");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));
        when(roleRepository.save(testRole)).thenReturn(testRole);

        RoleResponse response = roleService.updateRole(1L, roleRequest);

        assertNotNull(response);
        assertEquals("ROLE_USER", response.getName());

        verify(roleRepository, times(1)).save(testRole);
    }

    @Test
    @DisplayName("DeleteRole - Success with valid role id")
    void testDeleteRole_Success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));

        roleService.deleteRole(1L);

        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).delete(testRole);
    }

    @Test
    @DisplayName("DeleteRole - Fails when role not found")
    void testDeleteRole_RoleNotFound() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> roleService.deleteRole(999L));

        assertEquals("Role not found", exception.getMessage());
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    @DisplayName("AssignRoleToUser - Success with valid user and role")
    void testAssignRoleToUser_Success() {
        UUID userId = UUID.randomUUID();
        testUser.setId(userId);

        AssignRoleRequest assignRequest = new AssignRoleRequest();
        assignRequest.setUserId(userId.toString());
        assignRequest.setRoleName("ROLE_ADMIN");

        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ROLE_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        roleService.assignRoleToUser(assignRequest);

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findByName("ROLE_ADMIN");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("AssignRoleToUser - Succeeds silently when role already assigned")
    void testAssignRoleToUser_RoleAlreadyAssigned() {
        UUID userId = UUID.randomUUID();
        testUser.setId(userId);
        testUser.setRoles(Set.of(testRole));

        AssignRoleRequest assignRequest = new AssignRoleRequest();
        assignRequest.setUserId(userId.toString());
        assignRequest.setRoleName("ROLE_USER");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));

        roleService.assignRoleToUser(assignRequest);

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("AssignRoleToUser - Fails with invalid user id format")
    void testAssignRoleToUser_InvalidUserIdFormat() {
        AssignRoleRequest assignRequest = new AssignRoleRequest();
        assignRequest.setUserId("invalidUUID");
        assignRequest.setRoleName("ROLE_USER");

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> roleService.assignRoleToUser(assignRequest));

        assertEquals("Invalid userId format", exception.getMessage());
        verify(userRepository, never()).findById(any(UUID.class));
    }

    @Test
    @DisplayName("AssignRoleToUser - Fails when user not found")
    void testAssignRoleToUser_UserNotFound() {
        UUID userId = UUID.randomUUID();

        AssignRoleRequest assignRequest = new AssignRoleRequest();
        assignRequest.setUserId(userId.toString());
        assignRequest.setRoleName("ROLE_USER");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> roleService.assignRoleToUser(assignRequest));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, never()).findByName(any(String.class));
    }

    @Test
    @DisplayName("AssignRoleToUser - Fails when role not found")
    void testAssignRoleToUser_RoleNotFound() {
        UUID userId = UUID.randomUUID();
        testUser.setId(userId);

        AssignRoleRequest assignRequest = new AssignRoleRequest();
        assignRequest.setUserId(userId.toString());
        assignRequest.setRoleName("ROLE_NONEXISTENT");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_NONEXISTENT")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> roleService.assignRoleToUser(assignRequest));

        assertEquals("Role not found: ROLE_NONEXISTENT", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
