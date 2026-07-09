package com.portal.conecta.hub.module.notification.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationRabbitMqConfig {

    private final NotificationRabbitMqProperties properties;

    public NotificationRabbitMqConfig(NotificationRabbitMqProperties properties) {
        this.properties = properties;
    }


    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(properties.exchange(), true, false);
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(properties.queue())
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", properties.dlq())
                .build();
    }

    @Bean
    public Queue notificationsDlq() {
        return QueueBuilder.durable(properties.dlq()).build();
    }

    @Bean
    public Binding notificationsBinding(
            Queue notificationsQueue,
            TopicExchange notificationsExchange) {

        return BindingBuilder
                .bind(notificationsQueue)
                .to(notificationsExchange)
                .with(properties.routingKey());
    }

}
