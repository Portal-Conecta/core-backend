package com.portal.conecta.hub.module.user.domain.port;

import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Porta de persistencia do agregado de usuario.
 *
 * <p>As consultas operacionais usam {@link AccountStatus} como criterio
 * explicito de elegibilidade em vez de inferir estado por {@code active} e
 * {@code deletedAt}.</p>
 */
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findAuthUserById(UUID id);

    boolean existsByEmailIgnoreCase(String email);

    Page<UserEntity> findByAccountStatus(AccountStatus accountStatus, Pageable pageable);

    Page<UserEntity> findByAccountStatusAndType(AccountStatus accountStatus, TypeUser type, Pageable pageable);

    Optional<UserEntity> findByIdAndAccountStatus(UUID id, AccountStatus accountStatus);

    List<UserEntity> findAllByIdInAndAccountStatus(List<UUID> ids, AccountStatus accountStatus);

    List<UserEntity> findAllByIdInAndAccountStatusIn(List<UUID> ids, List<AccountStatus> accountStatuses);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    boolean existsByIdAndAccountStatus(UUID id, AccountStatus accountStatus);
}
