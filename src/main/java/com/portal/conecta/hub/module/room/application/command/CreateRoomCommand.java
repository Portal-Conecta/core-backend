package com.portal.conecta.hub.module.room.application.command;

import com.portal.conecta.hub.module.room.domain.model.TypeRoom;

public record CreateRoomCommand(Integer number, TypeRoom typeRoom) {
}
