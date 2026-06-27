package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Porta de persistência das notificações materializadas para usuários.
 *
 * <p>Enquanto {@code NotificationEntity} representa o evento global, esta porta opera sobre
 * os vínculos individuais que controlam leitura, descarte e visibilidade por usuário.</p>
 */
public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, UUID> {

    /**
     * Busca o vínculo entre usuário e notificação global.
     *
     * @param userId identificador do usuário destinatário.
     * @param notificationId identificador da notificação global.
     * @return vínculo individual da notificação, quando existir.
     */
    Optional<UserNotificationEntity> findByUserIdAndNotificationId(UUID userId, UUID notificationId);

    /**
     * Lista todos os vínculos não lidos de um usuário.
     *
     * @param userId identificador do usuário destinatário.
     * @return notificações de usuário ainda não lidas.
     */
    List<UserNotificationEntity> findAllByUserIdAndReadAtIsNull(UUID userId);

    boolean existsByNotificationIdAndUserId(UUID notificationId, UUID userId);

    /**
     * Lista notificações visíveis do usuário, excluindo as descartadas.
     *
     * @param userId identificador do usuário destinatário.
     * @param unreadOnly quando verdadeiro, retorna apenas notificações ainda não lidas.
     * @param pageable paginação solicitada.
     * @return página de notificações materializadas para o usuário.
     */
    @Query("""
        SELECT un FROM UserNotificationEntity un
        JOIN FETCH un.notification n
        WHERE un.user.id = :userId
          AND un.dismissedAt IS NULL
          AND (:unreadOnly = false OR un.readAt IS NULL)
        ORDER BY un.createdAt DESC
    """)
    Page<UserNotificationEntity> findVisibleByUserId(
            @Param("userId") UUID userId,
            @Param("unreadOnly") boolean unreadOnly,
            Pageable pageable
    );

    /**
     * Conta notificações não lidas e não descartadas de um usuário.
     *
     * @param userId identificador do usuário destinatário.
     * @return quantidade de notificações pendentes de leitura.
     */
    @Query("""
        SELECT COUNT(un) FROM UserNotificationEntity un
        WHERE un.user.id = :userId
          AND un.readAt IS NULL
          AND un.dismissedAt IS NULL
    """)
    long countUnreadByUserId(@Param("userId") UUID userId);

    /**
     * Materializa entrega direta para usuários ativos e não removidos logicamente.
     *
     * @param notificationId identificador da notificação global.
     * @param userIds destinatários informados diretamente no escopo USER.
     */
    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = """
            INSERT INTO user_notifications (id, notification_id, user_id, created_at)
            SELECT gen_random_uuid(), :notificationId, u.id, NOW()
            FROM users u
            WHERE u.id IN (:userIds)
              AND u.active = true
              AND u.deleted_at IS NULL
              AND NOT EXISTS (
                  SELECT 1 FROM user_notifications un
                  WHERE un.notification_id = :notificationId AND un.user_id = u.id
              )
            """, nativeQuery = true)
    void insertUsersDirectly(
            @Param("notificationId") UUID notificationId,
            @Param("userIds") Set<UUID> userIds
    );

    /**
     * Materializa entrega para usuários vinculados a turmas ativas.
     *
     * @param notificationId identificador da notificação global.
     * @param classIds turmas usadas como escopo de distribuição.
     * @param types tipos globais de usuário permitidos.
     */
    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = """
            INSERT INTO user_notifications (id, notification_id, user_id, created_at)
            SELECT gen_random_uuid(), :notificationId, uc.user_id, NOW()
            FROM user_classes uc
            JOIN users u  ON u.id  = uc.user_id
            JOIN classes c ON c.id = uc.class_id
            WHERE c.id IN (:classIds)
              AND c.deleted_at IS NULL
              AND c.active = true
              AND u.active = true
              AND u.deleted_at IS NULL
              AND u.type_user IN (:types)
              AND NOT EXISTS (
                  SELECT 1 FROM user_notifications un
                  WHERE un.notification_id = :notificationId AND un.user_id = uc.user_id
              )
            """, nativeQuery = true)
    void insertByClassScope(
            @Param("notificationId") UUID notificationId,
            @Param("classIds") List<UUID> classIds,
            @Param("types") Set<String> types
    );

    /**
     * Materializa entrega para usuários vinculados às turmas ativas dos cursos informados.
     *
     * @param notificationId identificador da notificação global.
     * @param courseIds cursos usados como escopo de distribuição.
     * @param types tipos globais de usuário permitidos.
     */
    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = """
            INSERT INTO user_notifications (id, notification_id, user_id, created_at)
            SELECT gen_random_uuid(), :notificationId, uc.user_id, NOW()
            FROM user_classes uc
            JOIN users u   ON u.id  = uc.user_id
            JOIN classes c ON c.id  = uc.class_id
            WHERE c.course_id IN (:courseIds)
              AND c.deleted_at IS NULL
              AND c.active = true
              AND u.active = true
              AND u.deleted_at IS NULL
              AND u.type_user IN (:types)
              AND NOT EXISTS (
                  SELECT 1 FROM user_notifications un
                  WHERE un.notification_id = :notificationId AND un.user_id = uc.user_id
              )
            """, nativeQuery = true)
    void insertByCourseScope(
            @Param("notificationId") UUID notificationId,
            @Param("courseIds") List<UUID> courseIds,
            @Param("types") Set<String> types
    );
}
