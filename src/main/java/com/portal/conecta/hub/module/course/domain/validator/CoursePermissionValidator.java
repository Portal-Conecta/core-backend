package com.portal.conecta.hub.module.course.domain.validator;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class CoursePermissionValidator {

    private static final EnumSet<TypeUser> PERMISSION_TYPES = EnumSet.of(
            TypeUser.ADMIN,
            TypeUser.WEG,
            TypeUser.SENAI
    );

    public boolean canCreate(TypeUser type) {
        if (type == null) return false;
        return PERMISSION_TYPES.contains(type);
    }
}
