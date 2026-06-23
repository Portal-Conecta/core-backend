package com.portal.conecta.hub.module.classes.domain.port;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;

public interface ClassEventPublisher {
    void publishCreated(ClassEntity classEntity);
    void publishDeleted(ClassEntity classEntity);
}