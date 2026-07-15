package com.portal.conecta.hub.module.user.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BulkUserRequest(
        @Schema(description = "IDs dos usuários consultados em lote. IDs duplicados são deduplicados antes da consulta.")
        @NotEmpty(message = "Os IDs são obrigatórios.")
        List<@NotNull UUID> ids,

        @Schema(description = "Quando true, inclui contas pendentes de ativação desde que não tenham sido removidas. O padrão false mantém apenas usuários ativos.", example = "true")
        Boolean includePending
) {
}
