package com.portal.conecta.hub.module.classes.domain.port;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;

/**
 * Port de publicação de eventos do ciclo de vida de turmas.
 *
 * <p>Desacopla os use cases da implementação de mensageria. Cada método
 * deve ser chamado após a persistência bem-sucedida da operação correspondente.</p>
 */
public interface ClassEventPublisher {
    /**
     * Publica evento de criação ou reativação de turma.
     *
     * <p>Utilizado após criação, restauração e reativação — situações em que
     * a turma passa a estar disponível nos fluxos normais do sistema.</p>
     *
     * @param classEntity turma cujo evento será publicado.
     */
    void publishCreated(ClassEntity classEntity);


    /**
     * Publica evento de remoção ou desativação de turma.
     *
     * <p>Utilizado após exclusão lógica e desativação — situações em que
     * a turma deixa de participar dos fluxos normais do sistema.</p>
     *
     * @param classEntity turma cujo evento será publicado.
     */
    void publishDeleted(ClassEntity classEntity);
}