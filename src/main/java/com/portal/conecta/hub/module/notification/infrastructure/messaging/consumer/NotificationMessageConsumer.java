package com.portal.conecta.hub.module.notification.infrastructure.messaging.consumer;

import com.portal.conecta.hub.module.notification.application.use_case.ProcessNotificationRequestUseCase;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.NotificationMessagePayload;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Consumidor RabbitMQ responsável por receber solicitações externas de notificação.
 *
 * <p>A mensagem recebida deve obedecer ao contrato de {@link NotificationMessagePayload}.
 * Este consumidor apenas valida e traduz o payload para o comando de aplicação; a criação
 * da notificação e a resolução dos destinatários são responsabilidade do use case.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
@Validated
public class NotificationMessageConsumer {

    private final ProcessNotificationRequestUseCase processNotificationRequestUseCase;

    public NotificationMessageConsumer(ProcessNotificationRequestUseCase processNotificationRequestUseCase) {
        this.processNotificationRequestUseCase = processNotificationRequestUseCase;
    }

    /**
     * Processa uma mensagem da fila configurada em {@code app.rabbitmq.notifications.queue}.
     *
     * @param payload payload validado da solicitação de notificação.
     * @throws com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException
     * quando o payload não atende ao contrato esperado pela aplicação.
     */
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
