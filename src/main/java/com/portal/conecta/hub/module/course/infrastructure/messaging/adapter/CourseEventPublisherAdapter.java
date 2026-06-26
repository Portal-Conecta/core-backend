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
 * </p>
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

    /**
     * Publica um evento no RabbitMQ indicando que um novo curso foi criado com sucesso.
     *
     * @param course A entidade do curso recém-criada cujos metadados serão enviados no evento.
     */
    @Override
    public void publishCreated(CourseEntity course) {
        publish("course.created", properties.routingKeyCourseCreated(), course);
    }

    /**
     * Publica um evento no RabbitMQ indicando que os dados de um curso existente foram alterados.
     *
     * @param course A entidade do curso com os dados já atualizados.
     */
    @Override
    public void publishUpdated(CourseEntity course) {
        publish("course.updated", properties.routingKeyCourseUpdated(), course);
    }

    /**
     * Publica um evento no RabbitMQ indicando que um curso foi removido (exclusão lógica/física).
     *
     * @param course A entidade do curso que foi deletada.
     */
    @Override
    public void publishDeleted(CourseEntity course) {
        publish("course.deleted", properties.routingKeyCourseDeleted(), course);
    }

    /**
     * Método utilitário privado responsável por construir o payload padronizado e enviá-lo ao broker.
     * <p>
     * Gera identificadores únicos (UUID) para rastreabilidade (event ID e correlation ID) e
     * utiliza o {@code RabbitTemplate} para despachar a mensagem para a exchange correta.
     * </p>
     *
     * @param eventType O tipo do evento gerado (ex: "course.created", "course.updated").
     * @param routingKey A chave de roteamento configurada para direcionar a mensagem à fila correspondente.
     * @param course A entidade do curso que serve como base para extração do payload (ID, nome e código).
     */
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
