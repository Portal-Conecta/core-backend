package com.portal.conecta.hub.module.room.domain.exception;

public class RoomNumberAlreadyInUseException extends RuntimeException {
    public RoomNumberAlreadyInUseException(Integer number) {
        super("O número da sala '" + number + "' já está em uso.");
    }
}
