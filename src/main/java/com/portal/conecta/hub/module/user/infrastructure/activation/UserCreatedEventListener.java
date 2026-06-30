package com.portal.conecta.hub.module.user.infrastructure.activation;

import com.portal.conecta.hub.module.user.application.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends account activation e-mails after user creation transactions are committed.
 */
@Component
@Slf4j
public class UserCreatedEventListener {

    private final AccountActivationEmailService emailService;

    public UserCreatedEventListener(AccountActivationEmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Handles user creation events after commit and requests the activation e-mail delivery.
     *
     * @param event user creation event containing the activation token
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserCreatedEvent event) {
        try {
            emailService.sendActivationEmail(
                    event.name(),
                    event.email(),
                    event.rawActivationToken(),
                    event.expiresAt()
            );
            log.info("E-mail de ativacao solicitado apos commit. targetUserId={}", event.userId());
        } catch (RuntimeException exception) {
            log.warn("Falha ao enviar e-mail de ativacao apos commit. targetUserId={}", event.userId(), exception);
        }
    }
}
