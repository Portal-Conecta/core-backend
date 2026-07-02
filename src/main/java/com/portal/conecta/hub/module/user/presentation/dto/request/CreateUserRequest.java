package com.portal.conecta.hub.module.user.presentation.dto.request;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "O nome e obrigatorio.")
        @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres.")
        String name,

        @NotBlank(message = "O email e obrigatorio.")
        @Email(message = "e-mail deve ser valido.")
        @Size(max = 180, message = "O email deve ter no maximo 180 caracteres.")
        String email,

        @NotNull(message = "O tipo de usuario e obrigatorio.")
        TypeUser typeUser
) {

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(name, email, typeUser);
    }
}
