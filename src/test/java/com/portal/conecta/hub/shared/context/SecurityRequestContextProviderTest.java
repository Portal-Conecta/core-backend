package com.portal.conecta.hub.shared.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityRequestContextProviderTest {

    private final SecurityRequestContextProvider requestContextProvider = new SecurityRequestContextProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getRequestContextReturnsAuthenticatedRequestContextPrincipal() {
        RequestContext requestContext = new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                requestContext,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));

        RequestContext result = requestContextProvider.getRequestContext();

        assertEquals(requestContext, result);
    }

    @Test
    void getRequestContextRejectsMissingAuthentication() {
        assertThrows(UnauthorizedUserException.class, requestContextProvider::getRequestContext);
    }

    @Test
    void getRequestContextRejectsAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));

        assertThrows(UnauthorizedUserException.class, requestContextProvider::getRequestContext);
    }

    @Test
    void getRequestContextRejectsPrincipalThatIsNotRequestContext() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));

        assertThrows(UnauthorizedUserException.class, requestContextProvider::getRequestContext);
    }
}
