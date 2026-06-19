package com.portal.conecta.hub.module.classes.domain.exception;

public class ActiveClassNotFoundException extends RuntimeException {
    public ActiveClassNotFoundException(String message) {super(message);
    }

    public ActiveClassNotFoundException() {
        super("Nenhuma turma ativa encontrada para o usuário informado.");
    }
}
