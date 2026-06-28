package com.portal.conecta.hub.shared.observability.logging;

import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.logging.UserIdResolver;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestContextUserIdResolver implements UserIdResolver {

    @Override
    public Optional<String> resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof RequestContext requestContext) {
            return Optional.of(requestContext.userId().toString());
        }

        return Optional.empty();
    }
}