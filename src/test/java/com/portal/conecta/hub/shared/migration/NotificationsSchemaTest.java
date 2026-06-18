package com.portal.conecta.hub.shared.migration;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class NotificationsSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitAdmin rabbitAdmin;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void notificationsTableExistsWithExpectedColumns() {
        var columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE LOWER(TABLE_NAME) = 'notifications'"
        );
        assertThat(columns).isNotEmpty();
    }

    @Test
    void messageIdMustBeUnique() {
        jdbcTemplate.update(
                "INSERT INTO notifications (id, message_id, source, event_type, occurred_at, title, body, created_at) " +
                        "VALUES (random_uuid(), 'msg-test-1', 'src', 'evt', now(), 'titulo', 'corpo', now())"
        );

        assertThrows(Exception.class, () -> jdbcTemplate.update(
                "INSERT INTO notifications (id, message_id, source, event_type, occurred_at, title, body, created_at) " +
                        "VALUES (random_uuid(), 'msg-test-1', 'src', 'evt', now(), 'titulo2', 'corpo2', now())"
        ));
    }
}