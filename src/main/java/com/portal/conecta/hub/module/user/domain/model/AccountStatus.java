package com.portal.conecta.hub.module.user.domain.model;

/**
 * Estados persistidos do ciclo de vida de uma conta de usuario.
 *
 * <p>O status separa ativacao, operacao normal, desativacao historica e
 * remocao logica com janela de purge.</p>
 */
public enum AccountStatus {
    PENDING_ACTIVATION,
    ACTIVE,
    DISABLED,
    PENDING_DELETION
}
