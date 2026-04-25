package com.authshield.authshield.service;

import java.util.List;

import com.authshield.authshield.dto.role.AssignRoleRequest;
import com.authshield.authshield.dto.role.RoleRequest;
import com.authshield.authshield.dto.role.RoleResponse;

public interface RoleService {

    List<RoleResponse> getAllRoles();

    RoleResponse createRole(RoleRequest dto);

    RoleResponse updateRole(Long id, RoleRequest dto);

    void deleteRole(Long id);

    void assignRoleToUser(AssignRoleRequest dto);

}
