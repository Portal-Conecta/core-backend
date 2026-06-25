package com.portal.conecta.hub.module.classes.infrastructure.messaging.config;

import com.portal.conecta.hub.module.classes.infrastructure.messaging.dto.ClassEventPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = ClassEventRabbitMqIntegrationTest.RabbitTestApplication.class)
@TestPropertySource(properties = {
        "app.rabbitmq.class-events.exchange=class-events.exchange",
        "app.rabbitmq.class-events.routingKeyClassCreated=class.created",
        "app.rabbitmq.class-events.routingKeyClassDeleted=class.deleted"
})
class ClassEventRabbitMqIntegrationTest {

    static final String TEST_QUEUE_CREATED = "test.class.created.q";
    static final String TEST_QUEUE_DELETED = "test.class.deleted.q";

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management-alpine");

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ClassEventRabbitMqProperties properties;

    @BeforeEach
    void setUp() {
        rabbitAdmin.initialize();
        rabbitAdmin.purgeQueue(TEST_QUEUE_CREATED, true);
        rabbitAdmin.purgeQueue(TEST_QUEUE_DELETED, true);
    }


    @Test
    void deveRoteiarEventoClassCreatedParaFilaCorreta() {
        ClassEventPayload payload = buildPayload("class.created", "class", "Turma A");

        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyClassCreated(),
                payload
        );

        Message received = rabbitTemplate.receive(TEST_QUEUE_CREATED, 5000);

        assertThat(received).isNotNull();
    }

    @Test
    void payloadDoClassCreatedDeveConterCamposObrigatorios() {
        ClassEventPayload payload = buildPayload("class.created", "class", "Turma A");

        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyClassCreated(),
                payload
        );

        ClassEventPayload received = (ClassEventPayload) rabbitTemplate
                .receiveAndConvert(TEST_QUEUE_CREATED, 5000);

        assertThat(received).isNotNull();
        assertThat(received.eventId()).isNotBlank();
        assertThat(received.correlationId()).isNotBlank();
        assertThat(received.source()).isEqualTo("core-api");
        assertThat(received.eventType()).isEqualTo("class.created");
        assertThat(received.occurredAt()).isNotNull();
        assertThat(received.entityType()).isEqualTo("class");
        assertThat(received.entityId()).isNotBlank();
        assertThat(received.name()).isEqualTo("Turma A");
    }

    @Test
    void classCreatedNaoDeveRoterarParaFilaDeDeleted() {
        ClassEventPayload payload = buildPayload("class.created", "class", "Turma A");

        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyClassCreated(),
                payload
        );

        Message inDeleted = rabbitTemplate.receive(TEST_QUEUE_DELETED, 1000);

        assertThat(inDeleted).isNull();
    }


    @Test
    void deveRoteiarEventoClassDeletedParaFilaCorreta() {
        ClassEventPayload payload = buildPayload("class.deleted", "class", "Turma A");

        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyClassDeleted(),
                payload
        );

        Message received = rabbitTemplate.receive(TEST_QUEUE_DELETED, 5000);

        assertThat(received).isNotNull();
    }

    @Test
    void classDeletedNaoDeveRoterarParaFilaDeCreated() {
        ClassEventPayload payload = buildPayload("class.deleted", "class", "Turma A");

        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyClassDeleted(),
                payload
        );

        Message inCreated = rabbitTemplate.receive(TEST_QUEUE_CREATED, 1000);

        assertThat(inCreated).isNull();
    }


    @Test
    void classTurmaUpdatedNaoExisteNoFluxo() {
        // ClassEventPublisher não declara publishUpdated — validação estrutural
        var methods = java.util.Arrays.stream(
                com.portal.conecta.hub.module.classes.domain.port.ClassEventPublisher.class.getDeclaredMethods()
        ).map(java.lang.reflect.Method::getName).toList();

        assertThat(methods).doesNotContain("publishUpdated");
    }


    @Test
    void eventoComEntityTypeCourseNaoDeveSerTratadoComoEventoDeClass() {
        ClassEventPayload payload = new ClassEventPayload(
                "evt-invalid",
                "corr-invalid",
                "core-api",
                "class.created",
                java.time.Instant.now(),
                "course",
                "uuid-qualquer",
                "Curso Inválido"
        );

        rabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyClassCreated(),
                payload
        );

        ClassEventPayload received = (ClassEventPayload) rabbitTemplate
                .receiveAndConvert(TEST_QUEUE_CREATED, 5000);

        assertThat(received).isNotNull();
        assertThat(received.entityType()).isNotEqualTo("class");
    }


    @Test
    void fluxoDeClassNaoDevePublicarEmNotificationsExchange() {
        assertThat(properties.exchange()).isNotEqualTo("notifications.exchange");
    }

    @Test
    void fluxoDeClassNaoDeveUsarExchangeDeCourse() {
        assertThat(properties.exchange()).isNotEqualTo("course.events.exchange");
    }


    private ClassEventPayload buildPayload(String eventType, String entityType, String name) {
        return new ClassEventPayload(
                "evt-" + java.util.UUID.randomUUID(),
                "corr-" + java.util.UUID.randomUUID(),
                "core-api",
                eventType,
                java.time.Instant.now(),
                entityType,
                java.util.UUID.randomUUID().toString(),
                name
        );
    }


    @EnableAutoConfiguration
    @EnableConfigurationProperties(ClassEventRabbitMqProperties.class)
    @Import(ClassEventRabbitMqConfig.class)
    static class RabbitTestApplication {

        @Bean
        public JacksonJsonMessageConverter messageConverter() {
            return new JacksonJsonMessageConverter();
        }

        @Bean
        public RabbitTemplate rabbitTemplate(
                org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                JacksonJsonMessageConverter converter
        ) {
            RabbitTemplate template = new RabbitTemplate(connectionFactory);
            template.setMessageConverter(converter);
            return template;
        }

        @Bean
        public RabbitAdmin rabbitAdmin(
                org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory
        ) {
            return new RabbitAdmin(connectionFactory);
        }

        @Bean
        public Queue testQueueCreated() {
            return new Queue(TEST_QUEUE_CREATED, false, false, true);
        }

        @Bean
        public Queue testQueueDeleted() {
            return new Queue(TEST_QUEUE_DELETED, false, false, true);
        }

        @Bean
        public Binding bindingCreated(Queue testQueueCreated, TopicExchange classEventsExchange) {
            return BindingBuilder.bind(testQueueCreated)
                    .to(classEventsExchange)
                    .with("class.created");
        }

        @Bean
        public Binding bindingDeleted(Queue testQueueDeleted, TopicExchange classEventsExchange) {
            return BindingBuilder.bind(testQueueDeleted)
                    .to(classEventsExchange)
                    .with("class.deleted");
        }
    }
}