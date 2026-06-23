package com.portal.conecta.hub.module.notification.infrastructure.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq.notifications")
public record NotificationRabbitMqProperties(
        String exchange,
        String queue,
        String dlq,
        String routingKey
) {
}
