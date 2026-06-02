package com.portal.conecta.hub.module.auth.presentation.controller;

import com.portal.conecta.hub.module.auth.application.command.LoginCommand;
import com.portal.conecta.hub.module.auth.application.command.RefreshTokenCommand;
import com.portal.conecta.hub.module.auth.application.result.LoginResult;
import com.portal.conecta.hub.module.auth.application.result.RefreshTokenResult;
import com.portal.conecta.hub.module.auth.application.use_case.LoginUseCase;
import com.portal.conecta.hub.module.auth.application.use_case.RefreshTokenUseCase;
import com.portal.conecta.hub.module.auth.presentation.dto.LoginRequest;
import com.portal.conecta.hub.module.auth.presentation.dto.LoginResponse;
import com.portal.conecta.hub.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.portal.conecta.hub.module.auth.presentation.dto.RefreshTokenRequest;
import com.portal.conecta.hub.module.auth.presentation.dto.RefreshTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Operações para autenticação e gestão de sessão do Hub.")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @Operation(
            summary = "Realiza login do usuário",
            description = "Recebe as credenciais do usuário e retorna os tokens de acesso para a sessão."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuário autenticado com sucesso.",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida (ex: payload incorreto ou incompleto).",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas ou não autorizadas.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais necessárias para realizar a autenticação.",
                    required = true
            )
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginCommand command = new LoginCommand(loginRequest.email(), loginRequest.password());
        LoginResult result = loginUseCase.execute(command);
        return ResponseEntity.ok(new LoginResponse(result.accessToken(), result.refreshToken(), result.expiresIn()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> updateToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        RefreshTokenCommand command = new RefreshTokenCommand(refreshTokenRequest.refreshToken());
        RefreshTokenResult result = refreshTokenUseCase.execute(command);
        return ResponseEntity.ok(new RefreshTokenResponse(result.accessToken(), result.refreshToken(), result.expiresIn()));
    }

}