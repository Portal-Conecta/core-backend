package com.portal.conecta.hub.module.user.application.command;

import java.util.UUID;

/**
 * Comando para solicitar a remocao logica de uma conta de usuario.
 *
 * @param targetUserId identificador da conta que sera marcada para exclusao.
 */
public record DeleteUserCommand(UUID targetUserId) {
}
