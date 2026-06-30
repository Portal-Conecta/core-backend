package com.portal.conecta.hub.module.auth.presentation.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}