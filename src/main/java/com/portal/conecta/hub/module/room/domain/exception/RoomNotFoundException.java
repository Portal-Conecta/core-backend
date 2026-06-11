package com.portal.conecta.hub.module.room.domain.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException() {
        super("Sala não encontrada.");
    }
}
