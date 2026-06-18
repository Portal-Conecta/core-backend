package com.portal.conecta.hub.module.notification.infrastructure.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "app.rabbitmq.exchange=notifications.exchange",
        "app.rabbitmq.queue=notifications.dispatch.q",
        "app.rabbitmq.dlq=notifications.dispatch.dlq",
        "app.rabbitmq.routing-key=notification.requested"
})
class NotificationRabbitMqConfigTest {

    @Autowired
    private TopicExchange notificationsExchange;

    @Autowired
    private Queue notificationsQueue;

    @Autowired
    private Queue notificationsDlq;

    @Autowired
    private Binding notificationsBinding;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Test
    void shouldDeclareNotificationsExchangeAsTopic() {
        assertThat(notificationsExchange.getName()).isEqualTo("notifications.exchange");
        assertThat(notificationsExchange.isDurable()).isTrue();
    }

    @Test
    void shouldDeclareNotificationsQueueWithDeadLetterArgs() {
        assertThat(notificationsQueue.getName()).isEqualTo("notifications.dispatch.q");
        assertThat(notificationsQueue.isDurable()).isTrue();
        assertThat(notificationsQueue.getArguments())
                .containsEntry("x-dead-letter-exchange", "")
                .containsEntry("x-dead-letter-routing-key", "notifications.dispatch.dlq");
    }

    @Test
    void shouldDeclareDeadLetterQueue() {
        assertThat(notificationsDlq.getName()).isEqualTo("notifications.dispatch.dlq");
        assertThat(notificationsDlq.isDurable()).isTrue();
    }

    @Test
    void shouldBindQueueToExchangeWithRoutingKey() {
        assertThat(notificationsBinding.getExchange()).isEqualTo("notifications.exchange");
        assertThat(notificationsBinding.getDestination()).isEqualTo("notifications.dispatch.q");
        assertThat(notificationsBinding.getRoutingKey()).isEqualTo("notification.requested");
    }

    @Test
    void shouldConfigureRabbitTemplateWithJsonConverter() {
        assertThat(rabbitTemplate.getMessageConverter())
                .isInstanceOf(JacksonJsonMessageConverter.class);
    }

    @Test
    void shouldRegisterRabbitAdmin() {
        assertThat(rabbitAdmin).isNotNull();
    }
}