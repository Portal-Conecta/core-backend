package com.portal.conecta.hub.module.auth.presentation.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}