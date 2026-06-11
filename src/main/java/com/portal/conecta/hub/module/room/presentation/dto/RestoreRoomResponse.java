package com.portal.conecta.hub.module.room.presentation.dto;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;

import java.time.Instant;
import java.util.UUID;

public record RestoreRoomResponse(
        UUID id,
        Integer number,
        TypeRoom typeRoom,
        Instant deletedAt
) {
    public static RestoreRoomResponse from(RoomEntity room) {
        return new RestoreRoomResponse(
                room.getId(),
                room.getNumber(),
                room.getTypeRoom(),
                room.getDeletedAt()
        );
    }
}