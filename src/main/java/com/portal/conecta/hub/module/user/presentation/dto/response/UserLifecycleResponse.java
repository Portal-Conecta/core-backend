package com.portal.conecta.hub.module.user.presentation.dto.response;

import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import java.time.Instant;
import java.util.UUID;

public record UserLifecycleResponse(
        UUID id,
        AccountStatus accountStatus,
        Instant deletedAt
) {
    public static UserLifecycleResponse from(UserEntity user) {
        return new UserLifecycleResponse(
                user.getId(),
                user.getAccountStatus(),
                user.getDeletedAt()
        );
    }
}
