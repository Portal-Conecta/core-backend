package com.portal.conecta.hub.module.classes.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BulkClassRequest(
        @NotEmpty(message = "ids is required. ")
        List<@NotNull UUID> ids,

        @Schema(
                description = "Quando true, turmas desativadas também são retornadas em items. Padrão: false.",
                defaultValue = "false"
        )
        Boolean includeInactive
) {
}
