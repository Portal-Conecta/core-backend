package com.portal.conecta.hub.module.user.presentation.dto.request;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String name,

        @NotBlank(message = "O email é obrigatório.")
        @Email(message = "e-mail deve ser válido.")
        @Size(max = 180, message = "O email deve ter no máximo 180 caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        String password,

        @NotNull(message = "O tipo de usuário é obrigatório.")
        TypeUser typeUser
) {

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(name, email, password, typeUser);
    }
}
