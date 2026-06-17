package com.portal.conecta.hub.module.user.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        String name,
        @Email(message = "e-mail deve ser válido")
        String email,
        String avatarUrl
) {
}
