package com.portal.conecta.hub.module.classes.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BulkClassRequest(
        @NotEmpty(message = "ids is required. ")
        List<@NotNull UUID> ids
) {
}
