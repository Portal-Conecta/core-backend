package com.portal.conecta.hub.module.notification.infrastructure.messaging.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("NotificationRabbitMqConfig")
class NotificationRabbitMqConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(NotificationRabbitMqConfig.class, TestConnectionFactoryConfig.class)
            .withBean(NotificationRabbitMqProperties.class, () -> new NotificationRabbitMqProperties(
                    "notifications.exchange",
                    "notifications.dispatch.q",
                    "notifications.dispatch.dlq",
                    "notification.requested"
            ));

    @Test
    @DisplayName("deve declarar exchange com nome, durável e sem auto-delete")
    void deveDeclaraExchangeCorretamente() {
        contextRunner.run(context -> {
            TopicExchange exchange = context.getBean("notificationsExchange", TopicExchange.class);

            assertThat(exchange.getName()).isEqualTo("notifications.exchange");
            assertThat(exchange.isDurable()).isTrue();
            assertThat(exchange.isAutoDelete()).isFalse();
        });
    }

    @Test
    @DisplayName("deve declarar fila principal com DLQ configurada")
    void deveDeclaraFilaPrincipalComDlq() {
        contextRunner.run(context -> {
            Queue queue = context.getBean("notificationsQueue", Queue.class);

            assertThat(queue.getName()).isEqualTo("notifications.dispatch.q");
            assertThat(queue.getArguments())
                    .containsEntry("x-dead-letter-exchange", "")
                    .containsEntry("x-dead-letter-routing-key", "notifications.dispatch.dlq");
        });
    }

    @Test
    @DisplayName("deve declarar DLQ como fila durável simples")
    void deveDeclaraDlqCorretamente() {
        contextRunner.run(context -> {
            Queue dlq = context.getBean("notificationsDlq", Queue.class);

            assertThat(dlq.getName()).isEqualTo("notifications.dispatch.dlq");
        });
    }

    @Test
    @DisplayName("deve declarar binding ligando fila à exchange com routing key correta")
    void deveDeclaraBindingCorretamente() {
        contextRunner.run(context -> {
            Binding binding = context.getBean("notificationsBinding", Binding.class);

            assertThat(binding.getExchange()).isEqualTo("notifications.exchange");
            assertThat(binding.getDestination()).isEqualTo("notifications.dispatch.q");
            assertThat(binding.getRoutingKey()).isEqualTo("notification.requested");
        });
    }

    @Test
    @DisplayName("não deve declarar RabbitTemplate, RabbitAdmin nem messageConverter — esses pertencem ao shared")
    void naoDeveDeclaraInfraestrutura() {
        contextRunner.run(context -> {
            assertThat(context.containsBean("rabbitTemplate")).isFalse();
            assertThat(context.containsBean("rabbitAdmin")).isFalse();
            assertThat(context.containsBean("messageConverter")).isFalse();
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