package com.portal.conecta.hub.module.user.application.command;

import java.util.UUID;

/**
 * Comando para solicitar a desativacao operacional de uma conta ativa.
 *
 * @param targetUserId identificador da conta que sera desativada.
 */
public record DeactivateUserCommand(UUID targetUserId) {
}
