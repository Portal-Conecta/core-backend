package com.portal.conecta.hub.module.notification.infrastructure.messaging.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class NotificationRabbitMqConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(NotificationRabbitMqConfig.class, TestConnectionFactoryConfig.class)
            .withBean(NotificationRabbitMqProperties.class, () -> new NotificationRabbitMqProperties(
                    true,
                    "notifications.exchange",
                    "notifications.dispatch.q",
                    "notifications.dispatch.dlq",
                    "notification.requested"
            ));

    @Test
    void declaresNotificationTopologyWithoutBrokerConnection() {
        contextRunner.run(context -> {
            TopicExchange exchange = context.getBean("notificationsExchange", TopicExchange.class);
            Queue queue = context.getBean("notificationsQueue", Queue.class);
            Queue dlq = context.getBean("notificationsDlq", Queue.class);
            Binding binding = context.getBean("notificationsBinding", Binding.class);

            assertThat(exchange.getName()).isEqualTo("notifications.exchange");
            assertThat(exchange.isDurable()).isTrue();
            assertThat(exchange.isAutoDelete()).isFalse();

            assertThat(queue.getName()).isEqualTo("notifications.dispatch.q");
            assertThat(queue.getArguments())
                    .containsEntry("x-dead-letter-exchange", "")
                    .containsEntry("x-dead-letter-routing-key", "notifications.dispatch.dlq");

            assertThat(dlq.getName()).isEqualTo("notifications.dispatch.dlq");

            assertThat(binding.getExchange()).isEqualTo("notifications.exchange");
            assertThat(binding.getDestination()).isEqualTo("notifications.dispatch.q");
            assertThat(binding.getRoutingKey()).isEqualTo("notification.requested");
        });
    }

    @Configuration
    static class TestConnectionFactoryConfig {

        @Bean
        ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }
    }
}
