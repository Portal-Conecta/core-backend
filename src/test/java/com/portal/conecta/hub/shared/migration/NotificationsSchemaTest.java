package com.portal.conecta.hub.shared.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("dev") // ou o profile que tem o Flyway com sua migration
class NotificationsSchemaTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

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