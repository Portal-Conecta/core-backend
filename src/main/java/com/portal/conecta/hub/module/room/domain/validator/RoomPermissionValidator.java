package com.portal.conecta.hub.module.room.domain.validator;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class RoomPermissionValidator {

    private static final EnumSet<TypeUser> ALLOWED_TYPES = EnumSet.of(
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

    public boolean canUpdate(TypeUser typeUser) {
        if (typeUser == null) {
            return false;
        }
        return ALLOWED_TYPES.contains(typeUser);
    }

    public boolean canRemove (TypeUser typeUser){
        if (typeUser == null){
            return false;
        }
        return ALLOWED_TYPES.contains(typeUser);
    }

}
