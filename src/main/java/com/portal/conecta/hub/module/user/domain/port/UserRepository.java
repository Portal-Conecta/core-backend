package com.portal.conecta.hub.module.user.domain.port;

import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
     UserEntity findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);
}
