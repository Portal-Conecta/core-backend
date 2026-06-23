package com.portal.conecta.hub.module.classes.presentation.controller;

import com.portal.conecta.hub.module.classes.application.command.*;
import com.portal.conecta.hub.module.classes.application.use_case.*;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.presentation.dto.request.*;
import com.portal.conecta.hub.module.classes.presentation.dto.response.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Turmas", description = "Operações para o gerenciamento de turmas e vínculos de membros no Hub.")
@RestController
@RequestMapping("/classes")
public class ClassController {

    private final CreateClassUseCase createClassUseCase;
    private final DeleteClassUseCase deleteClassUseCase;
    private final AddClassMemberUseCase addClassMemberUseCase;
    private final PromoteToRepresentativeUseCase promoteToRepresentativeUseCase;
    private final DemoteFromRepresentativeUseCase demoteFromRepresentativeUseCase;
    private final DeleteClassMembershipUseCase deleteClassMembershipUseCase;
    private final GetClassByIdUseCase getClassByIdUseCase;
    private final GetClassesBulkUseCase getClassesBulkUseCase;
    private final GetAllClassesUseCase getAllClassesUseCase;
    private final RestoreClassUseCase restoreClassUseCase;
    private final DeactivateClassUseCase deactivateClassUseCase;
    private final ReactivateClassUseCase reactivateClassUseCase;
    private final GetClassStudentUseCase getClassStudentUseCase;
    private final BulkAddClassMembersUseCase bulkAddClassMembersUseCase;
    private final GetActiveClassByUserUseCase getActiveClassByUserUseCase;


    public ClassController(CreateClassUseCase createClassUseCase, DeleteClassUseCase deleteClassUseCase, AddClassMemberUseCase addClassMemberUseCase, PromoteToRepresentativeUseCase promoteToRepresentativeUseCase, DemoteFromRepresentativeUseCase demoteFromRepresentativeUseCase, DeleteClassMembershipUseCase deleteClassMembershipUseCase, GetClassByIdUseCase getClassByIdUseCase, GetClassesBulkUseCase getClassesBulkUseCase, GetAllClassesUseCase getAllClassesUseCase, RestoreClassUseCase restoreClassUseCase, DeactivateClassUseCase deactivateClassUseCase, ReactivateClassUseCase reactivateClassUseCase, GetClassStudentUseCase getClassStudentUseCase, BulkAddClassMembersUseCase bulkAddClassMembersUseCase, GetActiveClassByUserUseCase getActiveClassByUserUseCase) {
        this.createClassUseCase = createClassUseCase;
        this.deleteClassUseCase = deleteClassUseCase;
        this.addClassMemberUseCase = addClassMemberUseCase;
        this.promoteToRepresentativeUseCase = promoteToRepresentativeUseCase;
        this.demoteFromRepresentativeUseCase = demoteFromRepresentativeUseCase;
        this.deleteClassMembershipUseCase = deleteClassMembershipUseCase;
        this.getClassByIdUseCase = getClassByIdUseCase;
        this.getClassesBulkUseCase = getClassesBulkUseCase;
        this.getAllClassesUseCase = getAllClassesUseCase;
        this.restoreClassUseCase = restoreClassUseCase;
        this.deactivateClassUseCase = deactivateClassUseCase;
        this.reactivateClassUseCase = reactivateClassUseCase;
        this.getClassStudentUseCase = getClassStudentUseCase;
        this.bulkAddClassMembersUseCase = bulkAddClassMembersUseCase;
        this.getActiveClassByUserUseCase = getActiveClassByUserUseCase;
    }

    @Operation(
            summary = "Cria uma nova turma",
            description = "Registra uma nova turma no sistema, vinculando-a a um curso e a um turno específico.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turma criada com sucesso.", content = @Content(schema = @Schema(implementation = CreateClassResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida. Parâmetros incorretos ou ausentes.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para criar turmas.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Curso não encontrado.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe turma com esse número neste curso.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<CreateClassResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados obrigatórios para a criação da turma.", required = true)
            @Valid @RequestBody CreateClassRequest request
    ) {
        ClassEntity createdClass = createClassUseCase.execute(new CreateClassCommand(
                request.shift(),
                request.courseId(),
                request.number()
        ));

        return ResponseEntity.created(URI.create("/classes/" + createdClass.getId()))
                .body(CreateClassResponse.from(createdClass));
    }

    @Operation(
            summary = "Exclui uma turma",
            description = "Remove permanentemente uma turma do sistema com base no seu identificador.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Turma excluída com sucesso."),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para excluir turmas.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador único da turma.", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id
    ) {
        deleteClassUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Adiciona membro à turma",
            description = "Vincula um usuário a uma turma, atribuindo-lhe um papel específico (ex: STUDENT, TEACHER).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membro adicionado com sucesso.", content = @Content(schema = @Schema(implementation = AddMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida ou o usuário já é membro desta turma.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para adicionar membros.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Turma ou usuário não encontrados.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{classId}/members")
    public ResponseEntity<AddMemberResponse> addMember(
            @Parameter(description = "Identificador único da turma.", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID classId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do usuário a ser vinculado e seu respectivo papel.", required = true)
            @Valid @RequestBody AddMemberRequest request
    ) {
        AddMemberCommand command = new AddMemberCommand(classId, request.userId(), request.classRole());
        ClassMembershipEntity membership = addClassMemberUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AddMemberResponse.from(membership));
    }

    @Operation(
            summary = "Promove membro a representante",
            description = "Altera o vínculo de um membro existente na turma para lhe conceder o status de representante.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro promovido a representante com sucesso.", content = @Content(schema = @Schema(implementation = PromoteMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Membro já é um representante ou estado inválido.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para alterar papéis de membros.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Vínculo entre usuário e turma não encontrado.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{classId}/members/{userId}/representative")
    public ResponseEntity<PromoteMemberResponse> promoteToRepresentative(
            @Parameter(description = "Identificador único da turma.", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID classId,
            @Parameter(description = "Identificador único do usuário.", example = "987e6543-e21b-34d5-c678-426614174999")
            @PathVariable UUID userId
    ) {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, userId);
        ClassMembershipEntity membership = promoteToRepresentativeUseCase.execute(command);
        return ResponseEntity.ok(PromoteMemberResponse.from(membership));
    }

    @Operation(
            summary = "Rebaixa representante",
            description = "Remove o status de representante de um membro, retornando-o às permissões do seu papel padrão.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status de representante removido com sucesso.", content = @Content(schema = @Schema(implementation = DemoteMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Membro não é um representante atual.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para alterar papéis de membros.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Vínculo entre usuário e turma não encontrado.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{classId}/members/{userId}/representative")
    public ResponseEntity<DemoteMemberResponse> demoteFromRepresentative(
            @Parameter(description = "Identificador único da turma.", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID classId,
            @Parameter(description = "Identificador único do usuário.", example = "987e6543-e21b-34d5-c678-426614174999")
            @PathVariable UUID userId
    ) {

        DemoteMemberCommand command = new DemoteMemberCommand(classId, userId);
        ClassMembershipEntity membership = demoteFromRepresentativeUseCase.execute(command);

        return ResponseEntity.ok(DemoteMemberResponse.from(membership));
    }

    @Operation(
            summary = "Remove membro da turma",
            description = "Desvincula um usuário de uma turma. O usuário perde o acesso associado a essa classe.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membro removido da turma com sucesso."),
            @ApiResponse(responseCode = "400", description = "Usuário tentou remover o próprio vínculo ou a regra de negócio é inválida.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para remover membros.", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Vínculo entre usuário e turma não encontrado.", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{classId}/members/{userId}")
    public ResponseEntity<Void> deleteMembership(
            @Parameter(description = "Identificador único da turma.", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID classId,
            @Parameter(description = "Identificador único do usuário a ser removido.", example = "987e6543-e21b-34d5-c678-426614174999")
            @PathVariable UUID userId
    ) {

        DeleteMembershipCommand command = new DeleteMembershipCommand(classId, userId);
        deleteClassMembershipUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Consulta turma por ID",
            description = "Retorna dados básicos de uma turma ativa. Turmas removidas ou inexistentes retornam 404.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma encontrada.",
                    content = @Content(schema = @Schema(implementation = ClassResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Turma inexistente ou removida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{classId}")
    public ResponseEntity<ClassResponse> getById(
            @Parameter(description = "Identificador da turma.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID classId) {
        return ResponseEntity.ok(ClassResponse.from(getClassByIdUseCase.execute(classId)));
    }

    @Operation(
            summary = "Consulta turmas em lote",
            description = """
                    Retorna turmas pelos IDs informados. IDs duplicados são ignorados.
                    Por padrão, apenas turmas ativas são retornadas e IDs de turmas desativadas aparecem em missingIds.
                    Use includeInactive=true para incluir turmas desativadas em items.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso.",
                    content = @Content(schema = @Schema(implementation = BulkClassResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request malformado ou IDs inválidos.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/bulk")
    public ResponseEntity<BulkClassResponse> bulk(
            @Valid @RequestBody BulkClassRequest request) {
        return ResponseEntity.ok(getClassesBulkUseCase.execute(request.ids(), Boolean.TRUE.equals(request.includeInactive())));
    }

    @Operation(
            summary = "Lista turmas",
            description = "Retorna uma lista paginada de turmas ativas. É possível filtrar e paginar o resultado através dos parâmetros de query.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso.",
                    content = @Content(schema = @Schema(implementation = ListClassesResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida. Parâmetros incorretos.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<ListClassesResponse> listAll(
            @Valid @ModelAttribute ListClassesRequest request
    ) {
        Page<ClassEntity> page = getAllClassesUseCase.execute(request.toQuery());
        return ResponseEntity.ok(ListClassesResponse.from(page));
    }


    @Operation(
            summary = "Restaura turma desativada",
            description = "Restaura uma turma removida logicamente. Apenas ADMIN, SENAI e WEG podem executar esta operação.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma restaurada com sucesso.",
                    content = @Content(schema = @Schema(implementation = RestoreClassResponse.class))),
            @ApiResponse(responseCode = "400", description = "Turma já está ativa.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para restaurar turma.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Turma inexistente.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{classId}/restore")
    public ResponseEntity<RestoreClassResponse> restore(
            @Parameter(description = "Identificador da turma.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID classId
    ) {
        return ResponseEntity.ok(RestoreClassResponse.from(
                restoreClassUseCase.execute(classId)));
    }

    @Operation(
            summary = "Inativa uma turma",
            description = "Marca a turma como inativa. A turma permanece no banco para consulta histórica, mas deixa de participar dos fluxos normais. Apenas ADMIN, SENAI e WEG podem executar esta operação.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma inativada com sucesso.",
                    content = @Content(schema = @Schema(implementation = DeactivateClassResponse.class))),
            @ApiResponse(responseCode = "400", description = "Turma já está inativa.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para inativar turma.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Turma inexistente.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{classId}/deactivate")
    public ResponseEntity<DeactivateClassResponse> deactivate(
            @Parameter(description = "Identificador da turma.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID classId
    ) {
        return ResponseEntity.ok(DeactivateClassResponse.from(
                deactivateClassUseCase.execute(classId)));
    }

    @Operation(
            summary = "Reativa uma turma inativa",
            description = "Restaura o status ativo de uma turma previamente inativada. Apenas ADMIN, SENAI e WEG podem executar esta operação.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma reativada com sucesso.",
                    content = @Content(schema = @Schema(implementation = ReactivateClassResponse.class))),
            @ApiResponse(responseCode = "400", description = "Turma já está ativa.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para reativar turma.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Turma inexistente.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{classId}/reactivate")
    public ResponseEntity<ReactivateClassResponse> reactivate(
            @Parameter(description = "Identificador da turma.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID classId
    ) {
        return ResponseEntity.ok(ReactivateClassResponse.from(
                reactivateClassUseCase.execute(classId)));
    }

    @Operation(
            summary = "Lista alunos da turma",
            description = "Retorna os aprendizes vinculados a uma turma ativa. Inclui representantes, pois continuam ocupando posição no mapa de sala. Não inclui docentes nem dados sensíveis.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de alunos retornada com sucesso.",
                    content = @Content(schema = @Schema(implementation = ClassStudentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Turma inexistente ou desativada.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{classId}/students")
    public ResponseEntity<List<ClassStudentResponse>> listStudents(
            @Parameter(description = "Identificador da turma.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID classId
    ) {
        List<ClassMembershipEntity> memberships = getClassStudentUseCase.execute(classId);
        List<ClassStudentResponse> response = memberships.stream()
                .map(ClassStudentResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Adiciona múltiplos membros à turma",
            description = "Vincula vários usuários a uma turma em uma única requisição. A operação é transacional: se qualquer item for inválido, nenhum vínculo é criado.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Todos os membros foram adicionados com sucesso.",
                    content = @Content(schema = @Schema(implementation = BulkAddMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload inválido, lista vazia, ou algum item violou regra de vínculo.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para adicionar membros.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada ou inativa.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{classId}/members/bulk")
    public ResponseEntity<BulkAddMemberResponse> bulkAddMembers(
            @PathVariable UUID classId,
            @Valid @RequestBody BulkAddMembersRequest request
    ) {
        List<ClassMembershipEntity> membership = bulkAddClassMembersUseCase.execute(request.toCommand(classId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BulkAddMemberResponse.from(membership));
    }


    @Operation(
            summary = "Consulta as turmas ativas de um usuário",
            description = "Retorna os vínculos de turma ativos de um usuário com papel STUDENT ou REPRESENTATIVE. "
                    + "Turmas desativadas ou removidas não são retornadas.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turmas ativas encontradas.",
                    content = @Content(schema = @Schema(implementation = ClassMembershipResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuário inexistente, inativo, removido ou sem turma ativa elegível.",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ClassMembershipResponse>> getActiveClass(
            @Parameter(description = "Identificador único do usuário.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID userId
    ) {
        List<ClassMembershipEntity> memberships = getActiveClassByUserUseCase.execute(new GetActiveClassByUserCommand(userId));

        List<ClassMembershipResponse> response = memberships.stream()
                .map(ClassMembershipResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}