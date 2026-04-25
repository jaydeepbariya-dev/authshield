package com.authshield.authshield.service;

import java.util.List;
import java.util.UUID;

import com.authshield.authshield.dto.user.UpdateUserRequest;
import com.authshield.authshield.dto.user.UserResponse;


public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(UUID id);

    UserResponse updateUser(UUID id, UpdateUserRequest dto);

    void deleteUser(UUID id);

}