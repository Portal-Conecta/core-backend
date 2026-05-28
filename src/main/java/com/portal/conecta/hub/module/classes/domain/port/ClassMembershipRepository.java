package com.portal.conecta.hub.module.classes.domain.port;

import java.util.List;
import java.util.UUID;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
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

}
