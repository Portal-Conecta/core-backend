package com.portal.conecta.hub.module.classes.presentation.dto;

import java.util.UUID;

public record BulkClassItemResponse(
        UUID id,
        boolean active) {
}
