package com.portal.conecta.hub.module.user.domain.port;

import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
     UserEntity findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<UserEntity> findByDeletedAtIsNull(Pageable pageable);

    Page<UserEntity> findByDeletedAtIsNullAndType(TypeUser type, Pageable pageable);
}
