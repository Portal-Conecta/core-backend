package com.portal.conecta.hub.module.auth.domain.port;

import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;

import java.util.List;
import java.util.UUID;

/**
 * Porta de saída responsável pela geração e validação de tokens JWT.
 *
 * <p>Abstrai o mecanismo de emissão de tokens, permitindo que os use cases
 * sejam independentes da biblioteca JWT utilizada na infraestrutura.</p>
 */
public interface TokenProviderPort {

    /**
     * Gera um refresh token assinado para o usuário informado.
     *
     * @param authUser usuário para o qual o token será emitido
     * @return refresh token JWT assinado
     */
    String generateRefreshToken(AuthUser authUser);

    /**
     * Gera um access token assinado contendo as turmas e o tipo do usuário como claims.
     *
     * @param authUser               usuário autenticado
     * @param classMembershipEntities vínculos do usuário com turmas
     * @return access token JWT assinado
     */
    String generateAccessToken(AuthUser authUser, List<ClassMembershipEntity> classMembershipEntities);

    /**
     * Valida o refresh token e retorna o ID do usuário contido no subject.
     *
     * @param refreshToken token JWT do tipo refresh
     * @return UUID do usuário dono do token
     * @throws AuthException se o token for inválido, expirado ou não for do tipo refresh
     */
    UUID validateRefreshToken(String refreshToken);

    /**
     * @return tempo de expiração do access token configurado, em milissegundos
     */
    Long getAccessTokenExpirationMs();

    /**
     * @return tempo de expiração do refresh token configurado, em milissegundos
     */
    Long getRefreshTokenExpirationMs();

}