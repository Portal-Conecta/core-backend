package com.portal.conecta.hub.module.user.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import com.portal.conecta.hub.module.classes.application.use_case.classes.get.GetActiveClassByUserUseCase;
import com.portal.conecta.hub.module.classes.presentation.dto.response.ClassMembershipResponse;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST do módulo de usuários do Hub Core.
 *
 * <p>Expõe endpoints para criação, listagem, atualização, desativação,
 * consulta por ID, consulta em lote e consulta de turma ativa por usuário.
 * Todas as rotas exigem autenticação Bearer JWT.
 */
@Tag(name = "Usuários", description = "Operações para administração e consulta de usuários do Hub.")
@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetAllUserUseCase getAllUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetUsersBulkUseCase getusersBulkCase;
    private final GetActiveClassByUserUseCase getActiveClassByUserUseCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            GetAllUserUseCase getAllUserUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeactivateUserUseCase deactivateUserUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            GetUsersBulkUseCase getusersBulkCase,
            GetActiveClassByUserUseCase getActiveClassByUserUseCase
    ) {
        this.createUserUseCase = createUserUseCase;
        this.getAllUserUseCase = getAllUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.getusersBulkCase = getusersBulkCase;
        this.getActiveClassByUserUseCase = getActiveClassByUserUseCase;
    }

    @Operation(
            summary = "Cria um novo usuário",
            description = "Registra um novo usuário no sistema. O e-mail fornecido deve pertencer aos domínios institucionais da WEG ou SENAI. O usuário logado precisa ter as permissões adequadas para criar o perfil solicitado.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação do novo usuário.",
                    required = true
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso.",
                    content = @Content(schema = @Schema(implementation = CreateUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos ou e-mail com formato/domínio não permitido.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário autenticado não tem permissão para criar este tipo de perfil.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já está em uso por outro usuário.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<CreateUserResponse> create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserEntity createdUser = createUserUseCase.execute(request.toCommand());

        return ResponseEntity.created(URI.create("/users/" + createdUser.getId()))
                .body(CreateUserResponse.from(createdUser));
    }

    @Operation(
            summary = "Lista usuários",
            description = "Retorna uma lista paginada e filtrada de usuários cadastrados. Acesso permitido apenas para perfis autorizados (Administradores, e usuários WEG ou SENAI).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso.",
                    content = @Content(schema = @Schema(implementation = ListUsersResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação ou filtro inválidos.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para listar usuários.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<ListUsersResponse> list(
            @Valid @ModelAttribute ListUsersRequest request
    ) {
        Page<UserEntity> users = getAllUserUseCase.execute(request.toQuery());

        return ResponseEntity.ok(ListUsersResponse.from(users));
    }

    @Operation(
            summary = "Atualiza dados do usuário",
            description = "Permite a atualização dos dados do usuário. Um usuário pode alterar seu próprio perfil, ou alterar outros perfis conforme regra de hierarquia permitida.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para atualização.",
                    required = true
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso.",
                    content = @Content(schema = @Schema(implementation = UpdateUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados fornecidos são inválidos.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para editar este perfil.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já está em uso por outro usuário.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<UpdateUserResponse> update(
            @Parameter(description = "Identificador do usuário a ser atualizado.", example = "550e8400-e29b-41d4-a716-446655440000")
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

    @Operation(
            summary = "Desativa usuário",
            description = "Realiza a desativação (exclusão lógica) do usuário especificado. Requer permissões administrativas ou níveis superiores de hierarquia.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário desativado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Formato de identificador inválido.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário autenticado não tem permissão para desativar este perfil.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @Parameter(description = "Identificador do usuário a ser desativado.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id
    ) {
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
            @ApiResponse(responseCode = "400", description = "Formato de identificador inválido.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuário inexistente, removido ou inativo.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "Identificador do usuário.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID userId
    ) {
        UserEntity userEntity = getUserByIdUseCase.execute(userId);
        return ResponseEntity.ok(UserResponse.from(userEntity));
    }

    @Operation(
            summary = "Consulta usuários em lote",
            description = "Retorna usuários ativos pelos IDs informados. IDs duplicados na requisição são filtrados. IDs inexistentes, inativos ou deletados serão mapeados no array missingIds.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Lista de identificadores de usuários.",
                    required = true
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso.",
                    content = @Content(schema = @Schema(implementation = BulkUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload malformado ou lista de IDs inválida.",
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

    @Operation(
            summary = "Consulta a turma ativa de um usuário",
            description = "Retorna o UUID da turma ativa vinculada a um usuário com papel STUDENT ou REPRESENTATIVE. "
                    + "Turmas desativadas ou removidas não são retornadas. "
                    + "Um aprendiz possui, no máximo, uma turma ativa elegível.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Turma ativa encontrada.",
                    content = @Content(schema = @Schema(implementation = UUID.class, example = "550e8400-e29b-41d4-a716-446655440000"))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Usuário inexistente, inativo ou removido.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{userId}/class")
    public ResponseEntity<List<ClassMembershipResponse>> getActiveClass(
            @Parameter(description = "Identificador único do usuário.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID userId
    ) {
        var classes = getActiveClassByUserUseCase.execute(new GetActiveClassByUserCommand(userId));

        var response = classes.stream()
                .map(ClassMembershipResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}