package com.portal.conecta.hub.module.room.presentation.dto;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;

import java.time.Instant;
import java.util.UUID;

public record CreateRoomResponse(
        UUID id,
        Integer number,
        TypeRoom type,
        String status,
        Instant createdAt
) {

    public static CreateRoomResponse from(RoomEntity room) {
        return new CreateRoomResponse(
                room.getId(),
                room.getNumber(),
                room.getTypeRoom(),
                room.getDeletedAt() == null ? "active" : "inactive",
                room.getCreatedAt()
        );
    }
}
