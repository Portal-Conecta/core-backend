package com.portal.conecta.hub.module.room.application.command;

import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;

import java.util.UUID;

public record UpdateRoomCommand(UUID roomId, Integer number, TypeRoom typeRoom) {
    public UpdateRoomCommand {
        if (number == null && typeRoom == null) {
            throw new InvalidRoomDataException("Pelo menos um campo deve ser informado.");
        }
    }
}
