package com.portal.conecta.hub.module.classes.presentation.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record BulkClassesRequest(
        @NotEmpty List<UUID> ids,
        @Schema(description = "Inclui turmas desativadas no resultado quando true", defaultValue = "false")
        Boolean includeInactive) {

    public boolean includeInactiveOrDefault() {
        return Boolean.TRUE.equals(includeInactive);
    }
}
