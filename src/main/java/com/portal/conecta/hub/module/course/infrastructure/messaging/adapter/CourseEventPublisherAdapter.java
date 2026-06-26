package com.portal.conecta.hub.module.course.infrastructure.messaging.adapter;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseEventPublisher;
import com.portal.conecta.hub.module.course.infrastructure.messaging.config.CourseEventRabbitMqProperties;
import com.portal.conecta.hub.module.course.infrastructure.messaging.dto.CourseEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptador de infraestrutura que implementa a publicação de eventos de curso via RabbitMQ.
 * <p>
 * Constrói o payload de integração contendo identificadores de rastreabilidade
 * (eventId e correlationId) garantindo que metadados básicos (ID, nome e código)
 * sejam entregues aos consumidores assim que uma transação de curso é comitada.
 */
@Component
@Profile({"dev", "prod"})
@Slf4j
public class CourseEventPublisherAdapter implements CourseEventPublisher {

    private static final String SOURCE = "core-api";
    private static final String ENTITY_TYPE = "course";

    private final RabbitTemplate courseEventRabbitTemplate;
    private final CourseEventRabbitMqProperties properties;

    public CourseEventPublisherAdapter(
            RabbitTemplate courseEventRabbitTemplate,
            CourseEventRabbitMqProperties properties
    ) {
        this.courseEventRabbitTemplate = courseEventRabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publishCreated(CourseEntity course) {
        publish("course.created", properties.routingKeyCourseCreated(), course);
    }

    @Override
    public void publishUpdated(CourseEntity course) {
        publish("course.updated", properties.routingKeyCourseUpdated(), course);
    }

    @Override
    public void publishDeleted(CourseEntity course) {
        publish("course.deleted", properties.routingKeyCourseDeleted(), course);
    }

    private void publish(String eventType, String routingKey, CourseEntity course) {
        CourseEventPayload payload = new CourseEventPayload(
                "evt-" + UUID.randomUUID(),
                "corr-" + UUID.randomUUID(),
                SOURCE,
                eventType,
                Instant.now(),
                ENTITY_TYPE,
                course.getId().toString(),
                course.getName(),
                course.getCode()
        );

        log.info("Publicando evento de curso. eventType={}, entityId={}", eventType, course.getId());
        courseEventRabbitTemplate.convertAndSend(properties.exchange(), routingKey, payload);
    }
}
