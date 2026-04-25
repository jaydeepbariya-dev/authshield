package com.authshield.authshield.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class TokenValidationRequest {

    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
