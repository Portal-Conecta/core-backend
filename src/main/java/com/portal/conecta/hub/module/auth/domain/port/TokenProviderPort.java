package com.portal.conecta.hub.module.auth.domain.port;

import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;

import java.util.List;
import java.util.UUID;

public interface TokenProviderPort {

    String generateRefreshToken(AuthUser authUser);

    String generateAccessToken(AuthUser authUser, List<ClassMembershipEntity> classMembershipEntities);

    UUID validateRefreshToken(String refreshToken);

    Long getAccessTokenExpirationMs();

}