package com.portal.conecta.hub.module.user.domain.port;

import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistência do agregado {@link UserEntity}.
 *
 * <p>Estende {@link JpaRepository} e {@link JpaSpecificationExecutor} para suporte
 * a consultas paginadas e dinâmicas. Consultas com {@code deletedAtIsNull} e {@code activeTrue}
 * restringem os resultados a usuários ativos (não excluídos via soft delete).
 */
public interface UserRepository extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    /** Busca usuário por e-mail para uso no fluxo de autenticação. */
    Optional<AuthUser> findByEmail(String email);

    /** Busca projeção de autenticação pelo ID do usuário. */
    Optional<AuthUser> findAuthUserById(UUID id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    /** Verifica existência de e-mail sem distinção de maiúsculas/minúsculas. */
    boolean existsByEmailIgnoreCase(String email);

    /** Retorna página de usuários ativos (sem soft delete), sem filtro de tipo. */
    Page<UserEntity> findByDeletedAtIsNull(Pageable pageable);

    /** Retorna página de usuários ativos filtrados por tipo. */
    Page<UserEntity> findByDeletedAtIsNullAndType(TypeUser type, Pageable pageable);

    /** Busca usuário ativo pelo ID; retorna vazio se excluído ou inativo. */
    Optional<UserEntity> findByIdAndDeletedAtIsNullAndActiveTrue(UUID id);

    /** Busca múltiplos usuários ativos por lista de IDs; IDs inexistentes ou inativos são ignorados. */
    List<UserEntity> findAllByIdInAndDeletedAtIsNullAndActiveTrue(List<UUID> ids);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    /** Verifica se o usuário está ativo e não excluído. */
    boolean existsByIdAndDeletedAtIsNullAndActiveTrue(UUID id);
}
