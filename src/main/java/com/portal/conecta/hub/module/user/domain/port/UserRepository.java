package com.portal.conecta.hub.module.user.domain.port;

import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findAuthUserById(UUID id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByEmailIgnoreCase(String email);

    Page<UserEntity> findByDeletedAtIsNull(Pageable pageable);

    Page<UserEntity> findByDeletedAtIsNullAndType(TypeUser type, Pageable pageable);

    Optional<UserEntity> findByIdAndDeletedAtIsNullAndActiveTrue(UUID id);

    List<UserEntity> findAllByIdInAndDeletedAtIsNullAndActiveTrue(List<UUID> ids);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

}
