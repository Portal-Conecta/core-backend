package com.portal.conecta.hub.module.course.presentation.controller;

import com.portal.conecta.hub.module.course.application.command.CreateCourseCommand;
import com.portal.conecta.hub.module.course.application.command.UpdateCourseCommand;
import com.portal.conecta.hub.module.course.application.use_case.CreateCourseUseCase;
import com.portal.conecta.hub.module.course.application.use_case.UpdateCourseUseCase;
import com.portal.conecta.hub.module.course.application.use_case.GetAllCoursesUseCase;
import com.portal.conecta.hub.module.course.application.use_case.GetCourseByIdUseCase;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.presentation.dto.CourseResponse;
import com.portal.conecta.hub.module.course.presentation.dto.CreateCourseRequest;
import com.portal.conecta.hub.module.course.presentation.dto.CreateCourseResponse;
import com.portal.conecta.hub.module.course.presentation.dto.UpdateCourseRequest;
import com.portal.conecta.hub.module.course.presentation.dto.UpdateCourseResponse;
import com.portal.conecta.hub.module.course.presentation.dto.ListCoursesResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Cursos", description = "Operações para cadastro, edição e consulta de cursos do Hub.")
@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CreateCourseUseCase createCourseUseCase;
    private final UpdateCourseUseCase updateCourseUseCase;
    private final GetAllCoursesUseCase getAllCoursesUseCase;
    private final GetCourseByIdUseCase getCourseByIdUseCase;

    public CourseController(CreateCourseUseCase createCourseUseCase,
                            UpdateCourseUseCase updateCourseUseCase,
                            GetAllCoursesUseCase getAllCoursesUseCase,
                            GetCourseByIdUseCase getCourseByIdUseCase) {
        this.createCourseUseCase = createCourseUseCase;
        this.updateCourseUseCase = updateCourseUseCase;
        this.getAllCoursesUseCase = getAllCoursesUseCase;
        this.getCourseByIdUseCase = getCourseByIdUseCase;
    }

    @Operation(
            summary = "Cria um novo curso",
            description = "Registra um novo curso na plataforma informando nome e código.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Curso criado com sucesso.",
                    content = @Content(schema = @Schema(implementation = CreateCourseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida (ex: payload incorreto ou incompleto).",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário autenticado sem permissão para executar a operação.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    public ResponseEntity<CreateCourseResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para criação do curso.",
                    required = true
            )
          @Valid @RequestBody CreateCourseRequest createCourseRequest
    ) {
        CourseEntity createdCourse = createCourseUseCase.execute(new CreateCourseCommand(
                createCourseRequest.name(),
                createCourseRequest.code()
        ));

        return ResponseEntity.created(URI.create("/courses/" + createdCourse.getId()))
                .body(CreateCourseResponse.from(createdCourse));
    }

    @Operation(
            summary = "Lista todos os cursos",
            description = "Retorna uma lista contendo todos os cursos cadastrados.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de cursos retornada com sucesso.",
                    content = @Content(schema = @Schema(implementation = ListCoursesResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário autenticado sem permissão para executar a operação.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public ResponseEntity<ListCoursesResponse> findAll (){
        return ResponseEntity.ok(
                ListCoursesResponse.from(getAllCoursesUseCase.execute())
        );
    }

    @Operation(
            summary = "Consulta curso pelo ID",
            description = "Retorna os detalhes de um curso específico com base no identificador informado.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Curso encontrado com sucesso.",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário autenticado sem permissão para executar a operação.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado. O curso com o ID informado não existe.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponse> findById (
            @Parameter(description = "Identificador do curso.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID courseId
    ){
        return ResponseEntity.ok(CourseResponse.from(getCourseByIdUseCase.execute(courseId)));
    }

    @Operation(
            summary = "Atualiza dados de um curso",
            description = "Altera as informações de um curso existente. Apenas os campos enviados serão alterados.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Curso atualizado com sucesso.",
                    content = @Content(schema = @Schema(implementation = UpdateCourseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida (ex: regras de validação violadas).",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário autenticado sem permissão para executar a operação.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Recurso não encontrado. O curso com o ID informado não existe.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PatchMapping("/{courseId}")
    public ResponseEntity<UpdateCourseResponse> update(
            @Parameter(description = "Identificador do curso a ser atualizado.", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID courseId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados para atualização do curso.",
                    required = true
            )
            @RequestBody UpdateCourseRequest updateCourseRequest
    ) {
        CourseEntity updatedCourse = updateCourseUseCase.execute(UpdateCourseCommand.of(
                courseId,
                updateCourseRequest.name(),
                updateCourseRequest.code()
        ));

        return ResponseEntity.ok(UpdateCourseResponse.from(updatedCourse));
    }
}