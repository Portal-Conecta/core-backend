package com.portal.conecta.hub.shared.observability.logging;

import com.portal.conecta.hub.shared.context.RequestContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticatedUserLogResolver {

    /**
     * Retorna o {@code userId} do usuário autenticado para inclusão em logs.
     *
     * @return {@code userId} como {@code String}, ou {@link Optional#empty()}
     *         se a requisição for anônima ou o principal não for um {@link com.portal.conecta.hub.shared.context.RequestContext}.
     */
    public Optional<String> resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof RequestContext requestContext) {
            return Optional.of(requestContext.userId().toString());
        }

        return Optional.empty();
    }

}