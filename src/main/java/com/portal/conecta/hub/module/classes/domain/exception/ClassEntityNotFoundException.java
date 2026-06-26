package com.portal.conecta.hub.module.classes.domain.exception;

/**
 * Lançada quando uma turma não é encontrada pelo identificador informado,
 * ou quando está removida logicamente e o fluxo exige uma turma ativa.
 */
public class ClassEntityNotFoundException extends RuntimeException {
    public ClassEntityNotFoundException() {
        super("Turma não encontrada. ");
    }
}
