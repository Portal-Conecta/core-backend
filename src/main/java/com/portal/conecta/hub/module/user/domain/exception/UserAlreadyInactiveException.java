package com.portal.conecta.hub.module.user.domain.exception;

public class UserAlreadyInactiveException extends RuntimeException {

    public UserAlreadyInactiveException () {super ("Usuário já está inativo.");}
}
