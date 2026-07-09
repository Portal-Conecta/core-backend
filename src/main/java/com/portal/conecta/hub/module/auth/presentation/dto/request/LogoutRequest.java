package com.portal.conecta.hub.module.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload para encerramento de sessão.")
public record LogoutRequest(

        @NotBlank(message = "O refresh token não pode estar ausente ou em branco.")
        @Schema(description = "Refresh token da sessão a ser encerrada.")
        String refreshToken
) {
}
