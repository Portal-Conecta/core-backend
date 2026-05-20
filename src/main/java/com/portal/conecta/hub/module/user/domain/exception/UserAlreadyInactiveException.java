package com.portal.conecta.hub.module.user.domain.exception;

public class UserAlreadyInactiveException extends RuntimeException {
    public UserAlreadyInactiveException(String message) {super(message);}

    public UserAlreadyInactiveException () {super ("User already inactive.");}
}
