package com.portal.conecta.hub.module.course.infrastructure.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rabbitmq.course-events")
public record CourseEventRabbitMqProperties(
        String exchange,
        String routingKeyCourseCreated,
        String routingKeyCourseUpdated,
        String routingKeyCourseDeleted
) {
}
