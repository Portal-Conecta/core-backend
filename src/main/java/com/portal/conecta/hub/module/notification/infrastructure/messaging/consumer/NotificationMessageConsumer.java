package com.portal.conecta.hub.module.notification.infrastructure.messaging.consumer;

import com.portal.conecta.hub.module.notification.application.use_case.ProcessNotificationRequestUseCase;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.NotificationMessagePayload;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Validated
public class NotificationMessageConsumer {

    private final ProcessNotificationRequestUseCase processNotificationRequestUseCase;

    public NotificationMessageConsumer(ProcessNotificationRequestUseCase processNotificationRequestUseCase) {
        this.processNotificationRequestUseCase = processNotificationRequestUseCase;
    }

    @RabbitListener(queues = "${app.rabbitmq.notifications.queue}")
    public void consume(@Valid @Payload NotificationMessagePayload payload) {
        log.info(
                "Notificação recebida para processamento. messageId={}, source={}",
                payload.messageId(),
                payload.source()
        );

        var command = payload.toCommand();
        processNotificationRequestUseCase.execute(command);

        log.info(
                "Notificação processada com sucesso. messageId={}",
                payload.messageId()
        );
    }
}