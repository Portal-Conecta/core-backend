package com.portal.conecta.hub.module.classes.domain.port;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.me.infrastructure.projection.UserCourseClassProjection;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Port de acesso a dados para vinculos entre usuarios e turmas.
 *
 * <p>Metodos com {@code active} no nome filtram usuarios ativos e nao removidos
 * logicamente. Metodos sem esse qualificador nao aplicam filtro de estado.</p>
 */
public interface ClassMembershipRepository extends JpaRepository<ClassMembershipEntity, ClassMembershipId> {

    /**
     * Retorna todos os vinculos do usuario sem filtro de estado.
     * Inclui turmas removidas logicamente, desativadas e usuarios inativos.
     */
    List<ClassMembershipEntity> findAllByUserId(UUID userId);

    /**
     * Verifica se ja existe vinculo entre o usuario e a turma, independentemente do papel.
     * Utilizado para impedir duplicidade de matricula.
     */
    @Query("SELECT COUNT(m) > 0 FROM ClassMembershipEntity m WHERE m.user.id = :userId AND m.classEntity.id = :classId")
    boolean existsByUserIdAndClassId(@Param("userId") UUID userId, @Param("classId") UUID classId);

    /**
     * Conta vinculos ativos do usuario em turmas nao removidas para um papel especifico.
     * Utilizado para validar o limite de turmas simultaneas de um estudante.
     */
    @Query("SELECT COUNT(m) FROM ClassMembershipEntity m WHERE m.user.id = :userId AND m.classRole = :classRole AND m.classEntity.deletedAt IS NULL AND m.classEntity.active = true")
    long countByUserIdAndClassRole(@Param("userId") UUID userId, @Param("classRole") ClassRole classRole);

    /**
     * Retorna cursos e turmas ativas vinculados a um usuario, agrupados por curso.
     * Exclui turmas e cursos removidos logicamente.
     */
    @Query("""
        SELECT
            c.id AS courseId,
            c.name AS courseName,
            c.code AS courseCode,

            cl.id AS classId,
            cl.name AS className,
            cl.number AS classNumber,
            cl.shift AS classShift,

            cm.classRole AS role

        FROM ClassMembershipEntity cm
            JOIN cm.classEntity cl
            JOIN cl.course c

            WHERE cm.user.id = :userId
            AND c.deletedAt IS NULL
            AND cl.deletedAt IS NULL
            AND cl.active = true

        ORDER BY c.name, cl.number
    """)
    List<UserCourseClassProjection> findCoursesByUserId(
            @Param("userId") UUID userId
    );

    /**
     * Conta vinculos com um papel especifico dentro de uma turma.
     * Utilizado para verificar disponibilidade de vaga de representante.
     */
    @Query("SELECT COUNT(m) FROM ClassMembershipEntity m WHERE m.classEntity.id = :classId AND m.classRole = :classRole")
    long countByClassIdAndClassRole(@Param("classId") UUID classId, @Param("classRole") ClassRole classRole);

    /**
     * Retorna membros ativos de uma turma filtrando pelos papeis informados.
     * Exclui usuarios inativos ou removidos.
     */
    @Query("""
            SELECT m FROM ClassMembershipEntity m
            JOIN FETCH m.user u
            WHERE m.classEntity.id = :classId
              AND m.classRole IN (:roles)
              AND u.accountStatus <> com.portal.conecta.hub.module.user.domain.model.AccountStatus.PENDING_DELETION
        """)
    List<ClassMembershipEntity> findNonRemovedMembersByClassIdAndRoles(
            @Param("classId") UUID classId,
            @Param("roles") EnumSet<ClassRole> roles
    );

    /**
     * Retorna membros nao removidos das turmas informadas filtrando pelos papeis.
     * Exclui usuarios removidos logicamente.
     */
    @Query("""
            SELECT DISTINCT m FROM ClassMembershipEntity m
            JOIN FETCH m.user u
            WHERE m.classEntity.id IN (:classIds)
              AND m.classRole IN (:roles)
              AND u.accountStatus <> com.portal.conecta.hub.module.user.domain.model.AccountStatus.PENDING_DELETION
        """)
    List<ClassMembershipEntity> findNonRemovedMembersByClassIdsAndRoles(
            @Param("classIds") List<UUID> classIds,
            @Param("roles") EnumSet<ClassRole> roles
    );

    /**
     * Retorna membros ativos de uma turma filtrando pelo {@code TypeUser} do usuario.
     * Exclui usuarios inativos ou removidos logicamente.
     */
    @Query("""
    SELECT m FROM ClassMembershipEntity m
    JOIN FETCH m.user u
    WHERE m.classEntity.id = :classId
      AND u.type IN (:types)
      AND u.accountStatus = com.portal.conecta.hub.module.user.domain.model.AccountStatus.ACTIVE
    """)
    List<ClassMembershipEntity> findActiveMembersByClassIdAndUserTypes(
            @Param("classId") UUID classId,
            @Param("types") EnumSet<TypeUser> types
    );

    /**
     * Retorna membros ativos de todas as turmas de um curso, filtrando pelo {@code TypeUser}.
     * Exclui turmas removidas logicamente e usuarios inativos ou removidos.
     */
    @Query("""
    SELECT m FROM ClassMembershipEntity m
    JOIN FETCH m.user u
    WHERE m.classEntity.course.id = :courseId
      AND u.type IN (:types)
      AND u.accountStatus = com.portal.conecta.hub.module.user.domain.model.AccountStatus.ACTIVE
      AND m.classEntity.deletedAt IS NULL
      AND m.classEntity.active = true
    """)
    List<ClassMembershipEntity> findActiveMembersByCourseIdAndUserTypes(
            @Param("courseId") UUID courseId,
            @Param("types") EnumSet<TypeUser> types
    );

    /**
     * Retorna vinculos ativos do usuario, carregando turma e curso associados.
     * Exclui turmas removidas logicamente ou inativas.
     */
    @Query("""
            SELECT m FROM ClassMembershipEntity m
            JOIN FETCH m.classEntity cl
            JOIN FETCH cl.course c
            WHERE m.user.id = :userId
              AND cl.deletedAt IS NULL
              AND cl.active = true
        """)
    List<ClassMembershipEntity> findActiveByUserId(@Param("userId") UUID userId);
}
