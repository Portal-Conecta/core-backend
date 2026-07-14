package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("GlobalScopeResolver integration")
class GlobalScopeResolverIntegrationTest {

    @Autowired
    private GlobalScopeResolver resolver;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("deve inserir apenas usuarios ativos e nao deletados sem duplicar reprocessamento")
    void deveInserirUsuariosAtivosSemDuplicar() {
        UserEntity activeStudent = saveActiveUser("global-active-student@test.local", TypeUser.STUDENT);
        UserEntity activeTeacher = saveActiveUser("global-active-teacher@test.local", TypeUser.TEACHER);
        UserEntity inactiveStudent = saveInactiveUser("global-inactive-student@test.local", TypeUser.STUDENT);
        UserEntity deletedStudent = saveDeletedUser("global-deleted-student@test.local", TypeUser.STUDENT);
        NotificationEntity notification = saveNotification("msg-global-all");

        entityManager.flush();

        resolver.insert(notification.getId(), EnumSet.noneOf(TypeUser.class));
        resolver.insert(notification.getId(), EnumSet.noneOf(TypeUser.class));

        assertThat(countRecipient(notification.getId(), activeStudent.getId())).isEqualTo(1);
        assertThat(countRecipient(notification.getId(), activeTeacher.getId())).isEqualTo(1);
        assertThat(countRecipient(notification.getId(), inactiveStudent.getId())).isZero();
        assertThat(countRecipient(notification.getId(), deletedStudent.getId())).isZero();
    }

    @Test
    @DisplayName("deve restringir notificacao global por filtro ROLE")
    void deveRestringirPorRole() {
        UserEntity activeStudent = saveActiveUser("global-role-student@test.local", TypeUser.STUDENT);
        UserEntity activeTeacher = saveActiveUser("global-role-teacher@test.local", TypeUser.TEACHER);
        UserEntity activeAdmin = saveActiveUser("global-role-admin@test.local", TypeUser.ADMIN);
        NotificationEntity notification = saveNotification("msg-global-role");

        entityManager.flush();

        resolver.insert(notification.getId(), EnumSet.of(TypeUser.STUDENT, TypeUser.TEACHER));

        assertThat(countRecipient(notification.getId(), activeStudent.getId())).isEqualTo(1);
        assertThat(countRecipient(notification.getId(), activeTeacher.getId())).isEqualTo(1);
        assertThat(countRecipient(notification.getId(), activeAdmin.getId())).isZero();
    }

    private UserEntity saveActiveUser(String email, TypeUser type) {
        return userRepository.save(new UserEntity("Usuario Teste", email, "hash", type));
    }

    private UserEntity saveInactiveUser(String email, TypeUser type) {
        return userRepository.save(UserEntity.createPendingActivation("Usuario Inativo", email, "hash", type, null));
    }

    private UserEntity saveDeletedUser(String email, TypeUser type) {
        UserEntity user = new UserEntity("Usuario Deletado", email, "hash", type);
        user.delete(null);
        return userRepository.save(user);
    }

    private NotificationEntity saveNotification(String messageId) {
        return notificationRepository.save(NotificationEntity.create(
                messageId,
                "corr-" + messageId,
                "SOURCE",
                "EVENT",
                Instant.now(),
                "Titulo",
                "Corpo",
                null
        ));
    }

    private long countRecipient(UUID notificationId, UUID userId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_notifications WHERE notification_id = ? AND user_id = ?",
                Long.class,
                notificationId,
                userId
        );
        return count == null ? 0 : count;
    }
}
