package com.portal.conecta.hub.module.auth.domain.port;

import com.portal.conecta.hub.module.auth.domain.model.AuthUser;

public interface TokenProviderPort {
    String generateRefreshToken(AuthUser authUser);
    String generateAccessToken(AuthUser authUser);
}