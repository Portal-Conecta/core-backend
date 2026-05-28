package com.portal.conecta.hub.module.classes.domain.validator;

import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class ClassPermissionValidator {

    private static final EnumSet<TypeUser> PERMISSION_TYPES = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.WEG,
            TypeUser.SENAI
    );

    public boolean canCreate(TypeUser type) {
        if (type == null) return false;
        return PERMISSION_TYPES.contains(type);
    }

    private static final EnumSet<TypeUser> DELETE_PERMISSION_TYPES = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.WEG,
            TypeUser.SENAI
    );

    public boolean canDelete(TypeUser type){
        if (type == null) return false;
        return DELETE_PERMISSION_TYPES.contains(type);
    }

    public void validateCanDelete (TypeUser type){
        if (!canDelete(type)){
            throw new UserPermissionDeniedException("User does not have permission to delete a class.");
        }
    }
}
