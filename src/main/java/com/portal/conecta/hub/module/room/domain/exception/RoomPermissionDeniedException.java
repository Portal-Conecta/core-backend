package com.portal.conecta.hub.module.room.domain.exception;

public class RoomPermissionDeniedException extends RuntimeException {
    public RoomPermissionDeniedException() {
        super("User does not have permission to create a room.");
    }
}
