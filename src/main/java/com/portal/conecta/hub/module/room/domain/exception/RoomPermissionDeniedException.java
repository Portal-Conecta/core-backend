package com.portal.conecta.hub.module.room.domain.exception;

public class RoomPermissionDeniedException extends RuntimeException {
    public RoomPermissionDeniedException() {
        super("Usuário não tem permissão para criar uma sala.");
    }
}
