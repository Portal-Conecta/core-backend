package com.portal.conecta.hub.module.classes.domain.exception;

/**
 * Lançada quando o vínculo entre um usuário e uma turma não é encontrado.
 *
 * <p>Utilizada em operações que exigem matrícula ativa existente,
 * como promoção, rebaixamento ou remoção de membro.</p>
 */
public class ClassMembershipNotFoundException extends RuntimeException {
    public ClassMembershipNotFoundException(String message) {
        super(message);
    }
}
