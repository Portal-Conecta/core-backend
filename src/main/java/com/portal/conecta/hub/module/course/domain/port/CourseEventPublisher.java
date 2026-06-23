package com.portal.conecta.hub.module.course.domain.port;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;

public interface CourseEventPublisher {
    void publishCreated(CourseEntity course);
    void publishUpdated(CourseEntity course);
    void publishDeleted(CourseEntity course);
}
