package com.portal.conecta.hub.module.course.domain.port;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;

/**
 * Porta de saída (Outbound Port) para mensageria.
 * <p>
 * Define o contrato para notificação de mudanças de estado no ciclo de vida dos cursos.
 * Utilizado para manter a consistência eventual com outros serviços do ecossistema
 * que mantêm réplicas ou referências do catálogo de cursos.
 */
public interface CourseEventPublisher {
    void publishCreated(CourseEntity course);
    void publishUpdated(CourseEntity course);
    void publishDeleted(CourseEntity course);
}
