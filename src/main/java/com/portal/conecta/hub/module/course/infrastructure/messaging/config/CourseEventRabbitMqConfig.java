package com.portal.conecta.hub.module.course.infrastructure.messaging.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CourseEventRabbitMqProperties.class)
public class CourseEventRabbitMqConfig {

    private final CourseEventRabbitMqProperties properties;

    public CourseEventRabbitMqConfig(CourseEventRabbitMqProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TopicExchange courseEventsExchange() {
        return new TopicExchange(properties.exchange(), true, false);
    }
}
