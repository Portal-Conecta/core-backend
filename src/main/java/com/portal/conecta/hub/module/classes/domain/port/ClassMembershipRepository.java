package com.portal.conecta.hub.module.classes.domain.port;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClassMembershipRepository extends JpaRepository<ClassMembershipEntity, UUID> {
    List<ClassMembershipEntity> findAllByUserId(UUID id);
}
