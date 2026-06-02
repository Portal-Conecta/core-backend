package com.portal.conecta.hub.module.room.presentation.mapper;

import com.portal.conecta.hub.module.room.application.command.CreateRoomCommand;
import com.portal.conecta.hub.module.room.presentation.dto.CreateRoomRequest;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public CreateRoomCommand toCommand(CreateRoomRequest request) {
        return new CreateRoomCommand(request.number(), request.type());
    }
}
