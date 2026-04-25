package com.authshield.authshield.dto.role;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class AssignRoleRequest {

    @NotNull
    private String userId;

    @NotBlank
    private String roleName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}