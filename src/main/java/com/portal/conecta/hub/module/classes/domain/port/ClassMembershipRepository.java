package com.portal.conecta.hub.module.classes.domain.port;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.me.infrastructure.projection.UserCourseClassProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassMembershipRepository extends JpaRepository<ClassMembershipEntity, ClassMembershipId> {
    List<ClassMembershipEntity> findAllByUserId(UUID userId);

    @Query("SELECT COUNT(m) > 0 FROM ClassMembershipEntity m WHERE m.user.id = :userId AND m.classEntity.id = :classId")
    boolean existsByUserIdAndClassId(@Param("userId") UUID userId, @Param("classId") UUID classId);

    @Query("SELECT COUNT(m) FROM ClassMembershipEntity m WHERE m.user.id = :userId AND m.classRole = :classRole AND m.classEntity.deletedAt IS NULL")
    long countByUserIdAndClassRole(@Param("userId") UUID userId, @Param("classRole") ClassRole classRole);

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
    @Query("SELECT COUNT(m) FROM ClassMembershipEntity m WHERE m.classEntity.id = :classId AND m.classRole = :classRole")
    long countByClassIdAndClassRole(@Param("classId") UUID classId, @Param("classRole") ClassRole classRole);

    @Query("""
            SELECT m FROM ClassMembershipEntity m
            JOIN FETCH m.user u
            WHERE m.classEntity.id = :classId
              AND m.classRole IN (:roles)
              AND u.active = true
              AND u.deletedAt IS NULL
        """)
    List<ClassMembershipEntity> findActiveStudentsByClassId(
            @Param("classId") UUID classId,
            @Param("roles")EnumSet<ClassRole> roles
            );

}
