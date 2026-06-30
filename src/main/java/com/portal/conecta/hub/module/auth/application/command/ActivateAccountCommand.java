package com.portal.conecta.hub.module.auth.application.command;

/**
 * Command used to activate a pending user account with the token received by e-mail.
 *
 * @param token raw activation token provided by the user
 * @param newPassword password that will be encoded and stored for the activated account
 */
public record ActivateAccountCommand(
        String token,
        String newPassword
) {
}
