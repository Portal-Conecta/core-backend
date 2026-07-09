package com.portal.conecta.hub.module.classes.infrastructure.messaging.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura o exchange RabbitMQ para eventos de turma.
 *
 * <p>Declara um {@link TopicExchange} durável e não auto-deletável,
 * com nome definido em {@code app.rabbitmq.class-events.exchange}.</p>
 */
@Configuration
@EnableConfigurationProperties(ClassEventRabbitMqProperties.class)
public class ClassEventRabbitMqConfig {

    private final ClassEventRabbitMqProperties properties;

    public ClassEventRabbitMqConfig(ClassEventRabbitMqProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TopicExchange classEventsExchange() {
        return new TopicExchange(properties.exchange(), true, false);
    }
}