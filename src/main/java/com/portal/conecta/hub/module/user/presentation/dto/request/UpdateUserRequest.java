package com.portal.conecta.hub.module.user.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(

        @NotBlank(message = "name is required.")
        String name,
        @NotBlank(message = "email is required.")
        @Email(message = "email must be valid.")
        String email,
        String avatarUrl
) {
}
