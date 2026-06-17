package com.portal.conecta.hub.module.user.domain.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("E-mail já está em uso: " +email);
    }
}
