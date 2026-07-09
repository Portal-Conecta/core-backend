package com.portal.conecta.hub.module.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload contendo o refresh token para renovação da sessão.")
public record RefreshTokenRequest(

        @Schema(description = "Refresh token JWT emitido no login ou na última renovação.", example = "eyJhbGci...")
        @NotBlank String refreshToken
) {}