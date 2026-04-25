package com.authshield.authshield.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.authshield.authshield.dto.user.UpdateUserRequest;
import com.authshield.authshield.dto.user.UserResponse;
import com.authshield.authshield.entity.User;
import com.authshield.authshield.repository.RefreshTokenRepository;
import com.authshield.authshield.repository.UserRepository;
import com.authshield.authshield.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }

        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }

        User updated = userRepository.save(user);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.deleteByUserId(id);

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {

        UserResponse res = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                false, user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()),
                user.getCreatedAt());

        res.setEnabled(user.isEnabled());
        res.setCreatedAt(user.getCreatedAt());

        return res;
    }
}