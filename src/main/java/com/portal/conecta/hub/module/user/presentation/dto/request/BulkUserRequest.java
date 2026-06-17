package com.portal.conecta.hub.module.user.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BulkUserRequest(
        @NotEmpty(message = "Os IDs são obrigatórios.")
        List<@NotNull UUID> ids
) {
}