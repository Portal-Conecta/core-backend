package com.portal.conecta.hub.module.room.presentation.dto;

import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRoomRequest(

        @Schema(description = "Número identificador da sala.", example = "101")
        @NotNull(message = "O número é obrigatório.")
        @Positive
        Integer number,

        @Schema(description = "Tipo da sala. Valores aceitos: CLASSROOM, ELECTROTECHNICS_LABORATORY, ELECTRONICS_LABORATORY, COMPUTER_LABORATORY, CNC_SIMULATION.", example = "COMPUTER_LABORATORY")
        @NotNull(message = "O tipo é obrigatório.")
        TypeRoom type
) {
}
