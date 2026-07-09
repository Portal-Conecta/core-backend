package com.portal.conecta.hub.module.notification.infrastructure.messaging.config;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = NotificationRabbitMqIntegrationTest.RabbitTestApplication.class)
@TestPropertySource(properties = {
        "app.rabbitmq.enabled=true",
        "app.rabbitmq.notifications.exchange=notifications.exchange",
        "app.rabbitmq.notifications.queue=notifications.dispatch.q",
        "app.rabbitmq.notifications.dlq=notifications.dispatch.dlq",
        "app.rabbitmq.notifications.routing-key=notification.requested"
})
class NotificationRabbitMqIntegrationTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management-alpine");

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private NotificationRabbitMqProperties properties;

    @BeforeEach
    void setUp() {
        rabbitAdmin.initialize();
        rabbitAdmin.purgeQueue(properties.queue(), false);
        rabbitAdmin.purgeQueue(properties.dlq(), false);
    }

    @Test
    void routesNotificationRequestsToMainQueue() {
        Message message = MessageBuilder
                .withBody("notification-request".getBytes(UTF_8))
                .build();

        rabbitTemplate.send(properties.exchange(), properties.routingKey(), message);

        Message received = rabbitTemplate.receive(properties.queue(), 5000);

        assertThat(received).isNotNull();
        assertThat(new String(received.getBody(), UTF_8)).isEqualTo("notification-request");
    }

    @Test
    void deadLettersExpiredMessagesFromMainQueueToDlq() {
        Message message = MessageBuilder
                .withBody("dead-letter-me".getBytes(UTF_8))
                .setExpiration("100")
                .build();

        rabbitTemplate.send(properties.exchange(), properties.routingKey(), message);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Message deadLetter = rabbitTemplate.receive(properties.dlq(), 1000);

            assertThat(deadLetter).isNotNull();
            assertThat(new String(deadLetter.getBody(), UTF_8)).isEqualTo("dead-letter-me");
        });
    }

    @EnableAutoConfiguration
    @EnableConfigurationProperties(NotificationRabbitMqProperties.class)
    @Import(NotificationRabbitMqConfig.class)
    static class RabbitTestApplication {
    }
}
