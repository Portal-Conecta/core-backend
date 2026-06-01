package com.portal.conecta.hub.module.classes.presentation.dto.request;

import com.portal.conecta.hub.module.classes.domain.model.Shift;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Payload para criação de uma nova turma.")
public record CreateClassRequest(

        @Schema(
                description = "Turno em que a turma será ministrada.",
                example = "FULL_AM_PM"
        )
        Shift shift,

        @Schema(
                description = "Identificador único do curso ao qual a nova turma será vinculada.",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID courseId
) {
}