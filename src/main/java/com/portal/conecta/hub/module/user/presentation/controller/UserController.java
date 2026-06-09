package com.portal.conecta.hub.module.user.presentation.controller;

import com.portal.conecta.hub.module.user.application.command.DeactivateUserCommand;
import com.portal.conecta.hub.module.user.application.command.UpdateUserCommand;
import com.portal.conecta.hub.module.user.application.use_case.*;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;

import com.portal.conecta.hub.module.user.presentation.dto.request.BulkUserRequest;
import com.portal.conecta.hub.module.user.presentation.dto.request.CreateUserRequest;
import com.portal.conecta.hub.module.user.presentation.dto.request.ListUsersRequest;
import com.portal.conecta.hub.module.user.presentation.dto.request.UpdateUserRequest;

import com.portal.conecta.hub.module.user.presentation.dto.response.*;

import com.portal.conecta.hub.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetAllUserUseCase getAllUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetUsersBulkUseCase getusersBulkCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            GetAllUserUseCase getAllUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeactivateUserUseCase deactivateUserUseCase, GetUserByIdUseCase getUserByIdUseCase, GetUsersBulkUseCase getusersBulkCase
    ) {
        this.createUserUseCase = createUserUseCase;
        this.getAllUserUseCase = getAllUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.getusersBulkCase = getusersBulkCase;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserEntity createdUser = createUserUseCase.execute(request.toCommand());

        return ResponseEntity.created(URI.create("/users/" + createdUser.getId()))
                .body(CreateUserResponse.from(createdUser));
    }

    @GetMapping
    public ResponseEntity<ListUsersResponse> list(
            @Valid @ModelAttribute ListUsersRequest request
    ) {
        Page<UserEntity> users = getAllUserUseCase.execute(request.toQuery());

        return ResponseEntity.ok(ListUsersResponse.from(users));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UpdateUserResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserEntity updated = updateUserUseCase.execute(new UpdateUserCommand(
                id,
                request.name(),
                request.email(),
                request.avatarUrl()
            ));

        return ResponseEntity.ok(UpdateUserResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        deactivateUserUseCase.execute(new DeactivateUserCommand(id));

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Consulta usuário por ID",
            description = "Retorna os dados básicos de um usuário ativo para integração. Se o usuário estiver inativo ou removido logicamente, retornará 404.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso.",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            // CORREÇÃO AQUI: Trocado a String por ApiError.class
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuário inexistente, removido ou inativo.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "Identificador do usuário.") @PathVariable UUID userId
    ) {
        UserEntity userEntity = getUserByIdUseCase.execute(userId);
        return ResponseEntity.ok(UserResponse.from(userEntity));
    }

    @Operation(
            summary = "Consulta usuários em lote",
            description = "Retorna usuários ativos pelos IDs informados. IDs duplicados na requisição são filtrados. IDs inexistentes, inativos ou deletados serão mapeados no array missingIds.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso.",
                    content = @Content(schema = @Schema(implementation = BulkUserResponse.class))),
            // CORREÇÃO AQUI: Trocado a String por ApiError.class
            @ApiResponse(responseCode = "400", description = "Request malformado ou IDs inválidos.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/bulk")
    public ResponseEntity<BulkUserResponse> getBulkUsers(
            @Valid @RequestBody BulkUserRequest request
    ) {
        return ResponseEntity.ok(getusersBulkCase.execute(request.ids()));
    }
}