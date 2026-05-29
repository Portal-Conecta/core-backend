package com.portal.conecta.hub.module.classes.presentation.dto;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Payload para vincular um usuário a uma turma.")
public record AddMemberRequest(

        @Schema(
                description = "Identificador único do usuário que será adicionado à turma.",
                example = "987e6543-e21b-34d5-c678-426614174999"
        )
        @NotNull
        UUID userId,

        @Schema(
                description = "Papel que o usuário exercerá na turma (ex: STUDENT, TEACHER).",
                example = "STUDENT"
        )
        @NotNull
        ClassRole classRole
) {
}
