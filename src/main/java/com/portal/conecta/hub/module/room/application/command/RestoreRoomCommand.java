package com.portal.conecta.hub.module.room.application.command;

import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;

import java.util.UUID;

public record RestoreRoomCommand(UUID roomId) {
    public RestoreRoomCommand {
        if (roomId == null)
            throw new InvalidRoomDataException("O ID da sala é obrigatório.");
    }
}
