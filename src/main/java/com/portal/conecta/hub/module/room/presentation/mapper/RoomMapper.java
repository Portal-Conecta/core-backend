package com.portal.conecta.hub.module.room.presentation.mapper;

import com.portal.conecta.hub.module.room.application.command.CreateRoomCommand;
import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomRequest;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public CreateRoomCommand toCommand(CreateRoomRequest request) {
        TypeRoom typeRoom = TypeRoom.fromApiValue(request.type());
        if (typeRoom == null) {
            throw new InvalidRoomDataException("type '" + request.type() + "' is not valid.");
        }
        return new CreateRoomCommand(request.number(), typeRoom);
    }
}
