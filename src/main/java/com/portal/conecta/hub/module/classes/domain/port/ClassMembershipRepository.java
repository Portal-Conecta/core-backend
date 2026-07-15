package com.portal.conecta.hub.module.classes.domain.port;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.me.infrastructure.projection.UserCourseClassProjection;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Port de acesso a dados para vínculos entre usuários e turmas.
 *
 * <p>Métodos com {@code active} no nome filtram usuários ativos e não removidos
 * logicamente. Métodos sem esse qualificador não aplicam filtro de estado.</p>
 */
public interface ClassMembershipRepository extends JpaRepository<ClassMembershipEntity, ClassMembershipId> {

    /**
     * Retorna todos os vínculos do usuário sem filtro de estado.
     * Inclui turmas removidas logicamente, desativadas e usuários inativos.
     */
    List<ClassMembershipEntity> findAllByUserId(UUID userId);

    /**
     * Verifica se já existe vínculo entre o usuário e a turma, independentemente do papel.
     * Utilizado para impedir duplicidade de matrícula.
     */
    @Query("SELECT COUNT(m) > 0 FROM ClassMembershipEntity m WHERE m.user.id = :userId AND m.classEntity.id = :classId")
    boolean existsByUserIdAndClassId(@Param("userId") UUID userId, @Param("classId") UUID classId);

    /**
     * Conta vínculos ativos do usuário em turmas não removidas para um papel específico.
     * Utilizado para validar o limite de turmas simultâneas de um estudante.
     */
    @Query("SELECT COUNT(m) FROM ClassMembershipEntity m WHERE m.user.id = :userId AND m.classRole = :classRole AND m.classEntity.deletedAt IS NULL")
    long countByUserIdAndClassRole(@Param("userId") UUID userId, @Param("classRole") ClassRole classRole);

    /**
     * Retorna cursos e turmas ativas vinculados a um usuário, agrupados por curso.
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

        ORDER BY c.name, cl.number
    """)
    List<UserCourseClassProjection> findCoursesByUserId(
            @Param("userId") UUID userId
    );

    /**
     * Conta vínculos com um papel específico dentro de uma turma.
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
              AND u.deletedAt IS NULL
        """)
    List<ClassMembershipEntity> findNonRemovedMembersByClassIdAndRoles(
            @Param("classId") UUID classId,
            @Param("roles") EnumSet<ClassRole> roles
    );

    /**
     * Retorna membros ativos de uma turma filtrando pelo {@code TypeUser} do usuário.
     * Exclui usuários inativos ou removidos logicamente.
     */
    @Query("""
    SELECT m FROM ClassMembershipEntity m
    JOIN FETCH m.user u
    WHERE m.classEntity.id = :classId
      AND u.type IN (:types)
      AND u.active = true
      AND u.deletedAt IS NULL
    """)
    List<ClassMembershipEntity> findActiveMembersByClassIdAndUserTypes(
            @Param("classId") UUID classId,
            @Param("types") EnumSet<TypeUser> types
    );

    /**
     * Retorna membros ativos de todas as turmas de um curso, filtrando pelo {@code TypeUser}.
     * Exclui turmas removidas logicamente e usuários inativos ou removidos.
     */
    @Query("""
    SELECT m FROM ClassMembershipEntity m
    JOIN FETCH m.user u
    WHERE m.classEntity.course.id = :courseId
      AND u.type IN (:types)
      AND u.active = true
      AND u.deletedAt IS NULL
      AND m.classEntity.deletedAt IS NULL
    """)
    List<ClassMembershipEntity> findActiveMembersByCourseIdAndUserTypes(
            @Param("courseId") UUID courseId,
            @Param("types") EnumSet<TypeUser> types
    );

    /**
     * Retorna vínculos ativos do usuário, carregando turma e curso associados.
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
