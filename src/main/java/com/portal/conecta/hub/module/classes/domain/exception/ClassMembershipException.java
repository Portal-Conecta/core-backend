package com.portal.conecta.hub.module.classes.domain.exception;

/**
 * Lançada para violações gerais de regra de vínculo de membro com a turma.
 *
 * <p>Cobre cenários como turma excluída recebendo novos membros, duplicidade
 * de usuário na requisição em lote e ausência de vaga de representante.</p>
 */
public class ClassMembershipException extends RuntimeException {
    public ClassMembershipException(String message) {
        super(message);
    }
}
