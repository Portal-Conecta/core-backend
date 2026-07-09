package com.portal.conecta.hub.module.classes.domain.exception;

/**
 * Lançada quando os dados fornecidos para criação ou alteração de uma turma
 * violam regras de domínio não cobertas por validação de payload.
 */
public class InvalidClassDataException extends RuntimeException {
    public InvalidClassDataException(String message) {
        super(message);
    }
}
