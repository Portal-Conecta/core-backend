package com.portal.conecta.hub.module.classes.infrastructure.messaging.adapter;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassEventPublisher;
import com.portal.conecta.hub.module.classes.infrastructure.messaging.config.ClassEventRabbitMqProperties;
import com.portal.conecta.hub.module.classes.infrastructure.messaging.dto.ClassEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Profile({"dev", "prod"})
@Slf4j
public class ClassEventPublisherAdapter implements ClassEventPublisher {

    private static final String SOURCE = "core-api";
    private static final String ENTITY_TYPE = "class";

    private final RabbitTemplate rabbitTemplate;
    private final ClassEventRabbitMqProperties properties;

    public ClassEventPublisherAdapter(
            RabbitTemplate rabbitTemplate,
            ClassEventRabbitMqProperties properties
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publishCreated(ClassEntity classEntity) {
        publish("class.created", properties.routingKeyClassCreated(), classEntity);
    }

    @Override
    public void publishDeleted(ClassEntity classEntity) {
        publish("class.deleted", properties.routingKeyClassDeleted(), classEntity);
    }

    private void publish(String eventType, String routingKey, ClassEntity classEntity) {
        ClassEventPayload payload = new ClassEventPayload(
                "evt-" + UUID.randomUUID(),
                "corr-" + UUID.randomUUID(),
                SOURCE,
                eventType,
                Instant.now(),
                ENTITY_TYPE,
                classEntity.getId().toString(),
                classEntity.getName()
        );

        log.info("Publicando evento de turma. eventType={}, entityId={}", eventType, classEntity.getId());
        rabbitTemplate.convertAndSend(properties.exchange(), routingKey, payload);
    }
}