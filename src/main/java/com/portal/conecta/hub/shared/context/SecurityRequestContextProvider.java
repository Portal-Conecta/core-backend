package com.portal.conecta.hub.shared.context;

import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
