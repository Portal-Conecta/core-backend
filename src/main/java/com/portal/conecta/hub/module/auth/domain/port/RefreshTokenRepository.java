package com.portal.conecta.hub.module.auth.domain.port;

import com.portal.conecta.hub.module.auth.domain.model.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência para refresh tokens ativos.
 *
 * <p>Cada registro representa um refresh token emitido e ainda não invalidado.
 * Tokens são removidos explicitamente durante a rotação ou revogação de sessão —
 * não há expiração automática por parte do repositório.</p>
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    /**
     * Busca um refresh token pelo valor exato do token JWT.
     *
     * <p>Retorna vazio se o token foi revogado, rotacionado ou nunca persistido.</p>
     */
    Optional<RefreshTokenEntity> findByToken(String token);

}
