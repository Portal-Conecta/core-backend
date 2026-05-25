package com.portal.conecta.hub.module.auth.domain.model;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;

import java.util.UUID;

public interface AuthUser {
    UUID getId();
    String getPasswordHash();
    TypeUser getType();
}