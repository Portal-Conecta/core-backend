package com.portal.conecta.hub.module.classes.domain.port;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId;

public interface ClassMembershipRepository extends JpaRepository<ClassMembershipEntity, ClassMembershipId> {
    List<ClassMembershipEntity> findAllByUserId(UUID id);
}
