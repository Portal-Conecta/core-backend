package com.portal.conecta.hub.module.classes.domain.port;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassRepository extends JpaRepository<ClassEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT MAX(c.number) FROM ClassEntity c WHERE c.course.id = :courseId")
    Optional<Integer> findLastNumberByCourseId(@Param("courseId") UUID courseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ClassEntity c WHERE c.id = :id")
    Optional<ClassEntity> findByIdForUpdate(@Param("id") UUID id);

    Optional<ClassEntity> findByIdAndDeletedAtIsNull(UUID id);

    @Query(value = """
    SELECT c FROM ClassEntity c WHERE c.id IN :ids AND c.deletedAt IS NULL
        """)
    List<ClassEntity> findAllByIdInAndDeletedAtIsNull(@Param("ids") List<UUID> ids);
}
