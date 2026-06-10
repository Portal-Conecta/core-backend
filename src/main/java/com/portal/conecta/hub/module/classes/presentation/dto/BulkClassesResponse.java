package com.portal.conecta.hub.module.classes.presentation.dto;

import java.util.List;
import java.util.UUID;

public record BulkClassesResponse(
        List<BulkClassItemResponse> items,
        List<UUID> foundIds,
        List<UUID> missingIds) {
}
