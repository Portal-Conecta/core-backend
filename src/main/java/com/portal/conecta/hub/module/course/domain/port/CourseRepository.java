package com.portal.conecta.hub.module.course.domain.port;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    boolean existsByName(String name);

    boolean existsByCode(String code);

    Optional<CourseEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<CourseEntity> findAllByDeletedAtIsNull();
}