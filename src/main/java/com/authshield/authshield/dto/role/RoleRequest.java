package com.authshield.authshield.dto.role;

import jakarta.validation.constraints.NotBlank;

public class RoleRequest {

    @NotBlank
    private String name; // e.g. ROLE_USER, ROLE_ADMIN

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
