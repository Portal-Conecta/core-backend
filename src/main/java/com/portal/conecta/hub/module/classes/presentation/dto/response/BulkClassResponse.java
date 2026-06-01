package com.portal.conecta.hub.module.classes.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record BulkClassResponse(
        List<ClassResponse> items,
        List<UUID> foundIds,
        List<UUID> missingIds
) {
}
