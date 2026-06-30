package com.portal.conecta.hub.module.auth.presentation.controller;

import com.portal.conecta.hub.module.auth.application.command.LoginCommand;
import com.portal.conecta.hub.module.auth.application.command.LogoutCommand;
import com.portal.conecta.hub.module.auth.application.command.RefreshTokenCommand;
import com.portal.conecta.hub.module.auth.application.result.LoginResult;
import com.portal.conecta.hub.module.auth.application.result.RefreshTokenResult;
import com.portal.conecta.hub.module.auth.application.use_case.LoginUseCase;
import com.portal.conecta.hub.module.auth.application.use_case.LogoutUseCase;
import com.portal.conecta.hub.module.auth.application.use_case.RefreshTokenUseCase;
import com.portal.conecta.hub.module.auth.presentation.dto.request.LoginRequest;
import com.portal.conecta.hub.module.auth.presentation.dto.request.LogoutRequest;
import com.portal.conecta.hub.module.auth.presentation.dto.response.LoginResponse;
import com.portal.conecta.hub.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.portal.conecta.hub.module.auth.presentation.dto.request.RefreshTokenRequest;
import com.portal.conecta.hub.module.auth.presentation.dto.response.RefreshTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ponto de entrada HTTP para o fluxo de autenticação do Hub.
 *
 * <p>Todos os endpoints são públicos — excluídos do filtro JWT
 * no {@code SecurityConfig}.</p>
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Operações para autenticação e gestão de sessão do Hub.")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(LoginUseCase loginUseCase, RefreshTokenUseCase refreshTokenUseCase, LogoutUseCase logoutUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

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

    @Operation(
            summary = "Renova os tokens de sessão",
            description = "Recebe um refresh token válido e retorna novos tokens de acesso e refresh."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens renovados com sucesso.",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida (ex: refresh token ausente ou em branco).",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido, expirado ou com tipo incorreto.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário inexistente, inativo ou bloqueado.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> updateToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token necessário para renovar a sessão.",
                    required = true
            )
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest
    ) {
        RefreshTokenCommand command = new RefreshTokenCommand(refreshTokenRequest.refreshToken());
        RefreshTokenResult result = refreshTokenUseCase.execute(command);
        return ResponseEntity.ok(new RefreshTokenResponse(result.accessToken(), result.refreshToken(), result.expiresIn()));
    }

    @Operation(
            summary = "Encerra a sessão do usuário",
            description = "Invalida o refresh token informado, impedindo sua reutilização. Nenhum novo token é emitido."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Sessão encerrada com sucesso."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida (ex: refresh token ausente ou em branco).",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido, expirado, já revogado ou inexistente.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token da sessão a ser encerrada.",
                    required = true
            )
            @Valid @RequestBody LogoutRequest logoutRequest
    ) {
        LogoutCommand command = new LogoutCommand(logoutRequest.refreshToken());
        logoutUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

}