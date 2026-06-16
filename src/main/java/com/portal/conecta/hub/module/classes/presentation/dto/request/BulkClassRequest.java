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
                description = "Quando true, turmas ativas e inativas são retornadas. Turmas deletadas nunca são retornadas. Padrão: false.",
                defaultValue = "false"
        )
        Boolean includeInactive
) {
}
