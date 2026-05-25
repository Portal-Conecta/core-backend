package com.portal.conecta.hub.module.user.presentation.dto.request;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "name is required.")
        @Size(max = 150, message = "name must have at most 150 characters.")
        String name,

        @NotBlank(message = "email is required.")
        @Email(message = "email must be valid.")
        @Size(max = 180, message = "email must have at most 180 characters.")
        String email,

        @NotBlank(message = "password is required.")
        String password,

        @NotNull(message = "typeUser is required.")
        TypeUser typeUser
) {

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(name, email, password, typeUser);
    }
}
