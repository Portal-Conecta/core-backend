package com.portal.conecta.hub.module.room.presentation.dto;

import java.util.List;
import java.util.UUID;

public record BulkRoomResponse(
        List<RoomResponse> items,
        List<UUID> foundIds,
        List<UUID> missingIds
) {
}
