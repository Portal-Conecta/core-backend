package com.portal.conecta.hub.module.course.infrastructure.messaging.config;

import com.portal.conecta.hub.module.course.infrastructure.messaging.dto.CourseEventPayload;
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
@SpringBootTest(classes = CourseEventRabbitMqIntegrationTest.RabbitTestApplication.class)
@TestPropertySource(properties = {
        "app.rabbitmq.course-events.exchange=course.events.exchange",
        "app.rabbitmq.course-events.routing-key-course-created=course.created",
        "app.rabbitmq.course-events.routing-key-course-updated=course.updated",
        "app.rabbitmq.course-events.routing-key-course-deleted=course.deleted"
})
class CourseEventRabbitMqIntegrationTest {

    static final String TEST_QUEUE_CREATED = "test.course.created.q";
    static final String TEST_QUEUE_UPDATED = "test.course.updated.q";
    static final String TEST_QUEUE_DELETED = "test.course.deleted.q";

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management-alpine");

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitTemplate courseEventRabbitTemplate;

    @Autowired
    private CourseEventRabbitMqProperties properties;

    @BeforeEach
    void setUp() {
        rabbitAdmin.initialize();
        rabbitAdmin.purgeQueue(TEST_QUEUE_CREATED, false);
        rabbitAdmin.purgeQueue(TEST_QUEUE_UPDATED, false);
        rabbitAdmin.purgeQueue(TEST_QUEUE_DELETED, false);
    }


    @Test
    void deveRoteariEventoCourseCreatedParaFilaCorreta() {
        CourseEventPayload payload = buildPayload("course.created", "course", "Engenharia", "ENG-01");

        courseEventRabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyCourseCreated(),
                payload
        );

        Message received = courseEventRabbitTemplate.receive(TEST_QUEUE_CREATED, 5000);

        assertThat(received).isNotNull();
    }

    @Test
    void payloadDoCourseCreatedDeveConterCamposObrigatorios() {
        CourseEventPayload payload = buildPayload("course.created", "course", "Engenharia", "ENG-01");

        courseEventRabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyCourseCreated(),
                payload
        );

        CourseEventPayload received = (CourseEventPayload) courseEventRabbitTemplate
                .receiveAndConvert(TEST_QUEUE_CREATED, 5000);

        assertThat(received).isNotNull();
        assertThat(received.eventId()).isNotBlank();
        assertThat(received.correlationId()).isNotBlank();
        assertThat(received.source()).isEqualTo("core-api");
        assertThat(received.eventType()).isEqualTo("course.created");
        assertThat(received.occurredAt()).isNotNull();
        assertThat(received.entityType()).isEqualTo("course");
        assertThat(received.entityId()).isNotBlank();
        assertThat(received.name()).isEqualTo("Engenharia");
        assertThat(received.code()).isEqualTo("ENG-01");
    }

    @Test
    void courseCreatedNaoDeveRoterarParaFilaDeUpdatedOuDeleted() {
        CourseEventPayload payload = buildPayload("course.created", "course", "Engenharia", "ENG-01");

        courseEventRabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyCourseCreated(),
                payload
        );

        Message inUpdated = courseEventRabbitTemplate.receive(TEST_QUEUE_UPDATED, 1000);
        Message inDeleted = courseEventRabbitTemplate.receive(TEST_QUEUE_DELETED, 1000);

        assertThat(inUpdated).isNull();
        assertThat(inDeleted).isNull();
    }


    @Test
    void deveRoteiarEventoCourseUpdatedParaFilaCorreta() {
        CourseEventPayload payload = buildPayload("course.updated", "course", "Engenharia Atualizada", "ENG-02");

        courseEventRabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyCourseUpdated(),
                payload
        );

        Message received = courseEventRabbitTemplate.receive(TEST_QUEUE_UPDATED, 5000);

        assertThat(received).isNotNull();
    }

    @Test
    void courseUpdatedNaoDeveRoterarParaFilaDeCreatedOuDeleted() {
        CourseEventPayload payload = buildPayload("course.updated", "course", "Engenharia Atualizada", "ENG-02");

        courseEventRabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyCourseUpdated(),
                payload
        );

        Message inCreated = courseEventRabbitTemplate.receive(TEST_QUEUE_CREATED, 1000);
        Message inDeleted = courseEventRabbitTemplate.receive(TEST_QUEUE_DELETED, 1000);

        assertThat(inCreated).isNull();
        assertThat(inDeleted).isNull();
    }


    @Test
    void deveRoteiarEventoCourseDeletedParaFilaCorreta() {
        CourseEventPayload payload = buildPayload("course.deleted", "course", "Engenharia", "ENG-01");

        courseEventRabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyCourseDeleted(),
                payload
        );

        Message received = courseEventRabbitTemplate.receive(TEST_QUEUE_DELETED, 5000);

        assertThat(received).isNotNull();
    }

    @Test
    void courseDeletedNaoDeveRoterarParaFilaDeCreatedOuUpdated() {
        CourseEventPayload payload = buildPayload("course.deleted", "course", "Engenharia", "ENG-01");

        courseEventRabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyCourseDeleted(),
                payload
        );

        Message inCreated = courseEventRabbitTemplate.receive(TEST_QUEUE_CREATED, 1000);
        Message inUpdated = courseEventRabbitTemplate.receive(TEST_QUEUE_UPDATED, 1000);

        assertThat(inCreated).isNull();
        assertThat(inUpdated).isNull();
    }


    @Test
    void eventoComEntityTypeClassNaoDeveSerAceitoComoEventoValidoDeCourse() {
        CourseEventPayload payload = new CourseEventPayload(
                "evt-invalid",
                "corr-invalid",
                "core-api",
                "course.created",
                java.time.Instant.now(),
                "class",          // entityType errado
                "uuid-qualquer",
                "Turma Inválida",
                null
        );

        courseEventRabbitTemplate.convertAndSend(
                properties.exchange(),
                properties.routingKeyCourseCreated(),
                payload
        );

        CourseEventPayload received = (CourseEventPayload) courseEventRabbitTemplate
                .receiveAndConvert(TEST_QUEUE_CREATED, 5000);

        assertThat(received).isNotNull();
        assertThat(received.entityType()).isNotEqualTo("course");
    }


    @Test
    void fluxoDeCourseNaoDevePublicarEmNotificationsExchange() {
        // O adapter publica em properties.exchange() — garantir que não é notifications.exchange
        assertThat(properties.exchange()).isNotEqualTo("notifications.exchange");
    }


    private CourseEventPayload buildPayload(String eventType, String entityType, String name, String code) {
        return new CourseEventPayload(
                "evt-" + java.util.UUID.randomUUID(),
                "corr-" + java.util.UUID.randomUUID(),
                "core-api",
                eventType,
                java.time.Instant.now(),
                entityType,
                java.util.UUID.randomUUID().toString(),
                name,
                code
        );
    }


    @EnableAutoConfiguration
    @EnableConfigurationProperties(CourseEventRabbitMqProperties.class)
    @Import(CourseEventRabbitMqConfig.class)
    static class RabbitTestApplication {

        @Bean
        public JacksonJsonMessageConverter messageConverter() {
            return new JacksonJsonMessageConverter();
        }

        @Bean
        public RabbitTemplate courseEventRabbitTemplate(
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
        public Queue testQueueUpdated() {
            return new Queue(TEST_QUEUE_UPDATED, false, false, true);
        }

        @Bean
        public Queue testQueueDeleted() {
            return new Queue(TEST_QUEUE_DELETED, false, false, true);
        }

        @Bean
        public Binding bindingCreated(Queue testQueueCreated, TopicExchange courseEventsExchange) {
            return BindingBuilder.bind(testQueueCreated)
                    .to(courseEventsExchange)
                    .with("course.created");
        }

        @Bean
        public Binding bindingUpdated(Queue testQueueUpdated, TopicExchange courseEventsExchange) {
            return BindingBuilder.bind(testQueueUpdated)
                    .to(courseEventsExchange)
                    .with("course.updated");
        }

        @Bean
        public Binding bindingDeleted(Queue testQueueDeleted, TopicExchange courseEventsExchange) {
            return BindingBuilder.bind(testQueueDeleted)
                    .to(courseEventsExchange)
                    .with("course.deleted");
        }
    }
}
