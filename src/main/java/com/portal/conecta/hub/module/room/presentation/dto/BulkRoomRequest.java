package com.portal.conecta.hub.module.room.presentation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BulkRoomRequest(
        @NotEmpty(message = "Os IDs são obrigatórios.")
        List<@NotNull UUID> ids
) {
}
