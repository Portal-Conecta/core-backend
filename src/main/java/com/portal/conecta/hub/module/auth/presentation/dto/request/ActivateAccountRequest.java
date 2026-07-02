package com.portal.conecta.hub.module.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for the public account activation endpoint.
 *
 * @param token raw activation token received in the activation link
 * @param newPassword password chosen by the user during activation
 */
public record ActivateAccountRequest(
        @NotBlank(message = "O token e obrigatorio.")
        String token,

        @NotBlank(message = "A nova senha e obrigatoria.")
        @Size(min = 6, message = "A nova senha deve ter no minimo 6 caracteres.")
        String newPassword
) {
}
