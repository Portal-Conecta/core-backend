package com.portal.conecta.hub.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura a infraestrutura RabbitMQ do Hub Core com serialização JSON via Jackson.
 *
 * <p>Ativa apenas quando {@code app.rabbitmq.enabled=true} (padrão ativo).
 * Registra {@link org.springframework.amqp.rabbit.core.RabbitAdmin} para gerenciamento
 * de filas e exchanges, e {@link org.springframework.amqp.rabbit.core.RabbitTemplate}
 * com conversor Jackson para que mensagens trafeguem como JSON.
 */
@Configuration
@ConditionalOnBooleanProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = true, matchIfMissing = true)
public class RabbitMqInfraConfig {

    @Bean
    public RabbitAdmin rabbitAdmin (ConnectionFactory connectionFactory){
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter
    ){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter((MessageConverter) messageConverter);
        return template;
    }

    @Bean
    ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}
