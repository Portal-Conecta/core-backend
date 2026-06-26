package com.portal.conecta.hub.module.classes.domain.port.stub;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de {@link ClassEventPublisher} para uso exclusivo em testes.
 *
 * <p>Armazena os eventos publicados em memória no formato {@code "tipo:id"},
 * permitindo verificar quais eventos foram disparados durante um teste
 * sem depender de infraestrutura de mensageria real.</p>
 *
 * <p>Usar {@link #reset()} entre testes para evitar contaminação de estado.</p>
 */
@Component
@Profile("test")
public class ClassEventPublisherStub implements ClassEventPublisher {

    private final List<String> publishedEvents = new ArrayList<>();

    @Override
    public void publishCreated(ClassEntity classEntity) {
        publishedEvents.add("turma.created:" + classEntity.getId());
    }

    @Override
    public void publishDeleted(ClassEntity classEntity) {
        publishedEvents.add("turma.deleted:" + classEntity.getId());
    }

    public List<String> getPublishedEvents() {
        return List.copyOf(publishedEvents);
    }

    public void reset() {
        publishedEvents.clear();
    }
}