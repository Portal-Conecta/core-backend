package com.portal.conecta.hub.module.classes.presentation.controller;

import java.util.UUID;

import com.portal.conecta.hub.module.classes.application.command.GetActiveClassByUserCommand;
import com.portal.conecta.hub.module.classes.application.use_case.GetActiveClassByUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{userId}/class")
@Tag(name = "Users", description = "Operações relacionadas a usuários")
public class UserActiveClassController {

    private final GetActiveClassByUserUseCase getActiveClassByUserUseCase;

    public UserActiveClassController(GetActiveClassByUserUseCase getActiveClassByUserUseCase) {
        this.getActiveClassByUserUseCase = getActiveClassByUserUseCase;
    }

    @GetMapping
    @Operation(
            summary = "Consulta a turma ativa de um usuário",
            description = "Retorna o identificador (UUID) da turma ativa vinculada a um usuário com papel "
                    + "STUDENT ou REPRESENTATIVE no contexto da turma. Um usuário possui, no máximo, "
                    + "uma turma ativa elegível."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turma ativa encontrada"),
            @ApiResponse(responseCode = "401", description = "Requisição sem autenticação válida"),
            @ApiResponse(responseCode = "404", description = "Usuário inexistente, indisponível ou sem turma ativa elegível")
    })
    public ResponseEntity<UUID> getActiveClass(@PathVariable UUID userId) {
        UUID classId = getActiveClassByUserUseCase.execute(new GetActiveClassByUserCommand(userId));
        return ResponseEntity.ok(classId);
    }
}