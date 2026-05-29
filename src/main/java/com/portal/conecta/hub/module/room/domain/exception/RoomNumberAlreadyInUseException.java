package com.portal.conecta.hub.module.room.domain.exception;

public class RoomNumberAlreadyInUseException extends RuntimeException {
    public RoomNumberAlreadyInUseException(Integer number) {
        super("Room number '" + number + "' is already in use.");
    }
}
