package com.authshield.authshield.service.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.authshield.authshield.dto.role.AssignRoleRequest;
import com.authshield.authshield.dto.role.RoleRequest;
import com.authshield.authshield.dto.role.RoleResponse;
import com.authshield.authshield.entity.Role;
import com.authshield.authshield.entity.User;
import com.authshield.authshield.repository.RoleRepository;
import com.authshield.authshield.repository.UserRepository;
import com.authshield.authshield.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleServiceImpl(RoleRepository roleRepository,
            UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse createRole(RoleRequest dto) {

        if (roleRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Role already exists: " + dto.getName());
        }

        Role role = new Role();
        role.setName(dto.getName());

        Role saved = roleRepository.save(role);

        return mapToResponse(saved);
    }

    @Override
    public RoleResponse updateRole(Long id, RoleRequest dto) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Role existing = roleRepository.findByName(dto.getName()).orElse(null);

        if (existing != null && !existing.getId().equals(id)) {
            throw new RuntimeException("Role name already in use");
        }

        role.setName(dto.getName());

        Role updated = roleRepository.save(role);

        return mapToResponse(updated);
    }

    @Override
    public void deleteRole(Long id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        roleRepository.delete(role);
    }

    @Override
    public void assignRoleToUser(AssignRoleRequest dto) {

        UUID userId;

        try {
            userId = UUID.fromString(dto.getUserId());
        } catch (Exception e) {
            throw new RuntimeException("Invalid userId format");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findByName(dto.getRoleName()).orElse(null);

        if (role == null) {
            throw new RuntimeException("Role not found: " + dto.getRoleName());
        }

        Set<Role> roles = user.getRoles();

        if (roles.contains(role)) {
            return;
        }

        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);
    }

    private RoleResponse mapToResponse(Role role) {
        RoleResponse res = new RoleResponse();
        res.setId(role.getId());
        res.setName(role.getName());
        return res;
    }
}