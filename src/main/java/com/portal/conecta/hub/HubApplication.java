package com.portal.conecta.hub;

import com.portal.conecta.hub.module.auth.infrastructure.security.JwtProperties;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.NotificationRabbitMqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, NotificationRabbitMqProperties.class})
public class HubApplication {

	public static void main(String[] args) {
		SpringApplication.run(HubApplication.class, args);
	}

}
