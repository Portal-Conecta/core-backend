package com.portal.conecta.hub.shared.context;

import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementação de {@link RequestContextProvider} baseada no {@link org.springframework.security.core.context.SecurityContextHolder}.
 *
 * <p>Extrai o {@link RequestContext} do principal da autenticação corrente,
 * que é preenchido pelo filtro JWT após validação do token.
 * Rejeita requisições anônimas e autenticações cujo principal não seja um {@link RequestContext}.
 *
 * @throws UnauthorizedUserException se a autenticação estiver ausente, for anônima ou o principal não corresponder ao tipo esperado.
 */
@Component
public class SecurityRequestContextProvider implements RequestContextProvider {

    @Override
    public RequestContext getRequestContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedUserException("Autenticação obrigatória.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof RequestContext requestContext) {
            return requestContext;
        }

        throw new UnauthorizedUserException("O contexto da requisição autenticada não está disponível.");
    }
}
