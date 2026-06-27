package com.portal.conecta.hub;

import com.portal.conecta.hub.module.auth.infrastructure.security.JwtProperties;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.config.NotificationRabbitMqProperties;
import com.portal.conecta.hub.shared.config.RabbitMqProperties;
import com.portal.conecta.logging.LoggingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		JwtProperties.class,
		NotificationRabbitMqProperties.class,
		RabbitMqProperties.class,
		LoggingProperties.class
})public class HubApplication {

	public static void main(String[] args) {
		SpringApplication.run(HubApplication.class, args);
	}

}
