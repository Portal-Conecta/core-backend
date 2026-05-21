package com.portal.conecta.hub.module.auth.domain.port;

import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;

import java.util.List;

public interface TokenProviderPort {
    String generateRefreshToken(AuthUser authUser);
    String generateAccessToken(AuthUser authUser, List<ClassMembershipEntity> classMembershipEntities);
}