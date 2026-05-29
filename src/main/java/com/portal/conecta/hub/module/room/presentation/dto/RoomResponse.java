package com.portal.conecta.hub.module.room.presentation.dto;

import com.portal.conecta.hub.module.room.domain.model.RoomEntity;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;

import java.util.UUID;

public record RoomResponse(
        UUID id,
        Integer number,
        TypeRoom typeRoom,
        String status
) {

    public static RoomResponse from (RoomEntity entity){
        return new RoomResponse(
                entity.getId(),
                entity.getNumber(),
                entity.getTypeRoom(),
                entity.getDeletedAt() == null ? "active" : "inactive"
        );
    }
}
