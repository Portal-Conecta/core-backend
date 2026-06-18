package com.portal.conecta.hub.module.notification.infrastructure.messaging.consumer;

import com.portal.conecta.hub.module.notification.application.usecase.ProcessNotificationRequestUseCase;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.NotificationRequestPayload;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class NotificationMessageConsumer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(NotificationMessageConsumer.class);

    private final ProcessNotificationRequestUseCase processNotificationRequestUseCase;

    public NotificationMessageConsumer(ProcessNotificationRequestUseCase processNotificationRequestUseCase) {
        this.processNotificationRequestUseCase = processNotificationRequestUseCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void consume(@Valid @Payload NotificationRequestPayload payload) {
        LOGGER.info(
                "Recebida solicitação de notificação. messageId={}, correlationId={}, source={}",
                payload.messageId(),
                payload.correlationId(),
                payload.source()
        );

        var command = payload.toCommand();
        processNotificationRequestUseCase.execute(command);

        LOGGER.info(
                "Notificação processada com sucesso. messageId={}",
                payload.messageId()
        );
    }
}