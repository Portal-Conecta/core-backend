package com.portal.conecta.hub.module.me.presentation.controller;

import com.portal.conecta.hub.module.me.application.use_case.GetMyCoursesUseCase;
import com.portal.conecta.hub.module.me.presentation.dto.MyListCourseResponse;
import com.portal.conecta.hub.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.portal.conecta.hub.module.me.application.use_case.GetMeUseCase;
import com.portal.conecta.hub.module.me.presentation.dto.MyProfileResponse;

/**
 * Controlador REST responsável por expor os dados contextuais do próprio usuário autenticado.
 * <p>
 * Atua como uma fachada de leitura para o perfil logado, fornecendo endpoints que
 * recuperam informações acadêmicas, cursos e turmas vinculados, sem expor operações
 * de escrita ou cadastros gerais de outros módulos.
 * </p>
 * <p>
 * <b>Nota importante de segurança:</b> Este controlador não realiza a autenticação do usuário.
 * Ele consome o contexto de segurança previamente validado e injetado na requisição (Token JWT).
 * </p>
 */
@Tag(name = "Meu Perfil", description = "Operações relacionadas ao contexto e vínculos do usuário autenticado.")
@RestController
@RequestMapping("/me")
public class MeController {

    private final GetMyCoursesUseCase getMyCoursesUseCase;
    private final GetMeUseCase getMeUseCase;

    public MeController(GetMyCoursesUseCase getMyCoursesUseCase, GetMeUseCase getMeUseCase) {
        this.getMyCoursesUseCase = getMyCoursesUseCase;
        this.getMeUseCase = getMeUseCase;
    }

    @Operation(
            summary = "Consulta o perfil do usuário autenticado",
            description = "Retorna os dados principais do usuário autenticado. O usuário é identificado automaticamente pelo token JWT, sem necessidade de enviar ID na requisição. A resposta não inclui senha, dados administrativos, auditoria ou vínculos acadêmicos.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil retornado com sucesso.",
                    content = @Content(schema = @Schema(implementation = MyProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário autenticado não encontrado na base de dados.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })

    @GetMapping
    public ResponseEntity<MyProfileResponse> getMe() {
        return ResponseEntity.ok(getMeUseCase.execute());
    }

    @Operation(
            summary = "Consulta os próprios vínculos acadêmicos",
            description = "Retorna os cursos e turmas vinculados ao usuário autenticado. O usuário é identificado automaticamente pelo token JWT, sem necessidade de enviar ID na requisição. A resposta exibe o papel contextual do usuário em cada turma, sem expor dados de outros membros. Caso não possua vínculos, retorna a estrutura com courses: [].",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vínculos retornados com sucesso.",
                    content = @Content(schema = @Schema(implementation = MyListCourseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Autenticação ausente ou inválida.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuário autenticado não encontrado na base de dados.",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/courses")
    public ResponseEntity<MyListCourseResponse> getMyCourses() {
        return ResponseEntity.ok(getMyCoursesUseCase.execute());
    }
}
