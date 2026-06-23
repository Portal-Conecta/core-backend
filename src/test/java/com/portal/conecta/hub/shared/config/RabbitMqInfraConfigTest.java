package com.portal.conecta.hub.shared.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("RabbitMqInfraConfig")
class RabbitMqInfraConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(RabbitMqInfraConfig.class, TestConnectionFactoryConfig.class)
            .withPropertyValues("app.rabbitmq.enabled=true");

    @Test
    @DisplayName("deve registrar RabbitAdmin")
    void deveRegistrarRabbitAdmin() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(RabbitAdmin.class)
        );
    }

    @Test
    @DisplayName("deve registrar JacksonJsonMessageConverter")
    void deveRegistrarMessageConverter() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(JacksonJsonMessageConverter.class)
        );
    }

    @Test
    @DisplayName("deve registrar RabbitTemplate com o messageConverter correto")
    void deveRegistrarRabbitTemplateComConverter() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RabbitTemplate.class);

            RabbitTemplate template = context.getBean(RabbitTemplate.class);
            assertThat(template.getMessageConverter())
                    .isInstanceOf(JacksonJsonMessageConverter.class);
        });
    }

    @Test
    @DisplayName("não deve registrar beans quando app.rabbitmq.enabled=false")
    void naoDeveRegistrarBeansQuandoDesabilitado() {
        new ApplicationContextRunner()
                .withUserConfiguration(RabbitMqInfraConfig.class, TestConnectionFactoryConfig.class)
                .withPropertyValues("app.rabbitmq.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RabbitAdmin.class);
                    assertThat(context).doesNotHaveBean(RabbitTemplate.class);
                    assertThat(context).doesNotHaveBean(JacksonJsonMessageConverter.class);
                });
    }

    @Configuration
    static class TestConnectionFactoryConfig {
        @Bean
        ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }
    }
}