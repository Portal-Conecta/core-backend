package com.portal.conecta.hub.module.room.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(

        @Schema(description = "Número identificador da sala.", example = "101")
        @NotNull(message = "number is required.")
        Integer number,

        @Schema(description = "Tipo da sala. Valores aceitos: classroom, laboratory, auditorium, other.", example = "classroom")
        @NotNull(message = "type is required.")
        String type
) {
}
