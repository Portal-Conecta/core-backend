package com.portal.conecta.hub.module.user.domain.validator;

import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class UserPermissionValidator {

    private static final Set<TypeUser> SENAI_ALLOWED_TYPES = EnumSet.of(
            TypeUser.STUDENT,
            TypeUser.TEACHER
    );

    private static final Set<TypeUser> WEG_ALLOWED_TYPES = EnumSet.of(
            TypeUser.STUDENT,
            TypeUser.WEG
    );

    public boolean canCreate(TypeUser type, TypeUser typeToCreate) {
        if (type == null || typeToCreate == null) {
            return false;
        }

        return switch (type) {
            case ADMIN -> true;

            case SENAI -> SENAI_ALLOWED_TYPES.contains(typeToCreate);

            case WEG -> WEG_ALLOWED_TYPES.contains(typeToCreate);

            case TEACHER, STUDENT, REPRESENTATIVE -> false;
        };
    }

    public void validateCanCreate(TypeUser type, TypeUser typeToCreate) {
        if (typeToCreate == null) {
            throw new InvalidUserDataException("typeUser is required.");
        }

        if (!canCreate(type, typeToCreate)) {
            throw new UserPermissionDeniedException("User does not have permission to create this type of user.");
        }
    }

}
