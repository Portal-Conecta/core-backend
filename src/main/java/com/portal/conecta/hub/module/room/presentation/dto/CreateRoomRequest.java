package com.portal.conecta.hub.module.room.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(

        @NotNull(message = "number is required.")
        Integer number,

        @NotNull(message = "type is required.")
        String type
) {
}
