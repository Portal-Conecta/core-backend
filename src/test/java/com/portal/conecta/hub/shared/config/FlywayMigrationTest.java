package com.portal.conecta.hub.shared.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationTest {

    @Test
    void deveAplicarTodasAsMigrationsNoH2() throws Exception {
        String databaseUrl = "jdbc:h2:mem:flyway-migrations;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

        try (var connection = DriverManager.getConnection(databaseUrl, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("CREATE DOMAIN TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE");
        }

        Flyway flyway = Flyway.configure()
                .dataSource(databaseUrl, "sa", "")
                .locations("classpath:db/migration")
                .load();

        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(8);

        try (var connection = DriverManager.getConnection(databaseUrl, "sa", "");
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("""
                     SELECT COUNT(*)
                     FROM INFORMATION_SCHEMA.INDEXES
                     WHERE INDEX_NAME IN (
                         'IDX_CLASSES_COURSE_ID_NUMBER_DELETED_AT',
                         'IDX_CLASSES_COURSE_ID_ACTIVE_DELETED_AT_SHIFT',
                         'IDX_USER_CLASSES_CLASS_ID_ROLE_CLASS',
                         'IDX_USER_NOTIFICATIONS_VISIBLE_BY_USER',
                         'IDX_USER_NOTIFICATIONS_UNREAD_BY_USER'
                     )
                     """)) {
            resultSet.next();
            assertThat(resultSet.getInt(1)).isEqualTo(5);
        }
    }
}
