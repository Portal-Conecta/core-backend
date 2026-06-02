package com.portal.conecta.hub.module.room.application.command;

import com.portal.conecta.hub.module.room.domain.model.TypeRoom;
import org.springframework.stereotype.Component;

import java.util.UUID;

public record UpdateRoomCommand(UUID roomId, Integer number, TypeRoom typeRoom) {
}
