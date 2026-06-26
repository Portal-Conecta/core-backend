package com.portal.conecta.hub.shared.security.user;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.shared.context.ContextClass;
import com.portal.conecta.hub.shared.context.RequestContext;

/**
 * Representa o usuário autenticado no contexto do Spring Security.
 *
 * <p>Construído pelo {@link com.portal.conecta.hub.shared.security.token.JwtExtractToken} a partir das claims do token JWT.
 * A autoridade concedida segue o padrão {@code ROLE_<userType>}.
 *
 * <p>{@link #getPassword()} retorna {@code null} intencionalmente —
 * a autenticação é baseada em token, não em credencial local.
 */
public class CustomUserDetails implements UserDetails {

    private final String userId;
    private final String userType;
    private final List<ContextClass> classes;
    private final String permissionVersion;
    private final List<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String userId, String userType, List<ContextClass> classes, String permissionVersion) {
        this.userId = userId;
        this.userType = userType;
        this.classes = classes;
        this.permissionVersion = permissionVersion;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_"+userType));
    }

    /**
     * Converte este objeto para {@link RequestContext}, usado como principal
     * no {@link org.springframework.security.core.context.SecurityContextHolder}.
     *
     * @return contexto da requisição com identidade e vínculos do usuário.
     */
    public RequestContext toRequestContext() {
        return new RequestContext(
                UUID.fromString(userId),
                TypeUser.valueOf(userType),
                classes == null ? List.of() : classes
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
