package com.portal.conecta.hub.shared.security;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.shared.context.ContextClass;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

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
