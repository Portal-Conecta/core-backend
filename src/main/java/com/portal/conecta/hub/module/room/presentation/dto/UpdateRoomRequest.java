package com.portal.conecta.hub.module.room.presentation.dto;

import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateRoomRequest(

        @Schema(description = "Número identificador da sala.", example = "204")
        Integer number,

        @Schema(description = "Tipo da sala. Valores aceitos: CLASSROOM, LABORATORY, AUDITORIUM, OTHER", example = "LABORATORY")
        TypeRoom type
) {
}