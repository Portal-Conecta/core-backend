package com.portal.conecta.hub.module.notification.infrastructure.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq")
public record NotificationRabbitMqProperties(
        Boolean enabled,
        String exchange,
        String queue,
        String dlq,
        String routingKey
) {
}
