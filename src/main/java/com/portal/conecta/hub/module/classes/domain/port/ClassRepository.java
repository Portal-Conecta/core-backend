package com.portal.conecta.hub.module.classes.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;

public interface ClassRepository extends JpaRepository<ClassEntity, UUID> {

    List<ClassEntity> findAllByIdIn(Collection<UUID> ids);
}
