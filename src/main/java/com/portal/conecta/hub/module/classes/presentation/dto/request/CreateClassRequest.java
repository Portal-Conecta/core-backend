package com.portal.conecta.hub.module.classes.presentation.dto.request;

import com.portal.conecta.hub.module.classes.domain.model.Shift;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Payload para criação de uma nova turma.")
public record CreateClassRequest(

        @NotNull(message = "O turno é obrigatório.")
        @Schema(
                description = "Turno em que a turma será ministrada.",
                example = "FULL_AM_PM"
        )
        Shift shift,

        @NotNull(message = "O curso é obrigatório.")
        @Schema(
                description = "Identificador único do curso ao qual a nova turma será vinculada.",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID courseId,

        @NotNull(message = "O número da turma é obrigatório.")
        @Min(value = 1, message = "O número da turma deve ser maior que zero.")
        @Schema(
                description = "Número da turma, representando a leva atual do curso.",
                example = "78"
        )
        Integer number
) {
}