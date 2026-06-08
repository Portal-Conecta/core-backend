package com.portal.conecta.hub.module.room.application.command;

import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;

import java.util.UUID;

public record RemoveRoomCommand(UUID roomId) {
    public RemoveRoomCommand{
        if (roomId == null)
            throw new InvalidRoomDataException("roomId is required.");
    }


}
