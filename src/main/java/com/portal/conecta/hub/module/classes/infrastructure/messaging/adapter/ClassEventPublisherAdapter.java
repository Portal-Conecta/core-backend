package com.portal.conecta.hub.module.classes.infrastructure.messaging.adapter;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassEventPublisher;
import com.portal.conecta.hub.module.classes.infrastructure.messaging.config.ClassEventRabbitMqProperties;
import com.portal.conecta.hub.module.classes.infrastructure.messaging.dto.ClassEventPayload;
import com.portal.conecta.logging.CorrelationIdProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementação de {@link ClassEventPublisher} para os perfis {@code dev} e {@code prod}.
 *
 * <p>Publica eventos de turma no RabbitMQ via {@link RabbitTemplate}, utilizando
 * o exchange e routing keys configurados em {@link ClassEventRabbitMqProperties}.</p>
 *
 * <p>Cada evento carrega um identificador único de evento ({@code evt-*}),
 * um identificador de correlação ({@code corr-*}), a origem ({@code core-api}),
 * o tipo da entidade ({@code class}) e os dados operacionais da turma.</p>
 *
 * <p>Em testes, esta classe é substituída por {@link com.portal.conecta.hub.module.classes.domain.port.stub.ClassEventPublisherStub}.</p>
 */
@Component
@Profile({"dev", "prod"})
@Slf4j
public class ClassEventPublisherAdapter implements ClassEventPublisher {

    private static final String SOURCE = "core-api";
    private static final String ENTITY_TYPE = "class";

    private final RabbitTemplate rabbitTemplate;
    private final ClassEventRabbitMqProperties properties;
    private final CorrelationIdProvider correlationIdProvider;

    public ClassEventPublisherAdapter(
            RabbitTemplate rabbitTemplate,
            ClassEventRabbitMqProperties properties, CorrelationIdProvider correlationIdProvider
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
        this.correlationIdProvider = correlationIdProvider;
    }

    /**
     * Publica evento de criação ou reativação de turma com routing key {@code class.created}.
     *
     * @param classEntity turma cujos dados serão incluídos no payload.
     */
    @Override
    public void publishCreated(ClassEntity classEntity) {
        publish("class.created", properties.routingKeyClassCreated(), classEntity);
    }

    /**
     * Publica evento de exclusão lógica ou desativação de turma com routing key {@code class.deleted}.
     *
     * @param classEntity turma cujos dados serão incluídos no payload.
     */
    @Override
    public void publishDeleted(ClassEntity classEntity) {
        publish("class.deleted", properties.routingKeyClassDeleted(), classEntity);
    }

    private void publish(String eventType, String routingKey, ClassEntity classEntity) {
        ClassEventPayload payload = new ClassEventPayload(
                "evt-" + UUID.randomUUID(),
                correlationIdProvider.get(),
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