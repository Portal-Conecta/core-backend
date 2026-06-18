package com.portal.conecta.hub.module.notification.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
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
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
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

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
