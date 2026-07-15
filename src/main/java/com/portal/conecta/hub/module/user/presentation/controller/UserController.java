package com.portal.conecta.hub.module.user.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import com.portal.conecta.hub.module.classes.application.use_case.classes.get.GetActiveClassByUserUseCase;
import com.portal.conecta.hub.module.classes.presentation.dto.response.ClassMembershipResponse;
import com.portal.conecta.hub.module.user.application.command.DeactivateUserCommand;
import com.portal.conecta.hub.module.user.application.command.DeleteUserCommand;
import com.portal.conecta.hub.module.user.application.command.ReactivateUserCommand;
import com.portal.conecta.hub.module.user.application.command.UpdateUserCommand;
import com.portal.conecta.hub.module.user.application.use_case.CreateUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.DeactivateUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.DeleteUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.GetAllUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.GetUserByIdUseCase;
import com.portal.conecta.hub.module.user.application.use_case.GetUsersBulkUseCase;
import com.portal.conecta.hub.module.user.application.use_case.ReactivateUserUseCase;
import com.portal.conecta.hub.module.user.application.use_case.UpdateUserUseCase;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.presentation.dto.request.BulkUserRequest;
import com.portal.conecta.hub.module.user.presentation.dto.request.CreateUserRequest;
import com.portal.conecta.hub.module.user.presentation.dto.request.ListUsersRequest;
import com.portal.conecta.hub.module.user.presentation.dto.request.UpdateUserRequest;
import com.portal.conecta.hub.module.user.presentation.dto.response.BulkUserResponse;
import com.portal.conecta.hub.module.user.presentation.dto.response.CreateUserResponse;
import com.portal.conecta.hub.module.user.presentation.dto.response.ListUsersResponse;
import com.portal.conecta.hub.module.user.presentation.dto.response.UpdateUserResponse;
import com.portal.conecta.hub.module.user.presentation.dto.response.UserLifecycleResponse;
import com.portal.conecta.hub.module.user.presentation.dto.response.UserResponse;
import com.portal.conecta.hub.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usuarios", description = "Operacoes para administracao e consulta de usuarios do Hub.")
@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetAllUserUseCase getAllUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ReactivateUserUseCase reactivateUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetUsersBulkUseCase getusersBulkCase;
    private final GetActiveClassByUserUseCase getActiveClassByUserUseCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            GetAllUserUseCase getAllUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeactivateUserUseCase deactivateUserUseCase,
            DeleteUserUseCase deleteUserUseCase,
            ReactivateUserUseCase reactivateUserUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            GetUsersBulkUseCase getusersBulkCase,
            GetActiveClassByUserUseCase getActiveClassByUserUseCase
    ) {
        this.createUserUseCase = createUserUseCase;
        this.getAllUserUseCase = getAllUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.reactivateUserUseCase = reactivateUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.getusersBulkCase = getusersBulkCase;
        this.getActiveClassByUserUseCase = getActiveClassByUserUseCase;
    }

    @Operation(summary = "Cria um novo usuario", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario criado com sucesso.",
                    content = @Content(schema = @Schema(implementation = CreateUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados invalidos.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuario sem permissao.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ja esta em uso.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<CreateUserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserEntity createdUser = createUserUseCase.execute(request.toCommand());
        return ResponseEntity.created(URI.create("/users/" + createdUser.getId()))
                .body(CreateUserResponse.from(createdUser));
    }

    @Operation(summary = "Lista usuarios", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<ListUsersResponse> list(@Valid @ModelAttribute ListUsersRequest request) {
        Page<UserEntity> users = getAllUserUseCase.execute(request.toQuery());
        return ResponseEntity.ok(ListUsersResponse.from(users));
    }

    @Operation(summary = "Atualiza dados do usuario", security = @SecurityRequirement(name = "bearerAuth"))
    @PatchMapping("/{id}")
    public ResponseEntity<UpdateUserResponse> update(
            @Parameter(description = "Identificador do usuario.", example = "550e8400-e29b-41d4-a716-446655440000")
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

    @Operation(summary = "Desativa usuario", description = "Muda uma conta ACTIVE para DISABLED sem preencher deletedAt.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<UserLifecycleResponse> deactivate(@PathVariable UUID id) {
        UserEntity user = deactivateUserUseCase.execute(new DeactivateUserCommand(id));
        return ResponseEntity.ok(UserLifecycleResponse.from(user));
    }

    @Operation(summary = "Reativa usuario", description = "Muda uma conta DISABLED para ACTIVE.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<UserLifecycleResponse> reactivate(@PathVariable UUID id) {
        UserEntity user = reactivateUserUseCase.execute(new ReactivateUserCommand(id));
        return ResponseEntity.ok(UserLifecycleResponse.from(user));
    }

    @Operation(summary = "Remove usuario", description = "Marca a conta como PENDING_DELETION e preenche deletedAt para purge futuro.", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<UserLifecycleResponse> delete(@PathVariable UUID id) {
        UserEntity user = deleteUserUseCase.execute(new DeleteUserCommand(id));
        return ResponseEntity.ok(UserLifecycleResponse.from(user));
    }

    @Operation(summary = "Consulta usuario por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        UserEntity userEntity = getUserByIdUseCase.execute(userId);
        return ResponseEntity.ok(UserResponse.from(userEntity));
    }

    @Operation(summary = "Consulta usuarios em lote", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/bulk")
    public ResponseEntity<BulkUserResponse> getBulkUsers(@Valid @RequestBody BulkUserRequest request) {
        return ResponseEntity.ok(getusersBulkCase.execute(request.ids(), Boolean.TRUE.equals(request.includePending())));
    }

    @Operation(summary = "Consulta a turma ativa de um usuario", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{userId}/class")
    public ResponseEntity<List<ClassMembershipResponse>> getActiveClass(@PathVariable UUID userId) {
        var classes = getActiveClassByUserUseCase.execute(new GetActiveClassByUserCommand(userId));
        var response = classes.stream()
                .map(ClassMembershipResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}
