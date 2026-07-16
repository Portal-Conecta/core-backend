package com.portal.conecta.hub.module.user.application.command;

import java.util.UUID;

/**
 * Comando para solicitar a reativacao operacional de uma conta desativada.
 *
 * @param targetUserId identificador da conta que sera reativada.
 */
public record ReactivateUserCommand(UUID targetUserId) {
}
