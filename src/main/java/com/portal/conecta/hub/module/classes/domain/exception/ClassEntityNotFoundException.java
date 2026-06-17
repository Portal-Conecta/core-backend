package com.portal.conecta.hub.module.classes.domain.exception;

public class ClassEntityNotFoundException extends RuntimeException {
    public ClassEntityNotFoundException() {
        super("Turma não encontrada. ");
    }
}
