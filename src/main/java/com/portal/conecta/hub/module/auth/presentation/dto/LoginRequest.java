package com.portal.conecta.hub.module.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload contendo as credenciais para realizar o login na plataforma.")
public record LoginRequest(

        @Schema(description = "E-mail de acesso do usuário.", example = "maria.silva@example.com")
        @NotBlank String email,

        @Schema(description = "Senha de acesso do usuário.", example = "senhaSegura123")
        @NotBlank String password
) {}