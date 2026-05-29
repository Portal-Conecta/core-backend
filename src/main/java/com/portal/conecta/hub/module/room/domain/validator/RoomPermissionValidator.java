package com.portal.conecta.hub.module.room.domain.validator;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RoomPermissionValidator {

    private static final Set<TypeUser> ALLOWED_TYPES = Set.of(
            TypeUser.ADMIN,
            TypeUser.SENAI,
            TypeUser.WEG
    );

    public boolean canCreate(TypeUser typeUser) {
        if (typeUser == null) {
            return false;
        }
        return ALLOWED_TYPES.contains(typeUser);
    }
}
