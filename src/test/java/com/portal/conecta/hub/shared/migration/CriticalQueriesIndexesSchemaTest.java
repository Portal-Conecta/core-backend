package com.portal.conecta.hub.shared.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=none"
})
class CriticalQueriesIndexesSchemaTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsIndexesForCriticalQueries() {
        createSchemaBeforeMigration();

        new ResourceDatabasePopulator(
                new ClassPathResource("db/migration/V7__add_indexes_for_critical_queries.sql")
        ).execute(dataSource);

        Set<String> indexNames = Set.copyOf(jdbcTemplate.queryForList(
                "SELECT LOWER(index_name) FROM information_schema.indexes " +
                        "WHERE LOWER(index_name) IN (" +
                        "'idx_classes_course_id_number_deleted_at', " +
                        "'idx_classes_course_id_active_deleted_at_shift', " +
                        "'idx_user_classes_class_id_role_class', " +
                        "'idx_user_notifications_visible_by_user', " +
                        "'idx_user_notifications_unread_by_user')",
                String.class
        ));

        assertThat(indexNames).containsExactlyInAnyOrder(
                "idx_classes_course_id_number_deleted_at",
                "idx_classes_course_id_active_deleted_at_shift",
                "idx_user_classes_class_id_role_class",
                "idx_user_notifications_visible_by_user",
                "idx_user_notifications_unread_by_user"
        );
    }

    private void createSchemaBeforeMigration() {
        jdbcTemplate.execute("CREATE TABLE classes (course_id UUID, number INTEGER, active BOOLEAN, deleted_at TIMESTAMP, shift VARCHAR(30))");
        jdbcTemplate.execute("CREATE TABLE user_classes (class_id UUID, role_class VARCHAR(30))");
        jdbcTemplate.execute("CREATE TABLE user_notifications (user_id UUID, dismissed_at TIMESTAMP, read_at TIMESTAMP, created_at TIMESTAMP)");
        jdbcTemplate.execute("CREATE INDEX idx_classes_course_id ON classes (course_id)");
        jdbcTemplate.execute("CREATE INDEX idx_user_classes_class_id ON user_classes (class_id)");
        jdbcTemplate.execute("CREATE INDEX idx_user_notifications_user_id ON user_notifications (user_id)");
    }
}
