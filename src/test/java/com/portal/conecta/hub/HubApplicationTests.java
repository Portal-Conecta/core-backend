package com.portal.conecta.hub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class HubApplicationTests {

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management-alpine");

	@Test
	void contextLoads() {
	}

}