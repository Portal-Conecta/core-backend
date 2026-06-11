package com.portal.conecta.hub.module.user.presentation.dto.response;

import java.util.List;
import java.util.UUID;

public record BulkUserResponse(
        List<UserResponse> items,
        List<UUID> foundIds,
        List<UUID> missingIds
) {
}