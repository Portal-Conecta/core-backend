package com.portal.conecta.hub.module.notification.application.usecase;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessNotificationRequestUseCase {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ProcessNotificationRequestUseCase.class);

    public void execute(ProcessNotificationRequestCommand command) {
        LOGGER.info(
                """
                Mock de processamento de notificação.
                messageId={}
                correlationId={}
                source={}
                eventType={}
                title={}
                """,
                command.messageId(),
                command.correlationId(),
                command.source(),
                command.eventType(),
                command.title()
        );
    }
}