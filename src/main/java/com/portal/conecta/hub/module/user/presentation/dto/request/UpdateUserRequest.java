package com.portal.conecta.hub.module.user.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        String name,
        @Email(message = "email must be valid")
        String email,
        String avatarUrl
) {
}
