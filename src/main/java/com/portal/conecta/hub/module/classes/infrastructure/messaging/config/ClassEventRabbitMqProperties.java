package com.portal.conecta.hub.module.classes.infrastructure.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq.class-events")
public record ClassEventRabbitMqProperties(
        String exchange,
        String routingKeyClassCreated,
        String routingKeyClassDeleted
) {
}
